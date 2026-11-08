package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.cache.BookingCart;
import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.client.PaymentClient;
import com.example.bookingserviceproject.dto.payment.PaymentResult;
import com.example.bookingserviceproject.dto.request.CardDetails;
import com.example.bookingserviceproject.dto.response.BookingResponse;
import com.example.bookingserviceproject.exception.ConflictException;
import com.example.bookingserviceproject.exception.EventPublishException;
import com.example.bookingserviceproject.exception.InternalServerException;
import com.example.bookingserviceproject.exception.NotFoundException;
import com.example.bookingserviceproject.exception.PassThroughException;
import com.example.bookingserviceproject.exception.PaymentUnavailableException;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import com.example.bookingserviceproject.mapper.BookingNotificationMapper;
import com.example.bookingserviceproject.mapper.BookingResMapper;
import com.example.bookingserviceproject.messaging.InventoryEventPublisher;
import com.example.bookingserviceproject.messaging.NotificationEventPublisher;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.utils.PriceUtils;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Confirming a booking: paying for the user's cart, saving it as a booking,
 * and telling the inventory-service and the notification-service. The full
 * step-by-step is in 02-booking-flow.md ("Confirm, step by step").
 * <p>
 * There is no idempotency key: the user's cart, its bookingId and its
 * {@code completed} field act as one. A second confirm is refused while the
 * first runs, and finds no cart once the first has finished.
 */
@Service
public class CheckoutService {
    private static final Logger log = LoggerFactory.getLogger(CheckoutService.class);

    // The first confirm attempt extends the cart by this much: still 2 minutes before inventory's cart expires
    private static final Duration CONFIRM_EXTENSION = Duration.ofMinutes(3);
    private static final String CART_EMPTY = "Your cart is empty";
    private static final String PAYMENT_UNAVAILABLE = "Your payment could not be confirmed. Please try again";

    private final BookingCart bookingCart;
    private final BookingService bookingService;
    private final PaymentClient paymentClient;
    private final InventoryEventPublisher inventoryEventPublisher;
    private final NotificationEventPublisher notificationEventPublisher;
    private final BookingResMapper bookingResMapper;
    private final BookingNotificationMapper bookingNotificationMapper;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingCart                access to the carts in the booking cache
     * @param bookingService             saves and loads bookings
     * @param paymentClient              client for the payment-service
     * @param inventoryEventPublisher    sends the events the inventory-service listens to
     * @param notificationEventPublisher sends the events the notification-service listens to
     * @param bookingResMapper           mapper from a booking to its response
     * @param bookingNotificationMapper  mapper from a booking to the confirmation email's notification
     */
    public CheckoutService(BookingCart bookingCart, BookingService bookingService, PaymentClient paymentClient,
                           InventoryEventPublisher inventoryEventPublisher, NotificationEventPublisher notificationEventPublisher,
                           BookingResMapper bookingResMapper, BookingNotificationMapper bookingNotificationMapper){
        this.bookingCart = bookingCart;
        this.bookingService = bookingService;
        this.paymentClient = paymentClient;
        this.inventoryEventPublisher = inventoryEventPublisher;
        this.notificationEventPublisher = notificationEventPublisher;
        this.bookingResMapper = bookingResMapper;
        this.bookingNotificationMapper = bookingNotificationMapper;
    }

    /**
     * Pays for the user's cart and saves it as a booking.
     *
     * @param userId the id of the user
     * @param card   the card to pay with
     * @return the confirmed booking
     * @throws NotFoundException           if the user has no cart, or it is empty or expired
     * @throws ConflictException           if the booking is already being completed
     * @throws PassThroughException        if the payment is refused (402 declined, 400 invalid card)
     * @throws ServiceUnavailableException if the payment could not be confirmed
     * @throws InternalServerException     if the booking could not be saved after the payment
     */
    public BookingResponse confirm(UUID userId, CardDetails card){
        // 1. Read the cart
        CartContents cart = fromCache(() -> bookingCart.getCart(userId));
        if (cart.isEmpty()) throw new NotFoundException(CART_EMPTY);

        // 2. An earlier confirm saved the booking, but stopped before deleting the cart (e.g. the service crashed)
        Optional<BookingDetails> saved = bookingService.findDetails(cart.bookingId());
        if (saved.isPresent()){
            log.warn("Booking {} of user {} was already saved, but its cart was not deleted. Returning the booking", cart.bookingId(), userId);
            deleteCart(userId);
            return bookingResMapper.toResponse(saved.get());
        }

        // 3. Lock the cart: no other confirm can start, and the cart cannot be changed
        if (!fromCache(() -> bookingCart.lockForCompletion(userId))){
            throw new ConflictException("Your booking is already being completed");
        }

        // 4. Read again: the cart may have been emptied or expired just before the lock. Locking a cart that
        //    expired creates a new key with no expiry, which must not be left behind. Only this read is used from here
        CartContents locked = fromCache(() -> bookingCart.getCart(userId));
        if (locked.isEmpty()){
            deleteCart(userId);
            throw new NotFoundException(CART_EMPTY);
        }

        // 5. Give the first attempt time for slow connections and the payment. Later attempts do not extend it
        fromCache(() -> {
            bookingCart.extendExpiryOnce(userId, CONFIRM_EXTENSION);
            return null;
        });

        // 6. Pay
        PaymentResult payment = pay(userId, locked, card);

        return completeBooking(userId, locked, payment);
    }

    /**
     * Charges the cart's total. The payment client retries once by itself if
     * the payment-service does not answer, and the circuit breaker stops calling
     * it for a while after repeated failures. On any failure the cart is opened
     * again, so the user can change it and try again.
     *
     * @param userId the id of the user
     * @param cart   the locked cart
     * @param card   the card to pay with
     * @return the successful payment
     */
    private PaymentResult pay(UUID userId, CartContents cart, CardDetails card){
        try {
            return paymentClient.charge(userId, cart.bookingId(), cart.total(), PriceUtils.CURRENCY, card);
        } catch (RuntimeException e){
            openCart(userId);
            throw toPaymentError(cart.bookingId(), e);
        }
    }

    /**
     * Steps 7-11: saves the paid cart, deletes it, and tells inventory and the
     * notification-service. Kept separate, so it can later run when a payment
     * result arrives through Kafka.
     *
     * @param userId  the id of the user
     * @param cart    the paid cart
     * @param payment the successful payment
     * @return the confirmed booking
     */
    private BookingResponse completeBooking(UUID userId, CartContents cart, PaymentResult payment){
        BookingDetails details;
        try {
            details = bookingService.saveFromCart(userId, cart);
        } catch (DataAccessException e){
            // Not decided yet (edgecase-notes.md, edge case 3). For now: refund in full, and open the cart again
            log.error("Booking {} of user {} was paid (payment {}) but could not be saved. Asking for a full refund",
                    cart.bookingId(), userId, payment.paymentId(), e);
            refund(cart.bookingId());
            openCart(userId);
            throw new InternalServerException("Your booking could not be saved, and your payment will be refunded. Please try again");
        }

        // The booking is in the database now, so the cart is redundant
        deleteCart(userId);

        // Inventory stores the rooms as booked. The booking is paid and saved, so a failure must not fail the request
        try {
            inventoryEventPublisher.reservationConfirmed(userId);
        } catch (EventPublishException e){
            List<UUID> itemIds = details.items().stream().map(BookingItem::getItemId).toList();
            log.error("Booking {} of user {} is paid and saved, but inventory was not told (reservation-confirmed). Rooms: {}",
                    cart.bookingId(), userId, itemIds, e);
        }

        // Does not wait, and never throws: the email must never undo a booking
        notificationEventPublisher.bookingConfirmed(bookingNotificationMapper.toNotification(details));

        log.info("Booking {} of user {} confirmed (payment {})", cart.bookingId(), userId, payment.paymentId());
        return bookingResMapper.toResponse(details);
    }

    /**
     * Turns a failed payment into the error returned to the user.
     *
     * @param bookingId the booking that was not paid
     * @param e         why the payment failed
     * @return the payment-service's refusal as it is (402, 400), or 503 if the outcome is unknown
     */
    private RuntimeException toPaymentError(UUID bookingId, RuntimeException e){
        // 409: a payment for this booking is still in progress, so the outcome is unknown, as after a timeout
        boolean inProgress = e instanceof PassThroughException refused && refused.getStatus().value() == HttpStatus.CONFLICT.value();
        if (inProgress || e instanceof PaymentUnavailableException || e instanceof CallNotPermittedException){
            log.warn("Payment for booking {} could not be confirmed: {}", bookingId, e.getMessage());
            return new ServiceUnavailableException(PAYMENT_UNAVAILABLE);
        }
        return e;
    }

    /**
     * Opens the cart again after a failed payment. If Redis fails, the cart
     * stays locked until it expires, so it is only logged.
     *
     * @param userId the id of the user
     */
    private void openCart(UUID userId){
        try {
            bookingCart.unlock(userId);
        } catch (DataAccessException e){
            log.error("Could not open the cart of user {} again. It stays locked until it expires", userId, e);
        }
    }

    /**
     * Deletes the user's cart. If Redis fails, it is only logged: the cart
     * stays locked until it expires, and a new confirm finds the saved booking (step 2).
     *
     * @param userId the id of the user
     */
    private void deleteCart(UUID userId){
        try {
            bookingCart.deleteCart(userId);
        } catch (DataAccessException e){
            log.error("Could not delete the cart of user {}", userId, e);
        }
    }

    /**
     * Asks the payment-service to refund a booking that could not be saved.
     * If that fails too, it is logged, so it can be refunded by hand (or later by a recovery job).
     *
     * @param bookingId the booking to refund
     */
    private void refund(UUID bookingId){
        try {
            paymentClient.refundFailedBooking(bookingId);
        } catch (RuntimeException e){
            log.error("Could not ask for a refund of booking {}. It must be refunded by hand", bookingId, e);
        }
    }

    /**
     * Runs a call to the booking cache, and turns a Redis failure into an
     * error the client understands.
     *
     * @param call the call to run
     * @param <T>  the type of the result
     * @return the result of the call
     * @throws InternalServerException if Redis cannot be reached
     */
    private <T> T fromCache(Supplier<T> call){
        try {
            return call.get();
        } catch (DataAccessException e){
            log.error("Could not reach the booking cache", e);
            throw new InternalServerException("Your cart is temporarily unavailable. Please try again");
        }
    }
}

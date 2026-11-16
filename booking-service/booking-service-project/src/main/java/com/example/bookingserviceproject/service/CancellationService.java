package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.dto.event.BookingCancelledPaymentEvent;
import com.example.bookingserviceproject.exception.ConflictException;
import com.example.bookingserviceproject.exception.EventPublishException;
import com.example.bookingserviceproject.exception.NotFoundException;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import com.example.bookingserviceproject.messaging.InventoryEventPublisher;
import com.example.bookingserviceproject.messaging.PaymentEventPublisher;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.repository.BookingItemRepository;
import com.example.bookingserviceproject.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Cancelling paid bookings. The full flow is in 02-booking-flow.md ("Cancel a paid booking").
 * <p>
 * A cancelled booking is an umbrella: only {@code bookings.cancelled} is set,
 * and its rooms and extras count as cancelled too. Refunds are decided by the
 * payment-service, which is told before the cancellation becomes final.
 */
@Service
public class CancellationService {
    private static final Logger log = LoggerFactory.getLogger(CancellationService.class);

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final InventoryEventPublisher inventoryEventPublisher;
    private final TransactionTemplate transactionTemplate;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingRepository       the bookings
     * @param bookingItemRepository   the booked rooms
     * @param paymentEventPublisher   sends the events the payment-service listens to
     * @param inventoryEventPublisher sends the events the inventory-service listens to
     * @param transactionManager      runs the cancellation in one transaction, ended inside this class
     */
    public CancellationService(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository,
                               PaymentEventPublisher paymentEventPublisher, InventoryEventPublisher inventoryEventPublisher,
                               PlatformTransactionManager transactionManager){
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.paymentEventPublisher = paymentEventPublisher;
        this.inventoryEventPublisher = inventoryEventPublisher;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    /**
     * Cancels one of the user's bookings. The payment-service is told inside the
     * transaction, before the commit: if it cannot be told, nothing is cancelled.
     * Inventory is told after the commit, so it only frees rooms of a booking that is really cancelled.
     *
     * @param userId    the id of the user
     * @param bookingId the id of the booking
     * @throws NotFoundException           if the user has no booking with this id
     * @throws ConflictException           if it is already cancelled, being cancelled right now, or has started
     * @throws ServiceUnavailableException if the payment-service could not be told, so nothing was cancelled
     */
    public void cancelMyBooking(UUID userId, UUID bookingId){
        List<UUID> itemIds;
        try {
            // Steps 1-5 in one transaction: an exception rolls everything back
            itemIds = transactionTemplate.execute(status -> cancelInTransaction(userId, bookingId));
        } catch (PessimisticLockingFailureException e){
            // Another request holds the lock: it is cancelling this booking right now. Refused at once, whatever its outcome
            throw new ConflictException("This booking is already being cancelled");
        } catch (EventPublishException e){
            log.error("Booking {} of user {} was not cancelled: the payment-service could not be told", bookingId, userId, e);
            throw new ServiceUnavailableException("The booking could not be cancelled right now. Please try again later");
        }

        // 6. After the commit, without waiting. A failure is only logged: the rooms stay blocked in inventory until fixed
        inventoryEventPublisher.bookingCancelled(bookingId, itemIds);
        log.info("Booking {} cancelled by user {}", bookingId, userId);
    }

    /**
     * Steps 1-4 of the cancellation. Runs inside the transaction, which is
     * committed (step 5) when this returns, and rolled back if it throws.
     *
     * @param userId    the id of the user
     * @param bookingId the id of the booking
     * @return the ids of the booking's rooms, for inventory
     */
    private List<UUID> cancelInTransaction(UUID userId, UUID bookingId){
        // 1. Lock the booking without waiting: a second request while this one runs fails at once
        Booking booking = bookingRepository.findLockedByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        // 2. Can it be cancelled?
        if (booking.isCancelled()) throw new ConflictException("This booking is already cancelled");
        if (booking.getStartTime().isBefore(LocalDateTime.now())){
            throw new ConflictException("This booking has already started, and can no longer be cancelled");
        }
        List<UUID> itemIds = bookingItemRepository.findAllByBooking_BookingId(bookingId).stream()
                .map(BookingItem::getItemId)
                .toList();

        // 3. Cancel, and write it to the database now, so a database problem shows before the payment-service is told
        booking.cancel();
        bookingRepository.flush();

        // 4. Tell the payment-service, waiting for Kafka. If this throws, the cancellation is rolled back
        paymentEventPublisher.bookingCancelled(new BookingCancelledPaymentEvent(
                bookingId, userId, booking.getStartTime(), Instant.now(), false));

        return itemIds;
    }
}

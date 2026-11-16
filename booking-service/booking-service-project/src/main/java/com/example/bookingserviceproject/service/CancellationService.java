package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.dto.event.BookingCancelledPaymentEvent;
import com.example.bookingserviceproject.dto.event.ItemCancelledPaymentEvent;
import com.example.bookingserviceproject.exception.ConflictException;
import com.example.bookingserviceproject.exception.EventPublishException;
import com.example.bookingserviceproject.exception.NotFoundException;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import com.example.bookingserviceproject.messaging.InventoryEventPublisher;
import com.example.bookingserviceproject.messaging.PaymentEventPublisher;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingDetails;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.model.CancelledBy;
import com.example.bookingserviceproject.model.CancelledItem;
import com.example.bookingserviceproject.repository.BookingAddonRepository;
import com.example.bookingserviceproject.repository.BookingItemRepository;
import com.example.bookingserviceproject.repository.BookingRepository;
import com.example.bookingserviceproject.repository.CancelledAddonRepository;
import com.example.bookingserviceproject.repository.CancelledItemRepository;
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
import java.util.function.Supplier;

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
    private final BookingAddonRepository bookingAddonRepository;
    private final CancelledItemRepository cancelledItemRepository;
    private final CancelledAddonRepository cancelledAddonRepository;
    private final PaymentEventPublisher paymentEventPublisher;
    private final InventoryEventPublisher inventoryEventPublisher;
    private final TransactionTemplate transactionTemplate;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingRepository       the bookings
     * @param bookingItemRepository   the booked rooms
     * @param bookingAddonRepository   the extras on booked rooms
     * @param cancelledItemRepository  the cancelled rooms
     * @param cancelledAddonRepository the cancelled extras
     * @param paymentEventPublisher   sends the events the payment-service listens to
     * @param inventoryEventPublisher sends the events the inventory-service listens to
     * @param transactionManager      runs the cancellation in one transaction, ended inside this class
     */
    public CancellationService(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository,
                               BookingAddonRepository bookingAddonRepository, CancelledItemRepository cancelledItemRepository,
                               CancelledAddonRepository cancelledAddonRepository, PaymentEventPublisher paymentEventPublisher,
                               InventoryEventPublisher inventoryEventPublisher, PlatformTransactionManager transactionManager){
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.bookingAddonRepository = bookingAddonRepository;
        this.cancelledItemRepository = cancelledItemRepository;
        this.cancelledAddonRepository = cancelledAddonRepository;
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
        // Steps 1-5 in one transaction: an exception rolls everything back
        List<UUID> itemIds = inCancellationTransaction("booking", bookingId, userId,
                () -> cancelBookingInTransaction(userId, bookingId));

        // 6. After the commit, without waiting. A failure is only logged: the rooms stay blocked in inventory until fixed
        inventoryEventPublisher.bookingCancelled(bookingId, itemIds);
        log.info("Booking {} cancelled by user {}", bookingId, userId);
    }

    /**
     * Cancels one room of the user's booking, and with it the extras on that
     * room. If it was the last active room, the whole booking is cancelled too.
     * The payment-service is told inside the transaction, before the commit: if
     * it cannot be told, nothing is cancelled. Inventory is told after the commit.
     *
     * @param userId    the id of the user
     * @param bookingId the id of the booking
     * @param itemId    the id of the room
     * @throws NotFoundException           if the user has no booking with this id, or the room is not in it
     * @throws ConflictException           if the booking is cancelled, being cancelled right now, or has started,
     *                                     or the room is already cancelled
     * @throws ServiceUnavailableException if the payment-service could not be told, so nothing was cancelled
     */
    public void cancelMyRoom(UUID userId, UUID bookingId, UUID itemId){
        inCancellationTransaction("room", bookingId, userId, () -> cancelRoomInTransaction(userId, bookingId, itemId));

        // After the commit, without waiting: only this room is freed
        inventoryEventPublisher.bookingCancelled(bookingId, List.of(itemId));
        log.info("Room {} of booking {} cancelled by user {}", itemId, bookingId, userId);
    }

    /**
     * Steps 1-4 of cancelling a whole booking. Runs inside the transaction,
     * which is committed (step 5) when this returns, and rolled back if it throws.
     *
     * @param userId    the id of the user
     * @param bookingId the id of the booking
     * @return the ids of the booking's rooms, for inventory
     */
    private List<UUID> cancelBookingInTransaction(UUID userId, UUID bookingId){
        // 1. Lock the booking without waiting: a second request while this one runs fails at once
        Booking booking = lockMyBooking(userId, bookingId, "Booking not found");

        // 2. Can it be cancelled?
        checkCanBeCancelled(booking);
        // Rooms or extras cancelled earlier are not refunded again: only what is still active
        BookingDetails details = loadDetails(booking);
        int amount = details.activeAmount();
        List<UUID> itemIds = details.items().stream().map(BookingItem::getItemId).toList();

        // 3. Cancel, and write it to the database now, so a database problem shows before the payment-service is told
        booking.cancel();
        bookingRepository.flush();

        // 4. Tell the payment-service, waiting for Kafka. If this throws, the cancellation is rolled back
        paymentEventPublisher.bookingCancelled(new BookingCancelledPaymentEvent(
                bookingId, userId, amount, booking.getStartTime(), Instant.now(), false));

        return itemIds;
    }

    /**
     * Cancels one room inside the transaction, which is committed when this
     * returns, and rolled back if it throws.
     *
     * @param userId    the id of the user
     * @param bookingId the id of the booking
     * @param itemId    the id of the room
     * @return nothing: inventory only needs the room's own id
     */
    private Void cancelRoomInTransaction(UUID userId, UUID bookingId, UUID itemId){
        // Lock the booking without waiting: no other cancellation on this booking runs at the same time.
        // A missing booking and a room that is not in it give the same answer, so nobody learns which ids exist
        Booking booking = lockMyBooking(userId, bookingId, "Room not found");
        BookingDetails details = loadDetails(booking);
        BookingItem item = details.items().stream()
                .filter(room -> room.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Room not found"));

        checkCanBeCancelled(booking);
        if (details.isItemCancelled(itemId)) throw new ConflictException("This room is already cancelled");

        // Its extras are cancelled with it (no rows of their own), so the amount includes those still active
        int amount = details.activeAmountOf(item);
        // A booking cannot be active without an active room
        boolean lastActiveRoom = details.activeItems().size() == 1;

        Instant now = Instant.now();
        cancelledItemRepository.save(new CancelledItem(item, booking, now, CancelledBy.CUSTOMER));
        if (lastActiveRoom) booking.cancel();
        // Write it to the database now, so a database problem shows before the payment-service is told.
        // The UNIQUE item_id is a second guard against cancelling the same room twice
        cancelledItemRepository.flush();

        // Always this one event, also for the last room: it covers the room and its extras
        paymentEventPublisher.itemCancelled(new ItemCancelledPaymentEvent(
                bookingId, itemId, userId, amount, booking.getStartTime(), now, false));
        return null;
    }

    /**
     * Runs a cancellation in one transaction, and turns its failures into
     * errors the user understands. An exception rolls everything back.
     *
     * @param what      what is cancelled, for the error message, e.g. "booking" or "room"
     * @param bookingId the id of the booking, for the logs
     * @param userId    the id of the user, for the logs
     * @param work      the cancellation
     * @param <T>       the type of its result
     * @return the result of the cancellation
     * @throws ConflictException           if another request is cancelling something on this booking right now
     * @throws ServiceUnavailableException if the payment-service could not be told
     */
    private <T> T inCancellationTransaction(String what, UUID bookingId, UUID userId, Supplier<T> work){
        try {
            return transactionTemplate.execute(status -> work.get());
        } catch (PessimisticLockingFailureException e){
            // Another request holds the lock: it is cancelling something on this booking right now. Refused at once
            throw new ConflictException("This booking is already being cancelled");
        } catch (EventPublishException e){
            log.error("The {} of booking {} (user {}) was not cancelled: the payment-service could not be told", what, bookingId, userId, e);
            throw new ServiceUnavailableException("The " + what + " could not be cancelled right now. Please try again later");
        }
    }

    /**
     * Finds the user's booking and locks its row without waiting. Must be called inside the transaction.
     *
     * @param userId          the id of the user
     * @param bookingId       the id of the booking
     * @param notFoundMessage the message if the user has no booking with this id
     * @return the locked booking
     * @throws NotFoundException if the user has no booking with this id
     */
    private Booking lockMyBooking(UUID userId, UUID bookingId, String notFoundMessage){
        return bookingRepository.findLockedByBookingIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new NotFoundException(notFoundMessage));
    }

    /**
     * Only upcoming, active bookings (and their rooms and extras) can be cancelled.
     *
     * @param booking the booking
     * @throws ConflictException if it is already cancelled, or has started
     */
    private void checkCanBeCancelled(Booking booking){
        if (booking.isCancelled()) throw new ConflictException("This booking is already cancelled");
        if (booking.getStartTime().isBefore(LocalDateTime.now())){
            throw new ConflictException("This booking has already started, and can no longer be cancelled");
        }
    }

    /**
     * Loads everything that belongs to a booking, one query per table.
     *
     * @param booking the booking
     * @return the booking with its rooms, extras and cancellations
     */
    private BookingDetails loadDetails(Booking booking){
        UUID bookingId = booking.getBookingId();
        return new BookingDetails(
                booking,
                bookingItemRepository.findAllByBooking_BookingId(bookingId),
                bookingAddonRepository.findAllByBooking_BookingId(bookingId),
                cancelledItemRepository.findAllByBooking_BookingId(bookingId),
                cancelledAddonRepository.findAllByBooking_BookingId(bookingId));
    }
}

package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.cache.CartItem;
import com.example.inventoryserviceproject.cache.ReservedCache;
import com.example.inventoryserviceproject.dto.event.BookingCancelledEvent;
import com.example.inventoryserviceproject.dto.event.BookingConfirmedEvent;
import com.example.inventoryserviceproject.dto.event.ReservationCancelledEvent;
import com.example.inventoryserviceproject.dto.event.ReservationItemRemovedEvent;
import com.example.inventoryserviceproject.exception.ConflictException;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding.KafkaAsyncKey;
import io.github.springwolf.bindings.kafka.annotations.KafkaAsyncOperationBinding.KafkaAsyncMessageBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncListener;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service handling events from the booking-service about reservations: storing
 * reserved rooms when a booking is confirmed, deleting them when a completed
 * booking is cancelled, releasing one room when it is removed from a cart, and
 * releasing all of them when a reservation is cancelled.
 * Only the entry point for the events; the work is done by {@link RoomService}
 * (database) and {@link ReservedCache} (cache).
 *
 * <p>Messages are read as text and converted here, so each listener decides
 * its own event type. Malformed messages are skipped, as retrying them cannot
 * succeed. If Redis or the database cannot be reached, the error is thrown so
 * Kafka retries.
 *
 * <p>Each listener is documented for Springwolf with {@link AsyncListener},
 * shown at /inventory/springwolf/asyncapi-ui.html. The listeners take a raw
 * String, so the payload type is given there explicitly.
 */
@Service
public class ReservationEventService {
    private static final Logger log = LoggerFactory.getLogger(ReservationEventService.class);

    private final ReservedCache reservedCache;
    private final RoomService roomService;
    private final JsonMapper jsonMapper;

    /**
     * Creates the service with the cache, service and JSON mapper it uses.
     *
     * @param reservedCache cache holding rooms in users' carts
     * @param roomService   service storing occupied rooms in the database
     * @param jsonMapper    mapper used to read the events
     */
    public ReservationEventService(ReservedCache reservedCache, RoomService roomService, JsonMapper jsonMapper){
        this.reservedCache = reservedCache;
        this.roomService = roomService;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Stores a user's reserved rooms as booked in the database when they have
     * paid and completed a booking, then releases them from the cache.
     *
     * @param message the event as JSON, e.g. {"userId": "..."}
     */
    @AsyncListener(operation = @AsyncOperation(
            channelName = "reservation-confirmed",
            description = """
                    Sent when a user has paid and completed a booking. Stores every room in the user's cart as booked \
                    in the database (one occupation per cart item, using the itemId as its id), then releases the cart.

                    - The cart is read from the inventory-service's own cache. The event only names the user.
                    - Safe to receive twice: an already handled event finds an empty cart and is ignored.
                    - If the cart has expired (30 minutes after the first room was added), nothing is stored. \
                    The booking-service must confirm before its own 25-minute limit.
                    - If a room can no longer be stored (conflict), nothing is stored and the cart is left to expire. \
                    The booking-service is not told yet.
                    - Malformed messages are skipped. If Redis or the database is down, Kafka retries.""",
            payloadType = BookingConfirmedEvent.class))
    @KafkaAsyncOperationBinding(groupId = "inventory-service-group",
            messageBinding = @KafkaAsyncMessageBinding(key = @KafkaAsyncKey(description = "The user id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")))
    @KafkaListener(topics = "reservation-confirmed", groupId = "inventory-service-group")
    public void onBookingConfirmed(String message){
        Optional<BookingConfirmedEvent> event = parse(message, BookingConfirmedEvent.class, "reservation-confirmed");
        if (event.isEmpty() || event.get().userId() == null) return;
        UUID userId = event.get().userId();

        // Empty if the cart expired, or the event was already handled and the cart released
        List<CartItem> items = reservedCache.getCartItems(userId);
        if (items.isEmpty()){
            log.warn("No reserved rooms to store for user {}: cart expired or already confirmed", userId);
            return;
        }

        try {
            int saved = roomService.saveBookedRooms(items);
            log.info("Stored {} booked room(s) for user {}", saved, userId);
        } catch (ConflictException e){
            // Retrying cannot help, so the cart is left to expire. The booking-service must be told (not handled yet)
            log.error("Could not store booked rooms for user {}: {}", userId, e.getMessage());
            return;
        }

        reservedCache.releaseCart(userId);
    }

    /**
     * Makes the rooms of a completed (paid) booking available again when it is
     * cancelled. Not used for reservations that are not yet completed.
     *
     * @param message the event as JSON, e.g. {"itemIds": ["...", "..."]}
     */
    @AsyncListener(operation = @AsyncOperation(
            channelName = "booking-cancelled",
            description = """
                    Sent when a completed (paid) booking is cancelled. Deletes the booked occupations of the given items, \
                    so the rooms are available again. Not used for unpaid reservations: use reservation-cancelled.

                    - Only booked occupations are deleted. Periods blocked by staff are never touched.
                    - Safe to receive twice: items already cancelled, or unknown item ids, are ignored.
                    - Malformed messages, and messages without itemIds, are skipped. If the database is down, Kafka retries.""",
            payloadType = BookingCancelledEvent.class))
    @KafkaAsyncOperationBinding(groupId = "inventory-service-group")
    @KafkaListener(topics = "booking-cancelled", groupId = "inventory-service-group")
    public void onBookingCancelled(String message){
        Optional<BookingCancelledEvent> event = parse(message, BookingCancelledEvent.class, "booking-cancelled");
        if (event.isEmpty() || event.get().itemIds() == null || event.get().itemIds().isEmpty()){
            if (event.isPresent()) log.warn("Skipping booking-cancelled event without itemIds: {}", message);
            return;
        }

        long cancelled = roomService.cancelBookedRooms(event.get().itemIds());
        log.info("Made {} of {} booked room(s) available again", cancelled, event.get().itemIds().size());
    }

    /**
     * Removes one room from a user's cart and releases it, when the user removes
     * it from their cart in the booking-service before completing the booking.
     *
     * @param message the event as JSON, e.g. {"userId": "...", "itemId": "..."}
     */
    @AsyncListener(operation = @AsyncOperation(
            channelName = "reservation-item-remove",
            description = """
                    Sent when a user removes one room from their cart before paying. Releases that room so others can reserve it.

                    - When the last room is removed, the whole cart is deleted.
                    - Safe to receive twice: an item that is already removed, or has expired, is ignored.
                    - Malformed messages, and messages without userId or itemId, are skipped. If Redis is down, Kafka retries.""",
            payloadType = ReservationItemRemovedEvent.class))
    @KafkaAsyncOperationBinding(groupId = "inventory-service-group",
            messageBinding = @KafkaAsyncMessageBinding(key = @KafkaAsyncKey(description = "The user id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")))
    @KafkaListener(topics = "reservation-item-remove", groupId = "inventory-service-group")
    public void onReservationItemRemoved(String message){
        Optional<ReservationItemRemovedEvent> event = parse(message, ReservationItemRemovedEvent.class, "reservation-item-remove");
        if (event.isEmpty() || event.get().userId() == null || event.get().itemId() == null) return;

        boolean removed = reservedCache.removeCartItem(event.get().userId(), event.get().itemId());
        if (removed) log.info("Removed item {} from the cart of user {}", event.get().itemId(), event.get().userId());
        else log.info("Item {} was not in the cart of user {}: already removed or expired", event.get().itemId(), event.get().userId());
    }

    /**
     * Releases a user's reserved rooms when they cancel a reservation that has
     * not yet been paid and completed. Not used for cancelling completed bookings.
     *
     * @param message the event as JSON, e.g. {"userId": "..."}
     */
    @AsyncListener(operation = @AsyncOperation(
            channelName = "reservation-cancelled",
            description = """
                    Sent when a user cancels a reservation before paying. Releases every room in the user's cart \
                    and deletes the cart. Not used for paid bookings: use booking-cancelled.

                    - Safe to receive twice: an already released or expired cart is ignored.
                    - Malformed messages, and messages without userId, are skipped. If Redis is down, Kafka retries.""",
            payloadType = ReservationCancelledEvent.class))
    @KafkaAsyncOperationBinding(groupId = "inventory-service-group",
            messageBinding = @KafkaAsyncMessageBinding(key = @KafkaAsyncKey(description = "The user id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")))
    @KafkaListener(topics = "reservation-cancelled", groupId = "inventory-service-group")
    public void onReservationCancelled(String message){
        Optional<ReservationCancelledEvent> event = parse(message, ReservationCancelledEvent.class, "reservation-cancelled");
        if (event.isEmpty() || event.get().userId() == null) return;

        int released = reservedCache.releaseCart(event.get().userId());
        log.info("Released {} reserved room(s) for user {}", released, event.get().userId());
    }

    /**
     * Converts a message to an event. Malformed messages, and messages without
     * a user id, are logged and skipped.
     *
     * @param message the event as JSON
     * @param type    the event type to convert to
     * @param topic   the topic the message came from, for logging
     * @return the event, or empty if the message is malformed
     */
    private <T> Optional<T> parse(String message, Class<T> type, String topic){
        try {
            return Optional.of(jsonMapper.readValue(message, type));
        } catch (JacksonException e){
            log.warn("Skipping malformed {} event: {}", topic, message);
            return Optional.empty();
        }
    }
}

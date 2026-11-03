package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.cache.CartItem;
import com.example.inventoryserviceproject.cache.ReservedCache;
import com.example.inventoryserviceproject.dto.event.BookingConfirmedEvent;
import com.example.inventoryserviceproject.dto.event.ReservationCancelledEvent;
import com.example.inventoryserviceproject.exception.ConflictException;
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
 * reserved rooms when a booking is confirmed, and releasing them when a
 * reservation is cancelled. Only the entry point for the events; the work is
 * done by {@link RoomService} (database) and {@link ReservedCache} (cache).
 *
 * <p>Messages are read as text and converted here, so each listener decides
 * its own event type. Malformed messages are skipped, as retrying them cannot
 * succeed. If Redis or the database cannot be reached, the error is thrown so
 * Kafka retries.
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
    @KafkaListener(topics = "reservation-confirmed", groupId = "inventory-service-group")
    public void onBookingConfirmed(String message){
        Optional<BookingConfirmedEvent> event = parse(message, BookingConfirmedEvent.class, "booking-confirmed");
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
     * Releases a user's reserved rooms when they cancel a reservation that has
     * not yet been paid and completed. Not used for cancelling completed bookings.
     *
     * @param message the event as JSON, e.g. {"userId": "..."}
     */
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

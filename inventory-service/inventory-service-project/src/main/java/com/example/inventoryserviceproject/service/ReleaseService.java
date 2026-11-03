package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.cache.ReservedCache;
import com.example.inventoryserviceproject.dto.event.ReservationCancelledEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Service releasing rooms reserved in the cache, based on events from the
 * booking-service.
 */
@Service
public class ReleaseService {
    private static final Logger log = LoggerFactory.getLogger(ReleaseService.class);

    private final ReservedCache reservedCache;
    private final JsonMapper jsonMapper;

    /**
     * Creates the service with the cache and JSON mapper it uses.
     *
     * @param reservedCache cache holding rooms in users' carts
     * @param jsonMapper    mapper used to read the events
     */
    public ReleaseService(ReservedCache reservedCache, JsonMapper jsonMapper){
        this.reservedCache = reservedCache;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Releases a user's reserved rooms when they cancel a reservation that has
     * not yet been paid and completed. Not used for cancelling completed bookings.
     *
     * <p>Messages are read as text and converted here, so each listener decides
     * its own event type. Malformed messages are skipped, as retrying them cannot
     * succeed. If Redis cannot be reached, the error is thrown so Kafka retries,
     * and if the release is never done, the holds still expire with the cart.
     *
     * @param message the event as JSON, e.g. {"userId": "..."}
     */
    @KafkaListener(topics = "reservation-cancelled", groupId = "inventory-service-group")
    public void onReservationCancelled(String message){
        ReservationCancelledEvent event;
        try {
            event = jsonMapper.readValue(message, ReservationCancelledEvent.class);
        } catch (JacksonException e){
            log.warn("Skipping malformed reservation-cancelled event: {}", message);
            return;
        }
        if (event.userId() == null){
            log.warn("Skipping reservation-cancelled event without userId: {}", message);
            return;
        }

        int released = reservedCache.releaseCart(event.userId());
        log.info("Released {} reserved room(s) for user {}", released, event.userId());
    }
}

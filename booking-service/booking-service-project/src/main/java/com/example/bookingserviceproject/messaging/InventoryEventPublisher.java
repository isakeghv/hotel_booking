package com.example.bookingserviceproject.messaging;

import com.example.bookingserviceproject.dto.event.ReservationCancelledEvent;
import com.example.bookingserviceproject.dto.event.ReservationItemRemoveEvent;
import com.example.bookingserviceproject.exception.EventPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Sends the events the inventory-service listens to. Events are sent as JSON
 * text, keyed by the user, so all events for one user's cart go to the same partition.
 */
@Component
public class InventoryEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(InventoryEventPublisher.class);
    private static final String RESERVATION_ITEM_REMOVE = "reservation-item-remove";
    private static final String RESERVATION_CANCELLED = "reservation-cancelled";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    /**
     * Creates the publisher with the Kafka client and JSON mapper it uses.
     *
     * @param kafkaTemplate client for sending to Kafka
     * @param jsonMapper    mapper used to convert events to JSON
     */
    public InventoryEventPublisher(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper){
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Tells inventory that the user removed a room from the cart, so it releases the room.
     * Waits until Kafka has stored the event.
     *
     * @param userId the id of the user
     * @param itemId the id of the removed room in the cart
     * @throws EventPublishException if the event could not be sent
     */
    public void reservationItemRemove(UUID userId, UUID itemId){
        send(RESERVATION_ITEM_REMOVE, userId.toString(), new ReservationItemRemoveEvent(userId, itemId));
    }

    /**
     * Tells inventory that the user emptied the cart, so it releases all rooms.
     * Does not wait for Kafka, and never throws: a failure is only logged, and
     * inventory releases the rooms itself when its cart expires.
     *
     * @param userId the id of the user
     */
    public void reservationCancelled(UUID userId){
        sendWithoutWaiting(RESERVATION_CANCELLED, userId.toString(), new ReservationCancelledEvent(userId));
    }

    /**
     * Sends an event and waits for Kafka to confirm it. How long it can take is
     * limited by the producer's timeouts in application.properties (about 5 seconds).
     *
     * @param topic the topic to send to
     * @param key   the key of the event
     * @param event the event, converted to JSON
     * @throws EventPublishException if the event could not be sent
     */
    private void send(String topic, String key, Object event){
        try {
            kafkaTemplate.send(topic, key, jsonMapper.writeValueAsString(event)).get();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new EventPublishException(topic, e);
        } catch (ExecutionException | RuntimeException e){
            // RuntimeException: send() itself fails when Kafka cannot be reached within max.block.ms
            throw new EventPublishException(topic, e);
        }
    }

    /**
     * Sends an event without waiting for Kafka to confirm it, for events the
     * response does not depend on. A failure is only logged. send() itself can
     * still block for up to max.block.ms (5 s) if Kafka cannot be reached.
     *
     * @param topic the topic to send to
     * @param key   the key of the event
     * @param event the event, converted to JSON
     */
    private void sendWithoutWaiting(String topic, String key, Object event){
        try {
            kafkaTemplate.send(topic, key, jsonMapper.writeValueAsString(event))
                    .whenComplete((result, e) -> {
                        if (e != null) log.error("Could not send event to {} with key {}", topic, key, e);
                    });
        } catch (RuntimeException e){
            log.error("Could not send event to {} with key {}", topic, key, e);
        }
    }
}

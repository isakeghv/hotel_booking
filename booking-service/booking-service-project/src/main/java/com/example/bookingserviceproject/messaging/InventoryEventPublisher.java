package com.example.bookingserviceproject.messaging;

import com.example.bookingserviceproject.dto.event.ReservationCancelledEvent;
import com.example.bookingserviceproject.dto.event.ReservationConfirmedEvent;
import com.example.bookingserviceproject.dto.event.ReservationItemRemoveEvent;
import com.example.bookingserviceproject.exception.EventPublishException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Sends the events the inventory-service listens to. Events are keyed by the
 * user, so all events for one user's cart go to the same partition.
 */
@Component
public class InventoryEventPublisher {
    private static final String RESERVATION_ITEM_REMOVE = "reservation-item-remove";
    private static final String RESERVATION_CANCELLED = "reservation-cancelled";
    private static final String RESERVATION_CONFIRMED = "reservation-confirmed";

    private final EventSender eventSender;

    /**
     * Creates the publisher with the sender it uses.
     *
     * @param eventSender sends events to Kafka as JSON
     */
    public InventoryEventPublisher(EventSender eventSender){
        this.eventSender = eventSender;
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
        eventSender.sendAndWait(RESERVATION_ITEM_REMOVE, userId.toString(), new ReservationItemRemoveEvent(userId, itemId));
    }

    /**
     * Tells inventory that the user emptied the cart, so it releases all rooms.
     * Does not wait for Kafka, and never throws: a failure is only logged, and
     * inventory releases the rooms itself when its cart expires.
     *
     * @param userId the id of the user
     */
    public void reservationCancelled(UUID userId){
        eventSender.sendWithoutWaiting(RESERVATION_CANCELLED, userId.toString(), new ReservationCancelledEvent(userId));
    }

    /**
     * Tells inventory that the user's booking is paid and saved, so it stores
     * the rooms in its cart as booked. Waits until Kafka has stored the event.
     *
     * @param userId the id of the user
     * @throws EventPublishException if the event could not be sent
     */
    public void reservationConfirmed(UUID userId){
        eventSender.sendAndWait(RESERVATION_CONFIRMED, userId.toString(), new ReservationConfirmedEvent(userId));
    }
}

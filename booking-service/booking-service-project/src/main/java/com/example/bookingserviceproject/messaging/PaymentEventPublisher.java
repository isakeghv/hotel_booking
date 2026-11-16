package com.example.bookingserviceproject.messaging;

import com.example.bookingserviceproject.dto.event.AddonCancelledPaymentEvent;
import com.example.bookingserviceproject.dto.event.BookingCancelledPaymentEvent;
import com.example.bookingserviceproject.dto.event.ItemCancelledPaymentEvent;
import com.example.bookingserviceproject.exception.EventPublishException;
import org.springframework.stereotype.Component;

/**
 * Sends the events the payment-service listens to. Events are keyed by the
 * booking, so all events for one booking go to the same partition.
 */
@Component
public class PaymentEventPublisher {
    private static final String PAYMENT_BOOKING_CANCELLED = "payment-booking-cancelled";
    private static final String PAYMENT_ITEM_CANCELLED = "payment-item-cancelled";
    private static final String PAYMENT_ADDON_CANCELLED = "payment-addon-cancelled";

    private final EventSender eventSender;

    /**
     * Creates the publisher with the sender it uses.
     *
     * @param eventSender sends events to Kafka as JSON
     */
    public PaymentEventPublisher(EventSender eventSender){
        this.eventSender = eventSender;
    }

    /**
     * Tells the payment-service that a booking was cancelled, so it decides
     * whether to refund it. Waits until Kafka has stored the event: a
     * cancellation must never become final without the payment-service being told.
     *
     * @param event the cancellation
     * @throws EventPublishException if the event could not be sent
     */
    public void bookingCancelled(BookingCancelledPaymentEvent event){
        eventSender.sendAndWait(PAYMENT_BOOKING_CANCELLED, event.bookingId().toString(), event);
    }

    /**
     * Tells the payment-service that one room (with its extras) was cancelled,
     * so it decides whether to refund it. Waits until Kafka has stored the event:
     * a cancellation must never become final without the payment-service being told.
     *
     * @param event the cancellation
     * @throws EventPublishException if the event could not be sent
     */
    public void itemCancelled(ItemCancelledPaymentEvent event){
        eventSender.sendAndWait(PAYMENT_ITEM_CANCELLED, event.bookingId().toString(), event);
    }

    /**
     * Tells the payment-service that one extra on a room was cancelled, so it
     * decides whether to refund it. Waits until Kafka has stored the event: a
     * cancellation must never become final without the payment-service being told.
     *
     * @param event the cancellation
     * @throws EventPublishException if the event could not be sent
     */
    public void addonCancelled(AddonCancelledPaymentEvent event){
        eventSender.sendAndWait(PAYMENT_ADDON_CANCELLED, event.bookingId().toString(), event);
    }
}

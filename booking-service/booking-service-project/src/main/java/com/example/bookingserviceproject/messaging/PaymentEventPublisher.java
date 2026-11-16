package com.example.bookingserviceproject.messaging;

import com.example.bookingserviceproject.dto.event.BookingCancelledPaymentEvent;
import com.example.bookingserviceproject.exception.EventPublishException;
import org.springframework.stereotype.Component;

/**
 * Sends the events the payment-service listens to. Events are keyed by the
 * booking, so all events for one booking go to the same partition.
 */
@Component
public class PaymentEventPublisher {
    private static final String BOOKING_CANCELLED_PAYMENT = "booking-cancelled-payment";

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
        eventSender.sendAndWait(BOOKING_CANCELLED_PAYMENT, event.bookingId().toString(), event);
    }
}

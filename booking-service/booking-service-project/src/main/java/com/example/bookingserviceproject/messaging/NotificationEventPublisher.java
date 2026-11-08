package com.example.bookingserviceproject.messaging;

import com.example.bookingserviceproject.dto.event.BookingConfirmedNotification;
import org.springframework.stereotype.Component;

/**
 * Sends the events the notification-service listens to.
 */
@Component
public class NotificationEventPublisher {
    private static final String BOOKING_CONFIRMED = "booking-confirmed";

    private final EventSender eventSender;

    /**
     * Creates the publisher with the sender it uses.
     *
     * @param eventSender sends events to Kafka as JSON
     */
    public NotificationEventPublisher(EventSender eventSender){
        this.eventSender = eventSender;
    }

    /**
     * Tells the notification-service that a booking is paid and saved, so it
     * sends the confirmation email. Does not wait for Kafka, and never throws:
     * the email must never undo a booking, so a failure is only logged.
     *
     * @param notification what the email needs
     */
    public void bookingConfirmed(BookingConfirmedNotification notification){
        eventSender.sendWithoutWaiting(BOOKING_CONFIRMED, notification.userId().toString(), notification);
    }
}

package com.example.bookingserviceproject.dto.event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sent on {@code booking-cancelled-payment} when a paid booking is cancelled,
 * so the payment-service decides whether to refund it (the 48 h rule, the same
 * for everyone). See payment-service-notes.md.
 *
 * @param bookingId            the cancelled booking, the payment's key
 * @param userId               the user who booked
 * @param startTime            when the booking starts (local hotel time), for the 48 h rule
 * @param cancelledAt          when the booking-service sent this (UTC). The rule is applied at this time,
 *                             so a slow delivery never costs the user a refund
 * @param ignoreRefundDeadline true to refund regardless of the 48 h rule, as long as the booking has not
 *                             started. Always false when the customer cancels
 */
public record BookingCancelledPaymentEvent(
        UUID bookingId,
        UUID userId,
        LocalDateTime startTime,
        Instant cancelledAt,
        boolean ignoreRefundDeadline
) {}

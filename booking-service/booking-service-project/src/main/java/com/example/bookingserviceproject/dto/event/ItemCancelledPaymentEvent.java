package com.example.bookingserviceproject.dto.event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Sent on {@code payment-item-cancelled} when one room of a paid booking is
 * cancelled, so the payment-service decides whether to refund it (the 48 h
 * rule, the same for everyone). One event covers the room and its extras.
 * See payment-service-notes.md.
 *
 * @param bookingId            the booking the room belongs to, to find the payment
 * @param itemId               the cancelled room. The payment-service's key: a room can only be cancelled once
 * @param userId               the user who booked
 * @param amount               what to refund at most: the room's price plus its extras that were still active
 * @param startTime            when the booking starts (local hotel time), for the 48 h rule
 * @param cancelledAt          when the booking-service sent this (UTC). The rule is applied at this time,
 *                             so a slow delivery never costs the user a refund
 * @param ignoreRefundDeadline true to refund regardless of the 48 h rule, as long as the booking has not
 *                             started. Always false when the customer cancels
 */
public record ItemCancelledPaymentEvent(
        UUID bookingId,
        UUID itemId,
        UUID userId,
        int amount,
        LocalDateTime startTime,
        Instant cancelledAt,
        boolean ignoreRefundDeadline
) {}

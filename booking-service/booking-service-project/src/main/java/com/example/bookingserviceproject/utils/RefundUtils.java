package com.example.bookingserviceproject.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * The refund rule, as the booking-service needs it to show prices. Refunds
 * themselves are decided and carried out by the payment-service: this must
 * match its rule.
 */
public final class RefundUtils {
    // The deadline: something cancelled this long or longer before the booking starts is refunded. Must match the payment-service
    public static final Duration REFUND_DEADLINE = Duration.ofHours(48);

    private RefundUtils(){}

    /**
     * Whether a cancellation was early enough to be refunded. The cancellation
     * time is UTC, the start local hotel time, so the cancellation time is
     * converted with the server's time zone.
     *
     * @param cancelledAt when it was cancelled (UTC)
     * @param startTime   when the booking starts (local hotel time)
     * @return true if it was cancelled {@link #REFUND_DEADLINE} or more before the start (exactly at the deadline included)
     */
    public static boolean isRefunded(Instant cancelledAt, LocalDateTime startTime){
        LocalDateTime cancelled = LocalDateTime.ofInstant(cancelledAt, ZoneId.systemDefault());
        return !cancelled.isAfter(startTime.minus(REFUND_DEADLINE));
    }
}

package com.example.bookingserviceproject.dto.event;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Sent on {@code booking-confirmed} when a booking is paid and saved, so the
 * notification-service sends the confirmation email. Only what the user needs
 * for an overview of the booking: no room numbers and no inventory ids.
 *
 * @param userId    used by the notification-service to get the user's email from the user-service
 * @param bookingId only used to make sure each email is sent once. Not shown to the user
 * @param bookedAt  when the booking was made (UTC)
 * @param from      the earliest check-in of the booking's rooms (local hotel time)
 * @param to        the latest check-out of the booking's rooms (local hotel time)
 * @param currency  the currency of all prices
 * @param rooms     the booked rooms
 * @param total     the price of the whole booking
 */
public record BookingConfirmedNotification(
        UUID userId,
        UUID bookingId,
        Instant bookedAt,
        LocalDateTime from,
        LocalDateTime to,
        String currency,
        List<Room> rooms,
        int total
) {
    /**
     * One booked room.
     *
     * @param roomType the name of the room type
     * @param variant  the name of the variant
     * @param from     check-in date and time
     * @param to       check-out date and time
     * @param nights   the number of nights
     * @param extras   the extras on the room
     * @param total    the price of the room including its extras, for its nights
     */
    public record Room(
            String roomType,
            String variant,
            LocalDateTime from,
            LocalDateTime to,
            int nights,
            List<Extra> extras,
            int total
    ) {}

    /**
     * One extra on a room. Its price is included in the room's total.
     *
     * @param name   the name of the extra
     * @param amount how many of the extra
     */
    public record Extra(String name, int amount) {}
}

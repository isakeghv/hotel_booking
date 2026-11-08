package com.example.bookingserviceproject.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A confirmed booking. Prices are given the same way as in the confirmation
 * email: each room's total includes its extras, and extras have no price of
 * their own. Inventory's room type and variant ids are left out.
 *
 * @param bookingId the id of the booking
 * @param createdAt when the booking was made (UTC)
 * @param from      the earliest check-in of the booking's rooms
 * @param to        the latest check-out of the booking's rooms
 * @param currency  the currency of all prices
 * @param total     the price of the whole booking
 * @param items     the booked rooms
 */
public record BookingResponse(
        UUID bookingId,
        Instant createdAt,
        LocalDateTime from,
        LocalDateTime to,
        String currency,
        int total,
        List<Item> items
) {
    /**
     * One booked room.
     *
     * @param itemId     the id of the booked room
     * @param roomNumber the room number
     * @param roomType   the name of the room type
     * @param variant    the name of the variant
     * @param from       check-in date and time
     * @param to         check-out date and time
     * @param nights     the number of nights
     * @param extras     the extras on the room. Always present, empty if the room has none
     * @param total      the price of the room including its extras, for its nights
     */
    public record Item(
            UUID itemId,
            int roomNumber,
            String roomType,
            String variant,
            LocalDateTime from,
            LocalDateTime to,
            int nights,
            List<Extra> extras,
            int total
    ) {}

    /**
     * One extra on a booked room. Its price is included in the room's total.
     *
     * @param name   the name of the extra
     * @param amount how many of the extra
     */
    public record Extra(String name, int amount) {}
}

package com.example.bookingserviceproject.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A booking, as the customer sees it. Left out on purpose: the room number
 * (for staff only), who cancelled and when, and inventory's variant and extra ids.
 *
 * @param bookingId the id of the booking
 * @param from      the earliest check-in of the booking's rooms
 * @param nights    the number of nights, from the earliest check-in to the latest check-out
 * @param currency  the currency of all prices
 * @param total     the price of the whole booking, as booked (also after a cancellation)
 * @param cancelled true if the whole booking is cancelled
 * @param items     the booked rooms
 */
public record BookingResponse(
        UUID bookingId,
        LocalDateTime from,
        int nights,
        String currency,
        int total,
        boolean cancelled,
        List<Item> items
) {
    /**
     * One booked room.
     *
     * @param itemId     the id of the booked room
     * @param roomTypeId the id of the room type in the inventory-service
     * @param roomType   the name of the room type
     * @param variant    the name of the variant
     * @param from       check-in date and time
     * @param nights     the number of nights
     * @param price      the price of the room alone, for its nights
     * @param cancelled  true if this room is cancelled
     * @param image      the room type's primary image. Always present
     * @param extras     the extras on the room. Always present, empty if the room has none
     */
    public record Item(
            UUID itemId,
            UUID roomTypeId,
            String roomType,
            String variant,
            LocalDateTime from,
            int nights,
            int price,
            boolean cancelled,
            ImageResponse image,
            List<Extra> extras
    ) {}

    /**
     * One extra on a booked room.
     *
     * @param extraId   the id of the extra in the inventory-service. A room has each extra at most once,
     *                  so the room's itemId and this id identify the extra, e.g. to cancel it
     * @param name      the name of the extra
     * @param amount    how many of the extra
     * @param price     the price of this extra for the room, all of them together
     * @param cancelled true if this extra is cancelled
     */
    public record Extra(UUID extraId, String name, int amount, int price, boolean cancelled) {}
}

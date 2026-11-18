package com.example.bookingserviceproject.dto.response;

import com.example.bookingserviceproject.model.CancelledBy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A booking as staff see it, with every room, its variant, extras, prices and
 * cancellations. Prices are whole units of {@code currency}.
 *
 * @param bookingId the id of the booking
 * @param bookedAt  when the booking was made (UTC)
 * @param user      the guest who booked. Always present
 * @param from      the earliest check-in of the booking's rooms
 * @param to        the latest check-out of the booking's rooms
 * @param nights    the number of nights, from {@code from} to {@code to}
 * @param currency  the currency of the prices
 * @param total     the sum of the rooms' totals. Not changed by cancelling the whole booking
 * @param cancelled true if the booking is cancelled as a whole
 * @param items     the rooms, by check-in, then room number
 */
public record StaffBookingResponse(
        UUID bookingId,
        Instant bookedAt,
        UserResponse user,
        LocalDateTime from,
        LocalDateTime to,
        int nights,
        String currency,
        int total,
        boolean cancelled,
        List<Item> items
) {
    /**
     * One booked room.
     *
     * @param itemId      the id of the room in the booking
     * @param roomNumber  the room number. Staff only
     * @param roomTypeId  the id of the room type in inventory
     * @param roomType    the room type's name, as booked
     * @param image       the room type's primary image. Always present
     * @param variant     the variant, as booked, with what it includes
     * @param from        the room's check-in
     * @param to          the room's check-out
     * @param nights      the room's number of nights
     * @param price       the room alone, as booked
     * @param total       the room plus its extras, without what was refunded
     * @param cancelled   true if the room is cancelled, on its own or with the booking
     * @param cancelledBy who cancelled the room on its own, or null
     * @param checkedIn   true if the guest has checked in
     * @param checkedOut  true if the guest has checked out
     * @param extras      the room's extras, by name
     */
    public record Item(
            UUID itemId,
            int roomNumber,
            UUID roomTypeId,
            String roomType,
            ImageResponse image,
            Variant variant,
            LocalDateTime from,
            LocalDateTime to,
            int nights,
            int price,
            int total,
            boolean cancelled,
            CancelledBy cancelledBy,
            boolean checkedIn,
            boolean checkedOut,
            List<Extra> extras
    ) {}

    /**
     * The variant of a booked room.
     *
     * @param variantId   the id of the variant in inventory
     * @param variantName the variant's name, as booked
     * @param includes    the extras included with the variant, from inventory. Empty if none, or if inventory could not be asked
     */
    public record Variant(
            UUID variantId,
            String variantName,
            List<Include> includes
    ) {}

    /**
     * An extra included with a variant.
     *
     * @param extraId the id of the extra in inventory
     * @param name    the name of the extra
     * @param amount  how many are included
     */
    public record Include(
            UUID extraId,
            String name,
            int amount
    ) {}

    /**
     * An extra on a booked room.
     *
     * @param extraId     the id of the extra in inventory
     * @param name        the name of the extra
     * @param amount      how many
     * @param price       the extra's price for the room's stay, as booked
     * @param cancelled   true if the extra is cancelled, on its own or with its room or the booking
     * @param cancelledBy who cancelled the extra on its own, or its room, or null
     */
    public record Extra(
            UUID extraId,
            String name,
            int amount,
            int price,
            boolean cancelled,
            CancelledBy cancelledBy
    ) {}
}

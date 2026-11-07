package com.example.bookingserviceproject.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * The user's cart. Only contains what the user needs to see and act on: room
 * numbers, the bookingId and the inventory ids of room types and variants are
 * left out. The itemId is included, as it is used to remove a room and to add
 * extras to it.
 *
 * @param expiresAt when the cart expires (UTC), or null if the cart is empty
 * @param currency  the currency of all prices
 * @param items     the rooms in the cart
 * @param total     the price of everything in the cart, in whole units of the currency
 */
public record CartResponse(
        Instant expiresAt,
        String currency,
        List<Item> items,
        int total
) {
    /**
     * One room in the cart.
     *
     * @param itemId   the id of the room in the cart
     * @param roomType the name of the room type
     * @param variant  the name of the chosen variant
     * @param from     check-in date and time
     * @param to       check-out date and time
     * @param nights   the number of nights
     * @param total    the price of the room for the whole stay, without extras
     * @param extras   the extras added to this room. Always present, empty if the room has none
     */
    public record Item(
            UUID itemId,
            String roomType,
            String variant,
            LocalDateTime from,
            LocalDateTime to,
            int nights,
            int total,
            List<Extra> extras
    ) {}

    /**
     * One extra added to a room in the cart.
     *
     * @param extraId the id of the extra, used to add more of it or remove it
     * @param name    the name of the extra
     * @param amount  how many of the extra are added
     * @param total   the price of the extra for the whole stay
     */
    public record Extra(
            UUID extraId,
            String name,
            int amount,
            int total
    ) {}
}

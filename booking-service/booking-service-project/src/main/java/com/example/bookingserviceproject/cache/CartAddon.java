package com.example.bookingserviceproject.cache;

import java.util.UUID;

/**
 * One extra on a room in the cart, stored as JSON in the booking cache under
 * {@code addon:{itemId}:{extraId}}. An extra belongs to one room.
 *
 * @param itemId     the id of the room the extra belongs to
 * @param extraId    the id of the extra in the inventory-service
 * @param name       the name of the extra (copied, so the cart can be shown without asking inventory)
 * @param amount     how many of the extra
 * @param unitPrice  the price of one unit, per stay if fixed, otherwise per night
 * @param priceFixed true if the price is per stay, false if per night
 * @param nights     the room's number of nights, used when the amount is changed
 * @param total      the price of the extra for the whole stay
 */
public record CartAddon(
        UUID itemId,
        UUID extraId,
        String name,
        int amount,
        int unitPrice,
        boolean priceFixed,
        int nights,
        int total
) {}

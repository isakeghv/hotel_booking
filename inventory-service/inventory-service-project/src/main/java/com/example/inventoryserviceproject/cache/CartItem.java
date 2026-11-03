package com.example.inventoryserviceproject.cache;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A room held in a user's cart in the cache. Contains everything needed to
 * later store it as an occupied room in the database, including the prices
 * quoted to the user, so price changes during the hold do not affect it.
 *
 * @param itemId            the unique id of the cart item
 * @param roomId            the id of the held room
 * @param roomNumber        the room number of the held room
 * @param roomTypeId        the id of the room type
 * @param variantId         the id of the chosen variant
 * @param from              check-in date and time
 * @param to                check-out date and time
 * @param roomPricePerNight the room type's price per night, in USD
 * @param variantPrice      the variant's price, in USD
 * @param variantPriceFixed true if the variant price is per stay, false if per night
 * @param total             the total price for this room, in USD
 */
public record CartItem(
        UUID itemId,
        UUID roomId,
        int roomNumber,
        UUID roomTypeId,
        UUID variantId,
        LocalDateTime from,
        LocalDateTime to,
        int roomPricePerNight,
        int variantPrice,
        boolean variantPriceFixed,
        int total
) {}

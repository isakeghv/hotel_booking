package com.example.bookingserviceproject.cache;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A room in a user's cart in the cache. Contains everything needed to later
 * save it as a {@code booking_items} row, including the names and the price
 * quoted by the inventory-service, so no further calls to inventory are needed.
 *
 * @param itemId         the id of the room in the cart, the same as inventory's itemId
 * @param roomNumber     the room number
 * @param roomTypeId     the id of the room type
 * @param roomTypeName   the name of the room type
 * @param variantId      the id of the chosen variant
 * @param variantName    the name of the chosen variant
 * @param from           check-in date and time
 * @param to             check-out date and time
 * @param nights         the number of nights
 * @param total          the price of the room for the whole stay, in USD
 */
public record CartItem(
        UUID itemId,
        int roomNumber,
        UUID roomTypeId,
        String roomTypeName,
        UUID variantId,
        String variantName,
        LocalDateTime from,
        LocalDateTime to,
        int nights,
        int total
) {}

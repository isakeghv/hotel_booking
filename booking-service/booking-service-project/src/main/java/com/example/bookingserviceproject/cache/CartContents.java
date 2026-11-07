package com.example.bookingserviceproject.cache;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Everything stored in a user's cart in the cache.
 *
 * @param bookingId the id the booking gets when it is confirmed, created with the cart
 * @param expiresAt when the cart and everything in it expires
 * @param items     the rooms in the cart
 * @param completed true while the booking is being completed, so the cart cannot be changed
 */
public record CartContents(UUID bookingId, Instant expiresAt, List<CartItem> items, boolean completed) {

    /**
     * @return the contents of a cart that does not exist or has expired
     */
    public static CartContents empty(){
        return new CartContents(null, null, List.of(), false);
    }

    /**
     * @return true if the cart has no rooms
     */
    public boolean isEmpty(){
        return items.isEmpty();
    }

    /**
     * @param itemId the id of a room in the cart
     * @return true if the room is in the cart
     */
    public boolean contains(UUID itemId){
        return items.stream().anyMatch(item -> item.itemId().equals(itemId));
    }
}

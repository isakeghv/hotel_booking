package com.example.bookingserviceproject.dto.event;

import java.util.UUID;

/**
 * Sent on {@code reservation-item-remove} when the user removes one room from
 * the cart. Inventory releases the room, and deletes its cart if it was the
 * last one. The field names must match inventory's event record.
 *
 * @param userId the id of the user whose cart the room is removed from
 * @param itemId the id of the removed room in the cart
 */
public record ReservationItemRemoveEvent(UUID userId, UUID itemId) {}

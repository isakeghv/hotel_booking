package com.example.inventoryserviceproject.dto.event;

import java.util.UUID;

/**
 * Sent by the booking-service on the "reservation-item-remove" topic when a
 * user removes one room from their cart before completing the booking.
 *
 * @param userId the id of the user whose cart the room is removed from
 * @param itemId the id of the cart item, as returned when the room was reserved
 */
public record ReservationItemRemovedEvent(UUID userId, UUID itemId) {}

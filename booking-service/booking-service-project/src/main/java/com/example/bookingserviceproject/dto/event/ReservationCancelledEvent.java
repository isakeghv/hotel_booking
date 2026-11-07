package com.example.bookingserviceproject.dto.event;

import java.util.UUID;

/**
 * Sent on {@code reservation-cancelled} when the user empties the cart before
 * paying. Inventory releases all rooms and deletes its cart. The field names
 * must match inventory's event record.
 *
 * @param userId the id of the user whose cart was emptied
 */
public record ReservationCancelledEvent(UUID userId) {}

package com.example.inventoryserviceproject.dto.event;

import java.util.UUID;

/**
 * Sent by the booking-service on the "reservation-cancelled" topic when a user
 * cancels a reservation that has not yet been paid and completed.
 *
 * @param userId the id of the user whose reserved rooms are released
 */
public record ReservationCancelledEvent(UUID userId) {}

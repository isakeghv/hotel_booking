package com.example.inventoryserviceproject.dto.event;

import java.util.UUID;

/**
 * Sent by the booking-service on the "reservation-confirmed" topic when a user has
 * paid and completed a booking.
 *
 * @param userId the id of the user whose reserved rooms are stored as booked
 */
public record BookingConfirmedEvent(UUID userId) {}

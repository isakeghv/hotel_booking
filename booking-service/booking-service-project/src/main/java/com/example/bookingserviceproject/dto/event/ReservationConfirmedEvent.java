package com.example.bookingserviceproject.dto.event;

import java.util.UUID;

/**
 * Sent on {@code reservation-confirmed} when the user's booking is paid and
 * saved. Inventory stores every room in the user's cart as booked, and deletes
 * its cart. The field names must match inventory's event record.
 *
 * @param userId the id of the user whose booking was confirmed
 */
public record ReservationConfirmedEvent(UUID userId) {}

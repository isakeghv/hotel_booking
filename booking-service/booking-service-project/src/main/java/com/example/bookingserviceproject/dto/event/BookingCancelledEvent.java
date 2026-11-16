package com.example.bookingserviceproject.dto.event;

import java.util.List;
import java.util.UUID;

/**
 * Sent on {@code booking-cancelled} when a paid booking is cancelled. Inventory
 * deletes the booked periods of these rooms, so they are free again. The field
 * names must match inventory's event record.
 *
 * @param itemIds the ids of the booking's rooms, the same ids inventory returned when they were reserved
 */
public record BookingCancelledEvent(List<UUID> itemIds) {}

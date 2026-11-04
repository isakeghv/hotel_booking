package com.example.inventoryserviceproject.dto.event;

import java.util.List;
import java.util.UUID;

/**
 * Sent by the booking-service on the "booking-cancelled" topic when a completed
 * (paid) booking is cancelled.
 *
 * @param itemIds the item ids of all rooms in the booking, the same ids as returned when the rooms were reserved
 */
public record BookingCancelledEvent(List<UUID> itemIds) {}

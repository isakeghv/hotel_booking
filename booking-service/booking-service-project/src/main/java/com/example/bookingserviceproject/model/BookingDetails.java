package com.example.bookingserviceproject.model;

import java.util.List;

/**
 * A booking with its rooms, extras and cancellations, as saved or loaded together.
 *
 * @param booking         the booking
 * @param items           its rooms
 * @param addons          the extras on its rooms
 * @param cancelledItems  its cancelled rooms
 * @param cancelledAddons its cancelled extras
 */
public record BookingDetails(
        Booking booking,
        List<BookingItem> items,
        List<BookingAddon> addons,
        List<CancelledItem> cancelledItems,
        List<CancelledAddon> cancelledAddons
) {}

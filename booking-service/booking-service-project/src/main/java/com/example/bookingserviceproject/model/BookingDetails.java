package com.example.bookingserviceproject.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
) {
    /**
     * The price of what is still active: rooms that are not cancelled, and the
     * extras on them that are not cancelled. Equals the booking's total price
     * when nothing is cancelled.
     *
     * @return the price of the active rooms and extras
     */
    public int activeAmount(){
        Set<UUID> cancelledItemIds = cancelledItems.stream()
                .map(cancelled -> cancelled.getItem().getItemId())
                .collect(Collectors.toSet());
        Set<UUID> cancelledAddonIds = cancelledAddons.stream()
                .map(cancelled -> cancelled.getAddon().getAddonId())
                .collect(Collectors.toSet());

        int rooms = items.stream()
                .filter(item -> !cancelledItemIds.contains(item.getItemId()))
                .mapToInt(BookingItem::getPrice)
                .sum();
        // An extra on a cancelled room is cancelled with it
        int extras = addons.stream()
                .filter(addon -> !cancelledItemIds.contains(addon.getItem().getItemId())
                        && !cancelledAddonIds.contains(addon.getAddonId()))
                .mapToInt(BookingAddon::getPrice)
                .sum();
        return rooms + extras;
    }
}

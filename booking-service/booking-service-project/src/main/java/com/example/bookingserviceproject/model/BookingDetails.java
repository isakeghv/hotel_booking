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
        return activeItems().stream().mapToInt(this::activeAmountOf).sum();
    }

    /**
     * @return the rooms that are not cancelled on their own
     */
    public List<BookingItem> activeItems(){
        Set<UUID> cancelledItemIds = cancelledItemIds();
        return items.stream()
                .filter(item -> !cancelledItemIds.contains(item.getItemId()))
                .toList();
    }

    /**
     * @param itemId the id of a room of this booking
     * @return true if the room is cancelled on its own
     */
    public boolean isItemCancelled(UUID itemId){
        return cancelledItemIds().contains(itemId);
    }

    /**
     * The price of one room and its extras that are not cancelled on their own.
     * Extras on the room count as cancelled when the room is, so this is what
     * cancelling the room takes away.
     *
     * @param item a room of this booking
     * @return the room's price plus the prices of its active extras
     */
    public int activeAmountOf(BookingItem item){
        Set<UUID> cancelledAddonIds = cancelledAddons.stream()
                .map(cancelled -> cancelled.getAddon().getAddonId())
                .collect(Collectors.toSet());
        int extras = addons.stream()
                .filter(addon -> addon.getItem().getItemId().equals(item.getItemId())
                        && !cancelledAddonIds.contains(addon.getAddonId()))
                .mapToInt(BookingAddon::getPrice)
                .sum();
        return item.getPrice() + extras;
    }

    /**
     * @return the ids of the rooms that are cancelled on their own
     */
    private Set<UUID> cancelledItemIds(){
        return cancelledItems.stream()
                .map(cancelled -> cancelled.getItem().getItemId())
                .collect(Collectors.toSet());
    }
}

package com.example.bookingserviceproject.dto.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * The rooms the inventory-service held in the user's cart for one request,
 * as returned by {@code GET /inventory/availability/variants/{variantId}}.
 * Only the fields the booking-service uses are read.
 *
 * @param expiresAt when inventory's cart expires (fixed, 30 minutes after its first room)
 * @param from      check-in date and time
 * @param to        check-out date and time
 * @param nights    the number of nights
 * @param roomType  the room type of the held rooms
 * @param variant   the chosen variant
 * @param rooms     the held rooms, each with its own itemId
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryReservation(
        Instant expiresAt,
        LocalDateTime from,
        LocalDateTime to,
        int nights,
        RoomType roomType,
        Variant variant,
        List<Room> rooms
) {
    /**
     * The room type of the held rooms.
     *
     * @param roomTypeId the id of the room type
     * @param name       the name of the room type
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoomType(UUID roomTypeId, String name) {}

    /**
     * The chosen variant.
     *
     * @param variantId the id of the variant
     * @param name      the name of the variant
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Variant(UUID variantId, String name) {}

    /**
     * One held room.
     *
     * @param itemId     the id of the room in the cart, also used when the booking is confirmed or cancelled
     * @param roomNumber the room number
     * @param total      the price of the room for the whole stay, in USD
     */
    public record Room(UUID itemId, int roomNumber, int total) {}

    /**
     * The body of inventory's response: {@code {"reservation": {...}}}.
     *
     * @param reservation the held rooms
     */
    public record Result(InventoryReservation reservation) {}
}

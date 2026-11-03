package com.example.inventoryserviceproject.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        String currency,
        Instant expiresAt,
        LocalDateTime from,
        LocalDateTime to,
        int nights,
        RoomTypeInfo roomType,
        VariantInfo variant,
        List<RoomInfo> rooms,
        int total
) {
    /**
     * The room type of the reserved rooms, with its price per night.
     */
    public record RoomTypeInfo(UUID roomTypeId, String name, int pricePerNight) {}

    /**
     * The chosen variant, with its price per stay if fixed, otherwise per night.
     */
    public record VariantInfo(UUID variantId, String name, int price, boolean priceFixed) {}

    /**
     * A reserved room, with its id in the cart and its total price for the stay.
     */
    public record RoomInfo(UUID itemId, int roomNumber, int total) {}
}

package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "The rooms held in the cart by one request, with prices")
public record AvailabilityResponse(
        @Schema(description = "Currency of all prices", example = "USD")
        String currency,
        @Schema(description = "When the cart and everything in it expires (UTC). Fixed: adding rooms does not extend it")
        Instant expiresAt,
        @Schema(description = "Check-in date and time", example = "2026-11-05T15:00:00")
        LocalDateTime from,
        @Schema(description = "Check-out date and time", example = "2026-11-08T12:00:00")
        LocalDateTime to,
        @Schema(description = "Number of nights", example = "3")
        int nights,
        RoomTypeInfo roomType,
        VariantInfo variant,
        @Schema(description = "The rooms held by this request. Store each itemId")
        List<RoomInfo> rooms,
        @Schema(description = "Price of all rooms held by this request, in USD", example = "1040")
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

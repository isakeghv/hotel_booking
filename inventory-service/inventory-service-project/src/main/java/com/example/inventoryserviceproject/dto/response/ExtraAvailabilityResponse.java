package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "An extra priced for the stay in the cart. Nothing is stored")
public record ExtraAvailabilityResponse(
        @Schema(description = "Currency of all prices", example = "USD")
        String currency,
        @Schema(description = "When the cart expires (UTC). The price is based on the stay in this cart")
        Instant expiresAt,
        ExtraInfo extra,
        @Schema(description = "How many of the extra", example = "2")
        int amount,
        @Schema(description = "The stay the price is based on: earliest check-in to latest check-out in the cart")
        StayInfo stay,
        @Schema(description = "Price of the extra for the whole stay, in USD", example = "90")
        int total
) {
    /**
     * The extra, with its price per unit. Per stay if fixed, otherwise per night.
     */
    public record ExtraInfo(UUID extraId, String name, int unitPrice, boolean priceFixed) {}

    /**
     * The stay the price is based on, from the earliest check-in to the latest check-out in the cart.
     */
    public record StayInfo(LocalDateTime from, LocalDateTime to, int nights) {}
}

package com.example.inventoryserviceproject.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExtraAvailabilityResponse(
        String currency,
        Instant expiresAt,
        ExtraInfo extra,
        int amount,
        StayInfo stay,
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

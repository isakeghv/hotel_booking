package com.example.inventoryserviceproject.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * The active variants of a room type, priced for a stay. Only contains what is
 * specific to each variant; the room type itself is returned by its own endpoint.
 *
 * @param variants the variants, lowest total first
 * @param nights   the number of nights in the stay
 * @param rooms    the number of rooms the prices are for
 * @param currency the currency of all prices, e.g. "USD"
 */
public record RoomTypeVariantsResponse(
        List<VariantInfo> variants,
        int nights,
        int rooms,
        String currency
) {
    /**
     * A variant with its prices, including the room type's price.
     *
     * @param variantId     the id of the variant
     * @param name          the name of the variant, e.g. "With breakfast"
     * @param description   the variant's main description, empty (null title, no lines) if it has none
     * @param includes      the extras included with the variant
     * @param pricePerNight the average price per night for all the rooms ({@code total} / nights, rounded up),
     *                      so it adds up to the total also when the variant has a price per stay
     * @param total         the price for all the rooms for the whole stay, everything included
     */
    public record VariantInfo(
            UUID variantId,
            String name,
            DescriptionResponse description,
            List<IncludedExtra> includes,
            int pricePerNight,
            int total
    ) {}

    /**
     * An extra included with a variant, e.g. "Breakfast" or "2 x Parking".
     *
     * @param name   the name of the extra
     * @param amount the amount included, or null if the amount is not shown
     */
    public record IncludedExtra(String name, Integer amount) {}
}

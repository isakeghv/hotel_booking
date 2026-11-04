package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "The variants of a room type, priced for a stay and number of rooms")
public record RoomTypeVariantsResponse(
        @Schema(description = "The variants, cheapest total first")
        List<VariantInfo> variants,
        @Schema(description = "Number of nights the prices are for", example = "3")
        int nights,
        @Schema(description = "Number of rooms the prices are for", example = "2")
        int rooms,
        @Schema(description = "Currency of all prices", example = "USD")
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
            @Schema(description = "Id of the variant. Send it when holding rooms")
            UUID variantId,
            @Schema(description = "Name of the variant", example = "With breakfast")
            String name,
            @Schema(description = "Main description of the variant")
            DescriptionResponse description,
            @Schema(description = "Extras included with the variant")
            List<IncludedExtra> includes,
            @Schema(description = "Average price per night for all the rooms (total / nights, rounded up), in USD", example = "348")
            int pricePerNight,
            @Schema(description = "Price for all the rooms for the whole stay, room and variant together, in USD", example = "1040")
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

package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * A single variant of a room type, with the extras it includes.
 *
 * @param variantId the id of the variant
 * @param name      the name of the variant
 * @param includes  the extras included with the variant
 */
@Schema(description = "A variant of a room type, with the extras it includes")
public record VariantResponse(
        @Schema(description = "Id of the variant")
        UUID variantId,
        @Schema(description = "Name of the variant", example = "With breakfast")
        String name,
        @Schema(description = "Extras included with the variant, sorted by name. Inactive extras are left out. Empty if it includes none")
        List<IncludedExtra> includes
) {
    /**
     * An extra included with a variant.
     *
     * @param extraId the id of the extra
     * @param name    the name of the extra
     * @param amount  how many of the extra are included
     */
    @Schema(description = "An extra included with a variant")
    public record IncludedExtra(
            @Schema(description = "Id of the extra")
            UUID extraId,
            @Schema(description = "Name of the extra", example = "Breakfast")
            String name,
            @Schema(description = "How many of the extra are included. Always the real amount, also when it is hidden from customers in the variant list", example = "2")
            int amount
    ) {}
}

package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * A variant with the extras it includes, without its id. Used as the value
 * when variants are returned in a map keyed by variant id.
 *
 * @param name     the name of the variant
 * @param includes the extras included with the variant
 */
@Schema(description = "A variant with the extras it includes. The variant id is the key it is stored under")
public record VariantIncludesResponse(
        @Schema(description = "Name of the variant", example = "With breakfast")
        String name,
        @Schema(description = "Extras included with the variant, sorted by name. Inactive extras are left out. Empty if it includes none")
        List<VariantResponse.IncludedExtra> includes
) {
}

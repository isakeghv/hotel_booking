package com.example.inventoryserviceproject.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * A single variant of a room type, with the extras it includes.
 *
 * @param variantId the id of the variant
 * @param name      the name of the variant
 * @param includes  the extras included with the variant
 */
public record VariantResponse(
        UUID variantId,
        String name,
        List<IncludedExtra> includes
) {
    /**
     * An extra included with a variant.
     *
     * @param extraId the id of the extra
     * @param name    the name of the extra
     * @param amount  how many of the extra are included
     */
    public record IncludedExtra(UUID extraId, String name, int amount) {}
}

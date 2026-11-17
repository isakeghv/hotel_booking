package com.example.inventoryserviceproject.dto.response;

import java.util.List;

/**
 * A variant with the extras it includes, without its id. Used as the value
 * when variants are returned in a map keyed by variant id.
 *
 * @param name     the name of the variant
 * @param includes the extras included with the variant
 */
public record VariantIncludesResponse(
        String name,
        List<VariantResponse.IncludedExtra> includes
) {
}

package com.example.bookingserviceproject.dto.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A variant with the extras it includes, as returned by
 * {@code GET /inventory/variants}. The variant id is the key it is stored under.
 *
 * @param name     the variant's current name in inventory
 * @param includes the extras included with the variant, empty if none
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryVariant(String name, List<Include> includes) {

    /**
     * An extra included with the variant.
     *
     * @param extraId the id of the extra
     * @param name    the name of the extra
     * @param amount  how many of the extra are included
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Include(UUID extraId, String name, int amount) {}

    /**
     * The body of inventory's response: {@code {"variants": {"<variantId>": {...}}}}.
     *
     * @param variants the variants found, by variant id. Ids that match no variant are left out
     */
    public record Result(Map<UUID, InventoryVariant> variants) {}
}

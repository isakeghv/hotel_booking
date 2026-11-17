package com.example.inventoryserviceproject.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * Query for looking up several variants at once, e.g.
 * {@code ?variantIds=<uuid>,<uuid>} or {@code ?variantIds=<uuid>&variantIds=<uuid>}.
 *
 * @param variantIds the ids of the variants, 1-30
 */
public record ReviewVariantsQuery(
        @NotNull(message = "Variant ids are required")
        @Size(min = 1, max = 30, message = "Between 1 and 30 variant ids must be given")
        List<@NotNull(message = "Variant id cannot be empty") UUID> variantIds
) {
}

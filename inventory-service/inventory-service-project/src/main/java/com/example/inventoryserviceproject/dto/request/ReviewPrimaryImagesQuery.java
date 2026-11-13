package com.example.inventoryserviceproject.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReviewPrimaryImagesQuery(
        @NotNull(message = "Room-type ids must be present")
        @Size(min = 1, max = 30, message = "Between 1 and 30 room-type ids must be given")
        @ArraySchema(
                arraySchema = @Schema(description = "The room types to get the primary image of. Duplicates are returned once"),
                schema = @Schema(example = "3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                minItems = 1, maxItems = 30)
        List<@NotNull(message = "Room-type id cannot be null") UUID> roomTypeIds
) {
}

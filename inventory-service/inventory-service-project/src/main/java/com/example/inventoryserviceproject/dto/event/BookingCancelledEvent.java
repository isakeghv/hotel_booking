package com.example.inventoryserviceproject.dto.event;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

/**
 * Sent by the booking-service on the "booking-cancelled" topic when a completed
 * (paid) booking is cancelled.
 *
 * @param itemIds the item ids of all rooms in the booking, the same ids as returned when the rooms were reserved
 */
@Schema(description = "A completed (paid) booking was cancelled. Its rooms are made available again")
public record BookingCancelledEvent(
        @ArraySchema(
                arraySchema = @Schema(description = "Item ids of all rooms in the booking, as returned when the rooms were reserved. Unknown ids are ignored", requiredMode = Schema.RequiredMode.REQUIRED),
                schema = @Schema(example = "7c9e6679-7425-40de-944b-e07fc1f90ae7"))
        List<UUID> itemIds
) {}

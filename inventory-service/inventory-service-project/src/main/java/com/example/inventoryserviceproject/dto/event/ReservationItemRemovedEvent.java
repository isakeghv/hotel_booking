package com.example.inventoryserviceproject.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Sent by the booking-service on the "reservation-item-remove" topic when a
 * user removes one room from their cart before completing the booking.
 *
 * @param userId the id of the user whose cart the room is removed from
 * @param itemId the id of the cart item, as returned when the room was reserved
 */
@Schema(description = "A user removed one room from their cart before paying. That room is released")
public record ReservationItemRemovedEvent(
        @Schema(description = "Id of the user whose cart the room is removed from", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID userId,
        @Schema(description = "Id of the cart item, as returned when the room was reserved", example = "7c9e6679-7425-40de-944b-e07fc1f90ae7", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID itemId
) {}

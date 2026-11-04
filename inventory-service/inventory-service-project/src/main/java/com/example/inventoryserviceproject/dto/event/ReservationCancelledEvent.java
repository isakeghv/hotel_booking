package com.example.inventoryserviceproject.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Sent by the booking-service on the "reservation-cancelled" topic when a user
 * cancels a reservation that has not yet been paid and completed.
 *
 * @param userId the id of the user whose reserved rooms are released
 */
@Schema(description = "A user cancelled a reservation before paying. All rooms in their cart are released")
public record ReservationCancelledEvent(
        @Schema(description = "Id of the user whose cart is released", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID userId
) {}

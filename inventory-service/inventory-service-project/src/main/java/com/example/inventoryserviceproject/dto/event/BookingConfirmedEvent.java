package com.example.inventoryserviceproject.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Sent by the booking-service on the "reservation-confirmed" topic when a user has
 * paid and completed a booking.
 *
 * @param userId the id of the user whose reserved rooms are stored as booked
 */
@Schema(description = "A user paid and completed a booking. All rooms in their cart are stored as booked")
public record BookingConfirmedEvent(
        @Schema(description = "Id of the user whose cart is booked", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6", requiredMode = Schema.RequiredMode.REQUIRED)
        UUID userId
) {}

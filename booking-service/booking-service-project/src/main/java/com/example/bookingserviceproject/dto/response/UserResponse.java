package com.example.bookingserviceproject.dto.response;

import java.util.UUID;

/**
 * The guest who booked, as shown to staff: enough to look the user up in the
 * user-service. The names and {@code isActive} are null if the user-service
 * could not be asked.
 *
 * @param userId    the id of the user, from the booking. Never null
 * @param firstName the user's first name, or null
 * @param lastName  the user's last name, or null
 * @param isActive  whether the user's account is active, or null
 */
public record UserResponse(
        UUID userId,
        String firstName,
        String lastName,
        Boolean isActive
) {}

package com.example.bookingserviceproject.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * One booking in the staff's list of bookings on a date. Only what an overview
 * needs: the rooms, room numbers and extras are in the booking details.
 *
 * @param bookingId the id of the booking, used to open it
 * @param user      the guest who booked. Always present
 * @param rooms     the number of active rooms for a live booking, or every room for a cancelled one
 * @param from      the earliest check-in of the booking's rooms
 * @param to        the latest check-out of the booking's rooms
 * @param nights    the number of nights, from {@code from} to {@code to}
 */
public record StaffBookingOverview(
        UUID bookingId,
        User user,
        int rooms,
        LocalDateTime from,
        LocalDateTime to,
        int nights
) {
    /**
     * The guest who booked. The names and {@code isActive} are null if the
     * user-service could not be asked.
     *
     * @param userId    the id of the user, from the booking. Never null
     * @param firstName the user's first name, or null
     * @param lastName  the user's last name, or null
     * @param isActive  whether the user's account is active, or null
     */
    public record User(
            UUID userId,
            String firstName,
            String lastName,
            Boolean isActive
    ) {}
}

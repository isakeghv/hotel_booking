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
        UserResponse user,
        int rooms,
        LocalDateTime from,
        LocalDateTime to,
        int nights
) {}

package com.example.bookingserviceproject.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * An overview of the user's bookings, in three lists. Only what an overview
 * needs: the full booking is returned by {@code GET /booking/bookings/me/{bookingId}}.
 *
 * @param upcoming  not cancelled, starting now or later, soonest first
 * @param previous  not cancelled, started before now, most recent first
 * @param cancelled cancelled as a whole, latest start date first
 */
public record MyBookingsResponse(
        List<Overview> upcoming,
        List<Overview> previous,
        List<Overview> cancelled
) {
    /**
     * One booking in a list.
     *
     * @param bookingId the id of the booking, used to open it
     * @param from      the earliest check-in of the booking's rooms
     * @param to        the latest check-out of the booking's rooms
     * @param rooms     the number of rooms in the booking
     * @param total     the price of the whole booking
     * @param currency  the currency of the price
     * @param image     the image of the booking's first room type. Always present
     */
    public record Overview(
            UUID bookingId,
            LocalDateTime from,
            LocalDateTime to,
            int rooms,
            int total,
            String currency,
            Image image
    ) {}

    /**
     * An image from the inventory-service. Both fields are null if there is no
     * image, or it could not be fetched, so the property itself is never missing.
     *
     * @param filePath the full URL of the image, or null
     * @param altText  the text describing the image, or null
     */
    public record Image(String filePath, String altText) {}
}

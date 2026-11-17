package com.example.bookingserviceproject.dto.response;

import java.util.List;

/**
 * The response bodies of the endpoints. Every response is an object with one
 * key, e.g. {@code {"cart": {...}}}. Each record here is one such body, so the
 * key and the content are typed.
 */
public final class Envelopes {

    private Envelopes(){}

    // ----- Cart -----

    /** Body when the cart is returned. */
    public record CartResult(CartResponse cart) {}

    // ----- Bookings -----

    /** Body when one booking is returned. */
    public record BookingResult(BookingResponse booking) {}

    /** Body of the user's booking overviews. */
    public record MyBookingsResult(MyBookingsResponse bookings) {}

    // ----- Staff -----

    /** Body of the staff's list of bookings on a date. */
    public record StaffBookingsResult(List<StaffBookingOverview> bookings) {}
}

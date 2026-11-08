package com.example.bookingserviceproject.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for confirming and paying for the cart. Nothing about the
 * booking itself is sent: what is booked, and the price, always come from the cart.
 *
 * @param card the card to pay with
 */
public record ReviewConfirmBooking(
        @NotNull(message = "Card details are required")
        @Valid
        CardDetails card
) {}

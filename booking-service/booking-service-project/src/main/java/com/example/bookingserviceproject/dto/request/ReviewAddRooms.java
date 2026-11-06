package com.example.bookingserviceproject.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for adding rooms of one variant to the cart. The same limits
 * are checked by the inventory-service, but checking them here first gives
 * clearer errors and saves a call.
 *
 * @param variantId the id of the chosen variant
 * @param amount    the number of rooms, 1-10
 * @param from      the check-in date
 * @param to        the check-out date, after the check-in date
 */
public record ReviewAddRooms(
        @NotNull(message = "Variant id is required")
        UUID variantId,

        // Wrapper type so a missing value is null and caught by @NotNull, instead of silently becoming 0
        @NotNull(message = "Amount of rooms is required")
        @Range(min = 1, max = 10, message = "Amount of rooms must be between 1 and 10")
        Integer amount,

        @NotNull(message = "Check-in date is required, e.g. 2026-11-05")
        @FutureOrPresent(message = "Check-in date cannot be in the past")
        LocalDate from,

        @NotNull(message = "Check-out date is required, e.g. 2026-11-08")
        LocalDate to
) {
    public static final int MAX_NIGHTS = 30;

    /**
     * Used by validation to ensure the stay ends after it starts. Missing
     * values are left to {@link NotNull}.
     *
     * @return true if "to" is after "from", or if either is missing
     */
    @AssertTrue(message = "Check-out date must be after the check-in date")
    public boolean isValidPeriod(){
        if (from == null || to == null) return true;
        return to.isAfter(from);
    }

    /**
     * Used by validation to limit the length of a stay, the same limit as the
     * inventory-service.
     *
     * @return true if the stay is at most {@link #MAX_NIGHTS} nights, or if either date is missing
     */
    @AssertTrue(message = "A stay cannot be longer than " + MAX_NIGHTS + " nights")
    public boolean isWithinMaxStay(){
        if (from == null || to == null) return true;
        return !to.isAfter(from.plusDays(MAX_NIGHTS));
    }
}

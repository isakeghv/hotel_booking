package com.example.inventoryserviceproject.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReviewSearchQuery(
        // Wrapper types so a missing value is null and caught by @NotNull, instead of silently becoming 0
        @NotNull(message = "Number of guests is required")
        @Range(min = 1, max = 20, message = "Number of guests must be between 1 and 20")
        Integer guests,

        @NotNull(message = "Minimum number of rooms is required")
        @Range(min = 1, max = 10, message = "Minimum number of rooms must be between 1 and 10")
        Integer rooms,

        @NotNull(message = "Check-in date is required, e.g. 2026-11-05")
        @FutureOrPresent(message = "Check-in date cannot be in the past")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate from,

        @NotNull(message = "Check-out date is required, e.g. 2026-11-08")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate to
) {

        /**
         * Used by validation to ensure every room has at least one guest.
         *
         * @return true if there are not more rooms than guests, or if either is missing
         */
        @AssertTrue(message = "Number of rooms cannot be more than the number of guests")
        public boolean isValidRoomsForGuests(){
                if (guests == null || rooms == null) return true;
                return rooms <= guests;
        }

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
         * Used by validation to limit the length of a stay, the same limit as
         * when reserving rooms.
         *
         * @return true if the stay is at most {@link ReviewAvailabilityQuery#MAX_NIGHTS} nights, or if either date is missing
         */
        @AssertTrue(message = "A stay cannot be longer than " + ReviewAvailabilityQuery.MAX_NIGHTS + " nights")
        public boolean isWithinMaxStay(){
                if (from == null || to == null) return true;
                return !to.isAfter(from.plusDays(ReviewAvailabilityQuery.MAX_NIGHTS));
        }
}

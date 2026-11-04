package com.example.inventoryserviceproject.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.Hidden;
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
        @Schema(description = "Number of guests, 1-20", example = "4")
        Integer guests,

        @NotNull(message = "Minimum number of rooms is required")
        @Range(min = 1, max = 10, message = "Minimum number of rooms must be between 1 and 10")
        @Schema(description = "Minimum number of rooms, 1-10, at most one per guest", example = "2")
        Integer rooms,

        @NotNull(message = "Check-in date is required, e.g. 2026-11-05")
        @FutureOrPresent(message = "Check-in date cannot be in the past")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Check-in date (YYYY-MM-DD). Check-in is at 15:00", example = "2026-11-05")
        LocalDate from,

        @NotNull(message = "Check-out date is required, e.g. 2026-11-08")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @Schema(description = "Check-out date (YYYY-MM-DD), after the check-in date. Check-out is at 12:00. At most 30 nights", example = "2026-11-08")
        LocalDate to
) {

        /**
         * Used by validation to ensure every room has at least one guest.
         *
         * @return true if there are not more rooms than guests, or if either is missing
         */
        @Hidden
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
        @Hidden
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
        @Hidden
        @AssertTrue(message = "A stay cannot be longer than " + ReviewAvailabilityQuery.MAX_NIGHTS + " nights")
        public boolean isWithinMaxStay(){
                if (from == null || to == null) return true;
                return !to.isAfter(from.plusDays(ReviewAvailabilityQuery.MAX_NIGHTS));
        }
}

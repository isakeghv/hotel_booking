package com.example.inventoryserviceproject.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReviewOccupyRoom(
        @NotNull(message = "Start-time must be a valid date-time, and must be present")
        @FutureOrPresent(message = "Start-time cannot be in the past")
        LocalDateTime from,

        @NotNull(message = "End-time must be a valid date-time, and must be present")
        LocalDateTime to
) {

        /**
         * Used by validation to ensure the period ends after it starts. Missing
         * values are left to {@link NotNull}.
         *
         * @return true if "to" is after "from", or if either is missing
         */
        @AssertTrue(message = "Must end after it starts")
        public boolean isValidPeriod(){
                if (from == null || to == null) return true;
                return to.isAfter(from);
        }
}

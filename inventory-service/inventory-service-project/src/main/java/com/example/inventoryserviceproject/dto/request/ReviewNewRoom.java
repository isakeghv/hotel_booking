package com.example.inventoryserviceproject.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record ReviewNewRoom(
        // Wrapper types so a missing value is null and caught by @NotNull, instead of silently becoming 0 or false
        @NotNull(message = "Room number is required")
        @Range(min = 101, max = 9999, message = "Room number must be between 101 and 9999")
        int roomNumber,

        @NotNull(message = "You must choose whether the room is active")
        Boolean isActive
) {

        // TODO : Implement proper sanitation

        /**
         * Used by validation to ensure the room number has a number on its floor.
         * The digits before the last two are the floor, so e.g. 200 is not a room.
         *
         * @return true if the room number does not end in 00
         */
        @AssertTrue(message = "Room number cannot end in 00")
        public boolean isValidRoomOnFloor(){
                return roomNumber % 100 != 0;
        }

}

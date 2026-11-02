package com.example.inventoryserviceproject.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record ReviewNewRoom(
        // Wrapper types so a missing value is null and caught by @NotNull, instead of silently becoming 0 or false
        @NotNull(message = "Room number is required")
        @Range(min = 1, max = 9999, message = "Room number must be between 1 and 9999")
        int roomNumber,

        @NotNull(message = "You must choose whether the room is active")
        Boolean isActive
) {

        // TODO : Implement proper sanitation

}

package com.example.inventoryserviceproject.dto;

import jakarta.validation.constraints.*;

import java.util.List;
import java.util.UUID;

public record ReviewAddBedToRoom(
        @Min(value =  1, message = "Must be at least 1")
        @Max(value = 4, message = "Cannot exceed 4")
        int amount

) {

        // TODO : Implement proper sanitation

}

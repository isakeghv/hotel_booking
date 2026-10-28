package com.example.inventoryserviceproject.dto;

import jakarta.validation.constraints.*;

public record ReviewNewExtra(
        @NotBlank
        @Size(min = 5, max = 50, message = "Length of the extra's name must be between 5 and 50 characters long")
        String name,

        @NotNull
        @Min(value = 0, message = "Price must be at least 0")
        @Max(value = 10000, message = "Price cannot exceed 10000")
        int price,

        @NotNull(message = "addonPossible cannot be missing or null")
        boolean addonPossible,

        @NotNull(message = "isActive cannot be missing or null")
        boolean isActive,

        @NotNull(message = "isPriceFixed cannot be missing or null")
        boolean isPriceFixed

){
    public ReviewNewExtra {

        // TODO : Implement proper sanitation

        name = name == null ? null : name.trim();
    }
}

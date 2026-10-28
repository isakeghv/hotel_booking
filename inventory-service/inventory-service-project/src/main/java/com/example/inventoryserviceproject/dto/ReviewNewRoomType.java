package com.example.inventoryserviceproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

public record ReviewNewRoomType(
        @NotBlank
        @Size(min = 5, max = 50, message = "Length of the extra's name must be between 5 and 50 characters long")
        String name,

        @Range(min = 1, max = 50000)
        @NotNull(message = "A rooms daily price must be between 1 and 50 000")
        int roomPrice,

        @NotNull(message = "isActive cannot be missing or null")
        boolean isActive

){
    public ReviewNewRoomType {

        // TODO : Implement proper sanitation

        name = name == null ? null : name.trim();
    }
}
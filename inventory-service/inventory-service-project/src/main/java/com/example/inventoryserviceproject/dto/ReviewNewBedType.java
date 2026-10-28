package com.example.inventoryserviceproject.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

public record ReviewNewBedType(
        @NotNull
        @Range(min = 70, max = 220, message = "Must be a number between 70 and 220")
        int width,

        @NotNull
        @Range(min = 70, max = 220, message = "Must be a number between 70 and 220")
        int height,

        @NotNull
        @Range(min = 1, max = 2, message = "Must be a number between 1 and 2")
        int capacity,

        @NotBlank
        @Size(min = 10, max = 50, message = "Length of the bed name must be between 10 and 50 characters long")
        String name
){
    public ReviewNewBedType {

        // TODO : Implement proper sanitation

        name = name == null ? null : name.trim();
    }
}
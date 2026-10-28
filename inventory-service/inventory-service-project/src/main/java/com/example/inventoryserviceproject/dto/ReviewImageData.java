package com.example.inventoryserviceproject.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewImageData(
        @NotBlank
        @Size(min = 5, max = 100, message = "Alt text must be between 5 and 100 characters long")
        String altText,

        @Size(min = 0, max = 250, message = "Image-text must be between 0 and 250 characters long")
        String imageText
) {
    public ReviewImageData{

        // TODO : Implement proper sanitation

        altText = altText == null ? null : altText.trim();
        imageText = imageText == null ? null : imageText.trim();
    }
}

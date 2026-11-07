package com.example.inventoryserviceproject.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReviewImageData(
        @NotBlank
        @Size(min = 5, max = 100, message = "Alt text must be between 5 and 100 characters long")
        String altText,

        @Size(min = 0, max = 250, message = "Image-text must be between 0 and 250 characters long")
        String imageText,

        // Optional: a missing value becomes false. The first image of a room type is made primary regardless
        @Schema(description = "Whether the image becomes the room type's primary image, shown when the room type is listed. " +
                "Defaults to false. The previous primary image is unset automatically. " +
                "The first image of a room type is always made primary, even if this is false", example = "false")
        Boolean primary
) {
    public ReviewImageData{

        // TODO : Implement proper sanitation

        altText = altText == null ? null : altText.trim();
        imageText = imageText == null ? null : imageText.trim();
        primary = primary != null && primary;
    }
}

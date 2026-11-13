package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * The primary image of a room type: the image shown when the room type is listed.
 */
public record PrimaryImageResponse(
        @Schema(description = "Id of the room type the image belongs to")
        UUID roomTypeId,

        @Schema(description = "Full URL of the image file. Null when listing several room types and this one has no primary image",
                nullable = true, example = "http://localhost:8080/images/3fa85f64-5717-4562-b3fc-2c963f66afa6.png")
        String filePath,

        @Schema(description = "Alternative text describing the image. Null when listing several room types and this one has no primary image",
                nullable = true, example = "Double room with a view of the sea")
        String altText) {
}

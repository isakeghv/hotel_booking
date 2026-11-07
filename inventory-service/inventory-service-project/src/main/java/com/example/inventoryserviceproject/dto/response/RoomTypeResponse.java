package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

public record RoomTypeResponse(
        UUID id,
        int price,
        String name,
        int capacity,
        List<BedInfo> beds,
        List<Image> images
) {
    /**
     * An image belonging to a room type. The primary image is listed first.
     */
    public record Image(
            @Schema(description = "Id of the image, used to make it the primary image")
            UUID imageId,
            String filePath,
            String imageText,
            String altText,
            @Schema(description = "Whether this is the room type's primary image, shown when the room type is listed. Exactly one image is primary")
            boolean primary
    ) {}

    /**
     * A bed type in a room type, with the number of beds of that type.
     */
    public record BedInfo(int width, int height, int capacity, String name, int amount) {}
}

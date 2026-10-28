package com.example.inventoryserviceproject.dto.response;

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
     * An image belonging to a room type.
     */
    public record Image(String filePath, String imageText, String altText) {}

    /**
     * A bed type in a room type, with the number of beds of that type.
     */
    public record BedInfo(int width, int height, int capacity, String name, int amount) {}
}

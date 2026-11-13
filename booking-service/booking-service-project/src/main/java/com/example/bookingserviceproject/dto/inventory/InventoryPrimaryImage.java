package com.example.bookingserviceproject.dto.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

/**
 * The primary image of a room type, as returned by
 * {@code POST /inventory/roomtypes/images/primary}. A room type without a
 * primary image has {@code filePath} and {@code altText} set to null.
 *
 * @param roomTypeId the id of the room type
 * @param filePath   the full URL of the image, or null
 * @param altText    the text describing the image, or null
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryPrimaryImage(UUID roomTypeId, String filePath, String altText) {

    /**
     * The body of inventory's response: {@code {"primaryImages": [...]}}.
     *
     * @param primaryImages one entry per distinct room type id
     */
    public record Result(List<InventoryPrimaryImage> primaryImages) {}
}

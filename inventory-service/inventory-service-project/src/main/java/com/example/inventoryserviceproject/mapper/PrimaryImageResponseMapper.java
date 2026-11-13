package com.example.inventoryserviceproject.mapper;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.inventoryserviceproject.dto.response.PrimaryImageResponse;
import com.example.inventoryserviceproject.model.ImageData;


/**
 * Mapper for creating a primaryImageResponse
 * Builds response to contain image-path, alt-text and roomTypeId
 * PrimaryImageResponseMapper
 */
@Component
public class PrimaryImageResponseMapper {
    @Value("${image.base.url}") String imageBaseUrl;

    public PrimaryImageResponse toResponse(ImageData imageData, UUID roomTypeId){
        String url = imageBaseUrl + imageData.getFileName();
        return new PrimaryImageResponse(roomTypeId, url, imageData.getAltText());
    }

    /**
     * Builds a response for a room type without a primary image (no images, or
     * the room type does not exist). The path and alt-text are null.
     *
     * @param roomTypeId the id of the room type
     * @return a response with only the room type id set
     */
    public PrimaryImageResponse toEmptyResponse(UUID roomTypeId){
        return new PrimaryImageResponse(roomTypeId, null, null);
    }
}
package com.example.inventoryserviceproject.mapper;

import com.example.inventoryserviceproject.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.inventoryserviceproject.dto.response.RoomTypeResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts room type entities into {@link RoomTypeResponse} response objects.
 */
@Component
public class RoomTypeResMapper {
    private final String imageBaseUrl;

    /**
     * Creates the mapper.
     *
     * @param imageBaseUrl base URL that image file names are appended to
     */
    public RoomTypeResMapper(@Value("${image.base.url}") String imageBaseUrl) {
        this.imageBaseUrl = imageBaseUrl;
    }

    /**
     * Builds the response object for a room type.
     *
     * @param info     the room type entity
     * @param beds     the bed types used in the room type
     * @param roomBeds the links stating how many of each bed the room has
     * @param images   the images belonging to the room type
     * @return the response object
     */
    public RoomTypeResponse toResponse(RoomType info, List<Bed> beds, List<RoomBed> roomBeds, List<ImageData> images) {
        Map<UUID, Integer> amountByBedId = roomBeds.stream()
                .collect(Collectors.toMap(RoomBed::getBedId, RoomBed::getAmount, (a, b) -> a));

        List<RoomTypeResponse.BedInfo> bedInfos = beds.stream()
                .map(bed -> new RoomTypeResponse.BedInfo(
                        bed.getWidth(),
                        bed.getHeight(),
                        bed.getCapacity(),
                        bed.getName(),
                        amountByBedId.getOrDefault(bed.getBedId(), 0)))
                .toList();

        List<RoomTypeResponse.Image> imageInfos = images.stream()
                .map(img -> new RoomTypeResponse.Image(
                        imageBaseUrl + img.getFileName(),
                        img.getImageText(),
                        img.getAltText()))
                .toList();

        return new RoomTypeResponse(
                info.getTypeId(),
                info.getRoomPrice(),
                info.getName(),
                info.getCapacity(),
                bedInfos,
                imageInfos);
    }
}
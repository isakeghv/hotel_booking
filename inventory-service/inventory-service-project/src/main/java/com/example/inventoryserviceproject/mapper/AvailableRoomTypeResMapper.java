package com.example.inventoryserviceproject.mapper;

import com.example.inventoryserviceproject.dto.response.AvailableRoomTypeResponse;
import com.example.inventoryserviceproject.model.*;
import com.example.inventoryserviceproject.utils.PriceUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts an available room type into an {@link AvailableRoomTypeResponse}.
 */
@Component
public class AvailableRoomTypeResMapper {
    private final String imageBaseUrl;
    private final DescriptionResMapper descriptionResMapper;

    /**
     * Creates the mapper.
     *
     * @param imageBaseUrl         base URL that image file names are appended to
     * @param descriptionResMapper mapper that builds the description
     */
    public AvailableRoomTypeResMapper(@Value("${image.base.url}") String imageBaseUrl, DescriptionResMapper descriptionResMapper) {
        this.imageBaseUrl = imageBaseUrl;
        this.descriptionResMapper = descriptionResMapper;
    }

    /**
     * Builds the response for one available room type, priced with its
     * cheapest variant.
     *
     * @param roomType       the room type
     * @param description    the room type's main description, or null if it has none
     * @param variant        the room type's cheapest active variant
     * @param roomBeds       the links stating how many of each bed the room type has
     * @param bedsById       the bed types used in the room type, by id
     * @param capacity       how many people one room sleeps
     * @param image          the room type's first image, or null if it has none
     * @param availableRooms how many rooms of the type are free for the stay
     * @param roomsToBook    how many rooms the customer needs to book to fit their search
     * @param nights         the number of nights in the stay
     * @return the response object
     */
    public AvailableRoomTypeResponse toResponse(
            RoomType roomType,
            RoomInformation description,
            RoomTypeVariant variant,
            List<RoomBed> roomBeds,
            Map<UUID, Bed> bedsById,
            int capacity,
            ImageData image,
            int availableRooms,
            int roomsToBook,
            int nights
    ){
        List<AvailableRoomTypeResponse.BedInfo> beds = roomBeds.stream()
                .map(roomBed -> {
                    Bed bed = bedsById.get(roomBed.getBedId());
                    return new AvailableRoomTypeResponse.BedInfo(bed.getName(), roomBed.getAmount(), bed.getCapacity());
                })
                .toList();

        AvailableRoomTypeResponse.Image imageInfo = image == null
                ? null
                : new AvailableRoomTypeResponse.Image(imageBaseUrl + image.getFileName(), image.getAltText());

        int roomTotal = PriceUtils.roomTotal(roomType.getRoomPrice(), nights);
        int variantTotal = PriceUtils.variantTotal(variant.getExtraPrice(), variant.isPriceFixed(), nights);
        int totalPerRoom = PriceUtils.total(roomTotal, variantTotal);
        int pricePerNight = PriceUtils.averagePerNight(totalPerRoom, nights);

        return new AvailableRoomTypeResponse(
                roomType.getRoomTypeId(),
                roomType.getName(),
                descriptionResMapper.toResponse(description),
                capacity,
                beds,
                imageInfo,
                availableRooms,
                roomsToBook,
                nights,
                pricePerNight,
                totalPerRoom,
                totalPerRoom * roomsToBook,
                PriceUtils.CURRENCY);
    }
}

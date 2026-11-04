package com.example.inventoryserviceproject.mapper;

import com.example.inventoryserviceproject.dto.response.RoomTypeVariantsResponse;
import com.example.inventoryserviceproject.model.Extra;
import com.example.inventoryserviceproject.model.RoomTypeVariant;
import com.example.inventoryserviceproject.model.VariantIncludes;
import com.example.inventoryserviceproject.model.VariantInformation;
import com.example.inventoryserviceproject.utils.PriceUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Converts a variant into a {@link RoomTypeVariantsResponse.VariantInfo}, priced for a stay.
 */
@Component
public class VariantResMapper {
    private final DescriptionResMapper descriptionResMapper;

    /**
     * Creates the mapper.
     *
     * @param descriptionResMapper mapper that builds the description
     */
    public VariantResMapper(DescriptionResMapper descriptionResMapper){
        this.descriptionResMapper = descriptionResMapper;
    }

    /**
     * Builds the response for one variant, with prices for the given number of
     * rooms and nights, including the room type's price.
     *
     * @param variant     the variant
     * @param roomPrice   the room type's price per night
     * @param description the variant's main description, or null if it has none
     * @param includes    the links to the extras included with the variant
     * @param extrasById  the active included extras, by id. Links to other extras are left out
     * @param nights      the number of nights in the stay
     * @param rooms       the number of rooms
     * @return the response object
     */
    public RoomTypeVariantsResponse.VariantInfo toResponse(
            RoomTypeVariant variant,
            int roomPrice,
            VariantInformation description,
            List<VariantIncludes> includes,
            Map<UUID, Extra> extrasById,
            int nights,
            int rooms
    ){
        List<RoomTypeVariantsResponse.IncludedExtra> includedExtras = includes.stream()
                .filter(include -> extrasById.containsKey(include.getExtraId()))
                .map(include -> new RoomTypeVariantsResponse.IncludedExtra(
                        extrasById.get(include.getExtraId()).getName(),
                        include.getShowAmount() ? include.getAmount() : null))
                .toList();

        int roomTotal = PriceUtils.roomTotal(roomPrice, nights);
        int variantTotal = PriceUtils.variantTotal(variant.getExtraPrice(), variant.isPriceFixed(), nights);
        int total = PriceUtils.total(roomTotal, variantTotal) * rooms;
        int pricePerNight = PriceUtils.averagePerNight(total, nights);

        return new RoomTypeVariantsResponse.VariantInfo(
                variant.getVariantId(),
                variant.getVariantName(),
                descriptionResMapper.toResponse(description),
                includedExtras,
                pricePerNight,
                total);
    }
}

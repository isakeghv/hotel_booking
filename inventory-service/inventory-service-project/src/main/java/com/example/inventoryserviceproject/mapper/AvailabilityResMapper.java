package com.example.inventoryserviceproject.mapper;

import com.example.inventoryserviceproject.cache.CartItem;
import com.example.inventoryserviceproject.dto.response.AvailabilityResponse;
import com.example.inventoryserviceproject.model.RoomType;
import com.example.inventoryserviceproject.model.RoomTypeVariant;
import com.example.inventoryserviceproject.utils.PriceUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Converts the rooms reserved by one request into an {@link AvailabilityResponse}.
 */
@Component
public class AvailabilityResMapper {

    /**
     * Builds the response for the rooms reserved by one request. Only contains
     * the prices of what was reserved, not the rest of the user's cart.
     *
     * @param variant   the chosen variant, with its room type loaded
     * @param items     the reserved rooms, all for the same variant and dates
     * @param nights    the number of nights
     * @param expiresAt when the reservation expires
     * @return the response object
     */
    public AvailabilityResponse toResponse(RoomTypeVariant variant, List<CartItem> items, int nights, Instant expiresAt){
        RoomType roomType = variant.getRoomType();
        CartItem first = items.getFirst();

        List<AvailabilityResponse.RoomInfo> rooms = items.stream()
                .map(item -> new AvailabilityResponse.RoomInfo(item.itemId(), item.roomNumber(), item.total()))
                .toList();

        return new AvailabilityResponse(
                PriceUtils.CURRENCY,
                expiresAt,
                first.from(),
                first.to(),
                nights,
                new AvailabilityResponse.RoomTypeInfo(roomType.getRoomTypeId(), roomType.getName(), first.roomPricePerNight()),
                new AvailabilityResponse.VariantInfo(variant.getVariantId(), variant.getVariantName(), first.variantPrice(), first.variantPriceFixed()),
                rooms,
                items.stream().mapToInt(CartItem::total).sum());
    }
}

package com.example.inventoryserviceproject.dto.request;

public record ReviewNewRoomVariant(

        // TODO : Implement validation

        String variantName,
        int extraPrice,
        boolean isActive,
        boolean isPriceFixed
) {

    // TODO: Implement sanitation
}

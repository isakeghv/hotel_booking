package com.example.inventoryserviceproject.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Range;

public record ReviewNewRoomVariant(
        @NotBlank(message = "Variant name is required")
        @Size(min = 3, max = 50, message = "Variant name must be between 3 and 50 characters long")
        String variantName,

        // Wrapper types so a missing value is null and caught by @NotNull, instead of silently becoming 0 or false
        @NotNull(message = "Additional price price is required")
        @Range(min = 0, max = 10000, message = "The additional price must be between 0 and 10000")
        int extraPrice,

        @NotNull(message = "You must choose whether the variant is active")
        Boolean isActive,

        @NotNull(message = "You must choose whether the extra price is per stay (fixed) or per night")
        Boolean isPriceFixed
) {
    public ReviewNewRoomVariant {

        // TODO : Implement proper sanitation

        variantName = variantName == null ? null : variantName.trim();
    }
}

package com.example.inventoryserviceproject.dto.request;

import jakarta.validation.constraints.*;

public record ReviewLinkExtraToVariant(
        @Min(value = 1, message = "Must be at least 1")
        @Max(value = 10, message = "Must be max 10")
        int amount,

        boolean showAmount
) {

        // TODO : Implement proper sanitation

}

package com.example.inventoryserviceproject.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record ReviewExtraQuery(
        // Wrapper type so a missing value is null and caught by @NotNull, instead of silently becoming 0
        @NotNull(message = "Amount is required")
        @Range(min = 1, max = 10, message = "Amount must be between 1 and 10")
        Integer amount
) {}

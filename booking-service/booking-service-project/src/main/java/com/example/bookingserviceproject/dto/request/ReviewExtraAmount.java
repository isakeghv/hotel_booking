package com.example.bookingserviceproject.dto.request;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

/**
 * Request body for setting how many of an extra a room in the cart has. The
 * amount replaces the current one, so the front end sends what it shows.
 *
 * @param amount how many of the extra the room should have, 1-10 (the same limit as the inventory-service)
 */
public record ReviewExtraAmount(
        // Wrapper type so a missing value is null and caught by @NotNull, instead of silently becoming 0
        @NotNull(message = "Amount is required")
        @Range(min = 1, max = 10, message = "Amount must be between 1 and 10")
        Integer amount
) {}

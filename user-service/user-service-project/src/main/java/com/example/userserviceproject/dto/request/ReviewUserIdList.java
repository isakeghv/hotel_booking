package com.example.userserviceproject.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

/**
 * validating request containing list of userId in body.
 * @param userIds List of UUID's.
 */
public record ReviewUserIdList(
        @NotNull(message = "userIds must be present")
        @Size(min = 1, max = 30, message = "Between 1 and 30 user ids must be given")
        List<@NotNull(message = "userId id cannot be null") UUID> userIds
) {
}

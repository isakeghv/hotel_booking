package com.example.userserviceproject.dto.response;

import java.util.UUID;

/**
 * Response record for basic information of single user.
 * @param firstName
 * @param lastName
 * @param userId
 * @param isActive
 */
public record BasicUserInfoResponse(
        String firstName,
        String lastName,
        UUID userId,
        boolean isActive
) {
}

package com.example.bookingserviceproject.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * A user's basic information, as returned by the user-service's
 * {@code POST /users/basic} and {@code GET /users/{userId}/basic}. A user the
 * user-service does not know has every field null, and {@code isActive} false.
 *
 * @param userId    the id of the user, or null if the user is not known
 * @param firstName the user's first name, or null
 * @param lastName  the user's last name, or null
 * @param isActive  whether the user's account is active
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record BasicUserInfo(UUID userId, String firstName, String lastName, boolean isActive) {

    // How a user the user-service does not know is shown, the same as POST /users/basic returns it
    public static final BasicUserInfo UNKNOWN = new BasicUserInfo(null, null, null, false);

    /**
     * The body of {@code GET /users/{userId}/basic}: {@code {"user": {...}}}.
     *
     * @param user the user
     */
    public record Result(BasicUserInfo user) {}
}

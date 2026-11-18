package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.dto.response.UserResponse;
import com.example.bookingserviceproject.dto.user.BasicUserInfo;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Converts the user-service's basic user information into the {@link UserResponse} shown to staff.
 */
@Component
public class UserResMapper {

    /**
     * Builds the guest shown with a booking. The user's id always comes from the
     * booking, so staff can find the user even without the user-service's answer.
     *
     * @param userId the id of the user, from the booking
     * @param user   the user's basic information, or null if the user-service could not be asked
     * @return the guest, with the names and {@code isActive} null if there is no user information
     */
    public UserResponse toResponse(UUID userId, BasicUserInfo user){
        if (user == null) return new UserResponse(userId, null, null, null);
        return new UserResponse(userId, user.firstName(), user.lastName(), user.isActive());
    }
}

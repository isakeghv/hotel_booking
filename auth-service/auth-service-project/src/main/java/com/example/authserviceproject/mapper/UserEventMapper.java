package com.example.authserviceproject.mapper;

import com.example.authserviceproject.dto.ReviewSignUp;
import com.example.authserviceproject.dto.UserRegisteredEvent;
import com.example.authserviceproject.model.User;

/**
 * Maps registration data to events published to Kafka.
 */
public class UserEventMapper {

    /**
     * Creates a {@link UserRegisteredEvent} from a saved user and the sign-up request. The user id comes from the user, and all other fields come from the request.
     *
     * @param user the newly saved user
     * @param req the sign-up request the user was created from
     * @return the event describing the registered user
     */
    public static UserRegisteredEvent toUserRegisteredEvent(User user, ReviewSignUp req){
        return new UserRegisteredEvent(
                user.getUserId(),
                req.firstName(),
                req.lastName(),
                req.email(),
                req.phoneNumber(),
                req.countryCode()
        );
    }
}

package com.example.userserviceproject.mapper;
import com.example.userserviceproject.dto.response.BasicUserInfoResponse;

import com.example.userserviceproject.model.User;
import org.springframework.stereotype.Component;

/**
 * Mapper for turning user-object into response object
 */
@Component
public class BasicUserInfoResponseMapper {

    /**
     * turning user-object into basic user response object
     * @param user user-object to turn into response-object
     * @return BasicUserInfoResponse of user.
     */
    public BasicUserInfoResponse toResponse(User user){
        return new BasicUserInfoResponse(
                user.getFirstName(),
                user.getLastName(),
                user.getUserId(),
                user.getIsActive()
        );
    }
}

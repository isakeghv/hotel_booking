package com.example.userserviceproject.service;

import com.example.userserviceproject.dto.response.BasicUserInfoResponse;
import com.example.userserviceproject.exception.InternalServerException;
import com.example.userserviceproject.exception.NotFoundException;
import com.example.userserviceproject.mapper.BasicUserInfoResponseMapper;
import java.util.stream.Collectors;
import com.example.userserviceproject.model.User;
import com.example.userserviceproject.repository.UserRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BasicUserInfoResponseMapper basicUserInfoResponseMapper;

    public UserService(
            UserRepository userRepository,
            BasicUserInfoResponseMapper basicUserInfoResponseMapper

    ){
        this.userRepository = userRepository;
        this.basicUserInfoResponseMapper = basicUserInfoResponseMapper;
    }

    /**
     * To get basic user information of a user
     * @param userId UUID, id of user to find
     * @return BasicR
     */
    public BasicUserInfoResponse getBasicUserInfo(UUID userId){
        User user;
        try {
            Optional<User> optUser = userRepository.findByUserId(userId);
            if (optUser.isEmpty()) throw new NotFoundException("user not found");
            user = optUser.get();
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error");
        }

        return basicUserInfoResponseMapper.toResponse(user);
    }

    /**
     * To get basic information for several users by userId.
     * <p>
     *     Does not throw any errs. If a user is not found, it instead sets it as an empty user.
     *     method is mainly intended to be used for docker-internal calls,
     *     where throwing err can cause unwanted issues; no users being returned at all due to few missing
     * </p>
     * @param userIds UUID, list of user-ids for users to find
     * @return a map of basic user-info by userId
     */
    public Map<UUID, BasicUserInfoResponse> getBasicUsersInfo(List<UUID> userIds){
        List<User> users = userRepository.findAllByUserIdIn(userIds);

        // Maps by filtering out single user from list using userId
        // If user is not found in list of users, an empty user is created.
        return  userIds.stream().collect(Collectors.toMap(
                userId -> userId,
                userId -> {
                    User user = users
                            .stream()
                            .filter(
                                    usr -> usr.getUserId().equals(userId)
                            ).findFirst()
                            .orElse(new User());
                    return basicUserInfoResponseMapper.toResponse(user);
                }
        ));


    }
}

package com.example.userserviceproject.controller;

import com.example.userserviceproject.dto.request.ReviewUserIdList;
import com.example.userserviceproject.dto.response.BasicUserInfoResponse;
import com.example.userserviceproject.service.UserService;
import com.example.userserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    /**
     * Used to return the user-information. Only for internal use
     * @param userId UUID, id of the user
     * @return the basic user information
     */
    @GetMapping("/{userId}/basic")
    public ResponseEntity<Map<String, BasicUserInfoResponse>> getUserInfo(@PathVariable UUID userId){
        BasicUserInfoResponse userResponse = userService.getBasicUserInfo(userId);
        return ClientResponse.ok("user", userResponse);
    }

    /**
     * Used to return the user-information of several users. Only for internal use
     * @param req http request containing userIds of users to get basic information for
     * @return basic user-information of several users
     */
    @PostMapping("/basic")
    public ResponseEntity<Map<UUID, BasicUserInfoResponse>> getUserInfo(@Valid @RequestBody ReviewUserIdList req){
        Map<UUID, BasicUserInfoResponse> userResponse = userService.getBasicUsersInfo(req.userIds());
        return ResponseEntity.status(200).body(userResponse);
    }
}

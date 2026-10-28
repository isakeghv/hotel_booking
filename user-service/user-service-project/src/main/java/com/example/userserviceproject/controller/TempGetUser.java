package com.example.userserviceproject.controller;

import com.example.userserviceproject.exception.NotFoundException;
import com.example.userserviceproject.model.User;
import com.example.userserviceproject.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

// Temp, used while developing
@RestController
@RequestMapping("/users")
public class TempGetUser {
    private final UserRepository userRepository;

    public TempGetUser(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(@RequestHeader("X-user-id") String rawUserId){
        UUID userId = UUID.fromString(rawUserId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return ResponseEntity.ok(user);
    }

    @GetMapping("")
    public ResponseEntity<List<User>> getUsers(){

        List<User> users = userRepository.findAll();

        return ResponseEntity.ok(users);
    }
}

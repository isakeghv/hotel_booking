package com.example.authserviceproject.controller;

import com.example.authserviceproject.dto.ReviewSignUp;
import com.example.authserviceproject.model.User;
import com.example.authserviceproject.service.RegistrationService;
import com.example.authserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for registering new users
 */
@RestController
@RequestMapping("/auth")
public class SignUp {
    private final RegistrationService service;

    /**
     * Creates the controller with the service that performs registration.
     *
     * @param service the service used to register new users
     */
    public SignUp(RegistrationService service){
        this.service = service;
    }

    /**
     * Registers a new user from the validated sign-up request.
     *
     * @param req the validated sign-up details from the request body
     * @return a 200 response with a message confirming the user was created
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@Valid @RequestBody ReviewSignUp req){
        service.registerUser(req);
        return ClientResponse.okMsg("User successfully created");
    }
}

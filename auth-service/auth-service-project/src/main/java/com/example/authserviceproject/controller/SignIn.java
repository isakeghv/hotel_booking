package com.example.authserviceproject.controller;

import com.example.authserviceproject.dto.ReviewSignin;
import com.example.authserviceproject.service.SignInService;
import com.example.authserviceproject.utils.ClientResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for signing users in
 */
@RestController
@RequestMapping("/auth")
public class SignIn {
    private final SignInService service;

    /**
     * Creates the controller with the service that performs sign-in.
     *
     * @param signInService the service used to authenticate users
     */
    public SignIn(SignInService signInService){
        this.service = signInService;
    }

    /**
     * Authenticates a user and starts a new session.
     *
     * @param req        the validated sign-in credentials from the request body
     * @param rawRequest the incoming HTTP request
     * @param res        the HTTP response, which the service may modify
     *                   (e.g. to set the refresh token cookie)
     * @return a 200 response containing the new access token under the key {@code accessToken}
     */
    @PostMapping("/signin")
    public ResponseEntity<Map<String, String>> signin(@Valid @RequestBody ReviewSignin req, HttpServletRequest rawRequest, HttpServletResponse res){
        String accessToken = service.signInUser(req, rawRequest, res);
        return ClientResponse.ok("accessToken", accessToken);
    }
}

package com.example.authserviceproject.controller;

import com.example.authserviceproject.service.RefreshService;
import com.example.authserviceproject.utils.ClientResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for issuing new access tokens
 */
@RestController
@RequestMapping("/auth")
public class Refresh {
    private final RefreshService service;

    /**
     * Creates the controller with the service that issues access tokens.
     *
     * @param service the service used to create new access tokens
     */
    public Refresh(RefreshService service){
        this.service = service;
    }

    /**
     * Issues a new access token for the caller, based on the information in
     * the incoming request (e.g. the refresh token cookie).
     *
     * @param request the incoming HTTP request
     * @return a 200 response containing the new access token under the key
     *         {@code accessToken}
     */
    @GetMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(HttpServletRequest request){
        String accessToken = service.accessToken(request);
        return ClientResponse.ok("accessToken", accessToken);
    }
}

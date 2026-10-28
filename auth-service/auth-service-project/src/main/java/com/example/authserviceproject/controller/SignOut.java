package com.example.authserviceproject.controller;

import com.example.authserviceproject.service.SignOutService;
import com.example.authserviceproject.utils.ClientResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for ending sessions
 */
@RestController
@RequestMapping("/auth")
public class SignOut {
    private final SignOutService service;

    /**
     * Creates the controller with the service that performs sign-out.
     *
     * @param service the service used to sign users out
     */
    public SignOut(SignOutService service){
        this.service = service;
    }

    /**
     * Signs the user out of the current session.
     *
     * @param req the incoming HTTP request
     * @param res the HTTP response, which the service may modify
     *            (e.g. to clear the session cookie)
     * @return a 200 response with the message returned by the service
     */
    @PostMapping("/signout")
    public ResponseEntity<Map<String, String>> signOut(HttpServletRequest req, HttpServletResponse res){
        String responseMsg = service.signOutUser(req, res);
        return ClientResponse.okMsg(responseMsg);
    }

    /**
     * Signs the user out of all of their sessions.
     *
     * @param req the incoming HTTP request
     * @param res the HTTP response, which the service may modify
     *            (e.g. to clear the session cookie)
     * @return a 200 response with the message returned by the service
     */
    @PostMapping("/signout/sessions")
    public ResponseEntity<Map<String, String>> signOutAll(HttpServletRequest req, HttpServletResponse res){
        String responseMsg = service.signOutAll(req, res);
        return ClientResponse.okMsg(responseMsg);
    }
}

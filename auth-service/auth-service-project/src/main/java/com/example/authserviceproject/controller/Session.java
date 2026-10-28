package com.example.authserviceproject.controller;

import com.example.authserviceproject.dto.SessionSummary;
import com.example.authserviceproject.service.SessionService;
import com.example.authserviceproject.utils.ClientResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for listing a user's sessions
 */
@RestController
@RequestMapping("/auth")
public class Session {

    private final SessionService service;

    /**
     * Creates the controller with the service that provides session data.
     *
     * @param service the service used to look up sessions
     */
    public Session(SessionService service){
        this.service = service;
    }

    /**
     * Returns a summary of the sessions belonging to the authenticated user.
     * The user and session ids come from the {@code X-User-Id} and
     * {@code X-Session-Id} headers, which the gateway sets from the validated
     * access token.
     *
     * @param request   the incoming HTTP request
     * @param userId    the id of the authenticated user
     * @param sessionId the id of the caller's current session
     * @return a 200 response with the list of session summaries under the key {@code sessions}
     */
    @GetMapping("/sessions")
    public ResponseEntity<Map<String, List<SessionSummary>>> getSessions(
            HttpServletRequest request,
            @RequestHeader("X-user-id") String userId,
            @RequestHeader("X-session-id") String sessionId
    ){

        // NOTE : Content below should remain
        List<SessionSummary> sessions = service.getSessions(request, userId, sessionId);
        return ClientResponse.ok("sessions", sessions);
    }
}

package com.example.authserviceproject.service;

import com.example.authserviceproject.dto.SessionSummary;
import com.example.authserviceproject.model.Session;
import com.example.authserviceproject.repository.SessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service for retrieving a user's sessions.
 */
@Service
public class SessionService {
    private final SessionRepository sessionRepo;

    /**
     * Creates the service with the repository used to load sessions.
     *
     * @param sessionRepo repository used to look up sessions
     */
    public SessionService(SessionRepository sessionRepo){
        this.sessionRepo = sessionRepo;
    }

    /**
     * Returns summaries of all sessions belonging to a user, marking the one matching the given session id as current.
     *
     * @param request the incoming HTTP request, currently unused
     * @param userId the id of the user, as a UUID string
     * @param sessionId the id of the current session, as a UUID string
     * @return a summary for each of the user's sessions
     */
    public List<SessionSummary> getSessions(HttpServletRequest request, String userId, String sessionId){
        UUID UuidUserId = UUID.fromString(userId);
        List<Session> sessions = sessionRepo.findByUserId(UuidUserId);

        return sessions.stream()
                .map(s -> new SessionSummary(
                        s.getSessionId(),
                        s.getSessionId().toString().equals(sessionId),
                        s.getIssuedAt()
                ))
                .toList();
    }
}

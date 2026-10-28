package com.example.authserviceproject.service;

import com.example.authserviceproject.exception.NotFoundException;
import com.example.authserviceproject.exception.UnauthorizedException;
import com.example.authserviceproject.repository.SessionRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service for ending sessions. Deletes sessions from the database and removes the refresh token cookie.
 */
@Service
public class SignOutService {
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final SessionRepository sessionRepo;

    /**
     * Creates the service with its dependencies.
     *
     * @param jwtService service used to parse refresh tokens
     * @param cookieService service used to read and remove the refresh token cookie
     * @param sessionRepo repository used to delete sessions
     */
    public SignOutService(JwtService jwtService, CookieService cookieService, SessionRepository sessionRepo){
        this.jwtService = jwtService;
        this.cookieService = cookieService;
        this.sessionRepo = sessionRepo;
    }

    /**
     * Signs the user out of the session identified by the refresh token cookie. The session is deleted and the cookie removed.
     *
     * @param request the incoming HTTP request, used to read the cookie
     * @param response the HTTP response, used to remove the cookie
     * @return a message confirming the sign-out
     * @throws UnauthorizedException if the cookie is missing, expired or invalid
     * @throws NotFoundException if the session does not exist
     */
    public String signOutUser(HttpServletRequest request, HttpServletResponse response){
        // TODO : Update to use x-user-id passed down by gateway instead
        String cookie = cookieService
                .findCookie(request)
                .orElseThrow(() -> new UnauthorizedException("Sign-in token not found"));

        try{
            // Parsing token into claims
            Claims claims = jwtService.parseRefreshToken(cookie);

            // Fetching sessionId and userId and turning sessionId and userId into UUID, to query db
            UUID sessionId = UUID.fromString(claims.get("sessionId", String.class));
            UUID userId = UUID.fromString(claims.getSubject());

            //deleting rows from sessions table. Throwing exception if none were found/deleted
            int deletedRows = sessionRepo.deleteBySessionIdAndUserId(sessionId, userId);
            if ( deletedRows == 0) throw new NotFoundException("Session not found");

            // Removing cookie once session is deleted and returning msg
            cookieService.removeCookie(response);
            return "Successfully signed out";

        } catch (ExpiredJwtException e){
            throw new UnauthorizedException("Token has expired");
        } catch (JwtException e){
            throw new UnauthorizedException("Invalid token");
        }
    }

    /**
     * Signs the user out of all of their sessions. The user is identified by the refresh token cookie, all of that user's sessions are deleted and the cookie removed.
     *
     * @param request the incoming HTTP request, used to read the cookie
     * @param response the HTTP response, used to remove the cookie
     * @return a message confirming that all sessions were signed out
     * @throws UnauthorizedException if the cookie is missing, expired or invalid
     * @throws NotFoundException if the user has no sessions
     */
    public String signOutAll(HttpServletRequest request, HttpServletResponse response){
        // Finding cookie, throwing exception if not found
        String cookie = cookieService
                .findCookie(request)
                .orElseThrow(() -> new UnauthorizedException("Sign-in token not found"));

        try {
            // Parsing token into claims
            Claims claims = jwtService.parseRefreshToken(cookie);

            // Fetching sessionId and userId and turning sessionId and userId into UUID, to query db
            UUID sessionId = UUID.fromString(claims.get("sessionId", String.class));
            UUID userId = UUID.fromString(claims.getSubject());

            //deleting rows from sessions table. Throwing exception if none were found/deleted
            int deletedRows = sessionRepo.deleteByUserId(userId);
            if (deletedRows == 0) throw new NotFoundException("No sessions found.");

            // Removing cookie once session is deleted and returning msg
            cookieService.removeCookie(response);
            return "Successfully signed out all sessions";

        } catch (ExpiredJwtException e){
            throw new UnauthorizedException("Token has expired");
        } catch (JwtException e){
            throw new UnauthorizedException("Invalid token");
        }
    }
}

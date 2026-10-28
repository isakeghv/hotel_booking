package com.example.authserviceproject.service;

import com.example.authserviceproject.exception.NotFoundException;
import com.example.authserviceproject.exception.UnauthenticatedException;
import com.example.authserviceproject.exception.UnauthorizedException;
import com.example.authserviceproject.model.Session;
import com.example.authserviceproject.model.User;
import com.example.authserviceproject.repository.SessionRepository;
import com.example.authserviceproject.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for issuing new access tokens from a refresh token.
 */
@Service
public class RefreshService {
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final SessionRepository sessionRepo;
    private final UserRepository userRepo;

    /**
     * Creates the service with its dependencies.
     *
     * @param jwtService service used to parse and create tokens
     * @param cookieService service used to read the refresh token cookie
     * @param sessionRepo repository used to look up and delete sessions
     * @param userRepo repository used to look up users
     */
    public RefreshService(JwtService jwtService, CookieService cookieService, SessionRepository sessionRepo, UserRepository userRepo){
        this.jwtService = jwtService;
        this.cookieService = cookieService;
        this.sessionRepo = sessionRepo;
        this.userRepo = userRepo;
    }

    /**
     * Issues a new access token. The refresh token is read from the cookie and validated, the session must exist and not be expired, and the user must exist. An expired session is deleted.
     *
     * @param request the incoming HTTP request, used to read the cookie
     * @return a new access token for the session
     * @throws NotFoundException if the cookie, session or user is not found
     * @throws UnauthenticatedException if the refresh token or the session has expired
     * @throws UnauthorizedException if the refresh token is invalid or malformed
     */
    public String accessToken(HttpServletRequest request){
        Optional<String> optCookie = cookieService.findCookie(request);

        if (optCookie.isEmpty()) throw new NotFoundException("No sign-in token found");
        String cookie = optCookie.get();

        // Attempting to parse token into claims. If failed, catch and throw exception
        Claims claims;
        try {
            claims = jwtService.parseRefreshToken(cookie);
        } catch (ExpiredJwtException e){
            throw new UnauthenticatedException("Sign-in has expired");
        } catch (SignatureException e){
            throw new UnauthorizedException("Sign-in token is invalid");
        } catch (MalformedJwtException e){
            throw new UnauthorizedException("Sign-in token is malformed");
        } catch (JwtException e){
            throw new UnauthorizedException("Invalid token");
        }

        // Fetching sessionId and userId and as UUID, to query db
        UUID sessionId = UUID.fromString(claims.get("sessionId", String.class));
        UUID userId = UUID.fromString(claims.getSubject());

        // Getting session from db, throwing err if session is not found
        Optional<Session> optSession = sessionRepo.findBySessionIdAndUserId(sessionId, userId);
        if (optSession.isEmpty()) throw new NotFoundException("Session not found");

        Session session = optSession.get();
        LocalDateTime expiration = session.getExpiresAt();

        // Checking if session has expired in db
        // Delete session in db and throw err if true
        if (expiration.isBefore(LocalDateTime.now())) {
            sessionRepo.deleteBySessionIdAndUserId(sessionId, userId);
            throw new UnauthenticatedException("Sign-in session has expired");
        }

        // Getting user from db, throwing exception if not found
        Optional<User> optUser = userRepo.findByUserId(userId);
        if ( optUser.isEmpty()) throw new NotFoundException("User not found");
        User user = optUser.get();

        boolean userIsActive = user.getIsActive();
        if (!userIsActive) throw new UnauthorizedException("Inactive account");

        // Creating and returning new accessToken
        return jwtService.getAccessToken(session, user.getPermissionLevel());
    }
}

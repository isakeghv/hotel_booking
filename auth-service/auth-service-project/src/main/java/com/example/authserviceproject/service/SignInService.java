package com.example.authserviceproject.service;

import com.example.authserviceproject.dto.ReviewSignin;
import com.example.authserviceproject.exception.ConflictException;
import com.example.authserviceproject.exception.NotFoundException;
import com.example.authserviceproject.exception.UnauthorizedException;
import com.example.authserviceproject.model.Session;
import com.example.authserviceproject.model.User;
import com.example.authserviceproject.repository.SessionRepository;
import com.example.authserviceproject.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for signing users in. Creates a session, issues access and refresh tokens, and sets the refresh token cookie.
 */
@Service
public class SignInService {
    private final UserRepository userRepo;
    private final PasswordService pwdService;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final SessionRepository sessionRepo;

    /**
     * Creates the service with its dependencies.
     *
     * @param userRepository repository used to look up users
     * @param passwordService service used to verify passwords
     * @param jwtService service used to create and parse tokens
     * @param cookieService service used to manage the refresh token cookie
     * @param sessionRepo repository used to store sessions
     */
    public SignInService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService, CookieService cookieService, SessionRepository sessionRepo){
        this.userRepo = userRepository;
        this.pwdService = passwordService;
        this.jwtService = jwtService;
        this.cookieService = cookieService;
        this.sessionRepo = sessionRepo;
    }

    /**
     * Signs a user in. If the request carries a valid refresh cookie for an unexpired session, the user is considered already signed in. If the session is expired it is deleted, and an invalid or expired cookie is removed before sign-in continues. The user must exist, be active and supply the correct password. On success a new session is stored and the refresh token is set as a cookie.
     *
     * @param req the validated sign-in request
     * @param rawRequest the incoming HTTP request, used to read the existing cookie
     * @param res the HTTP response, used to set or remove the cookie
     * @return a new access token for the session
     * @throws ConflictException if the user already has a valid session
     * @throws NotFoundException if the user does not exist or is inactive
     * @throws UnauthorizedException if the password is incorrect
     */
    public String signInUser(ReviewSignin req, HttpServletRequest rawRequest, HttpServletResponse res){
        // First check if user is already signed in, e.g. has a cookie
        // If cookie exists and is valid, check if session exists.
        //      - If true, check if expired or not.
        //          - If not expired, redirect to refresh endpoint.
        //          - If expired, remove session
        //      - if not valid, remove cookie and proceed
        Optional<String> optCookie = cookieService.findCookie(rawRequest);
        if (optCookie.isPresent()){
            String cookie = optCookie.get();
            try {
                Claims claims = jwtService.parseRefreshToken(cookie);

                // Fetching sessionId and userId and as UUID, to query db
                UUID sessionId = UUID.fromString(claims.get("sessionId", String.class));
                UUID userId = UUID.fromString(claims.getSubject());

                // Getting session from db
                Optional<Session> optSession = sessionRepo.findBySessionIdAndUserId(sessionId, userId);
                if (optSession.isPresent() && optSession.get().getExpiresAt().isAfter(LocalDateTime.now())){
                    // Throw exception if already signed in
                    // TODO: Fix so user gets new accessToken instead of throwing exception
                    throw new ConflictException("Already signed in. Please refresh token instead");
                }
                else if (optSession.isPresent()){
                    // Delete session if present and expired
                    sessionRepo.deleteBySessionIdAndUserId(sessionId, userId);
                }

                // Removing cookie if expired.
                cookieService.removeCookie(res);
            } catch (JwtException e){
                cookieService.removeCookie(res);
            }
        }

        // Check if user exists, if not, throw exception
        User user = userRepo
                .findByCredentials_Email(req.email())
                .orElseThrow(() -> new NotFoundException("Invalid credentials"));

        // If the user is not active, e.g. deleted, throw now found exception
        if (!user.getIsActive()) throw new NotFoundException("Invalid credentials");

        // Checking if pwd is correct, if false, throw exception
        boolean PwdIsMatching = pwdService.checkPassword(req.password(), user.getCredentials().getPassword());
        if (!PwdIsMatching) throw new UnauthorizedException("Invalid credentials");

        //creating session obj and storing to db
        Session session = new Session(user.getUserId());
        sessionRepo.save(session);

        // Creating access- and refreshToken and setting cookie
        String accessToken = jwtService.getAccessToken(session, user.getPermissionLevel());
        String refreshToken = jwtService.getRefreshToken(session, user.getPermissionLevel());
        cookieService.setCookie(res, refreshToken, session.getExpiresAt());

        return accessToken;
    }
}

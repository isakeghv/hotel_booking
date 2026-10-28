package com.example.authserviceproject.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

/**
 * Service for managing the HTTP-only refresh token cookie. The cookie name, path and secure flag are configurable through {@code cookie.name}, {@code cookie.path} and {@code cookie.secure}.
 */
@Service
public class CookieService {
    @Value("${cookie.secure:true}")
    private boolean cookieSecure;
    @Value("${cookie.path:/auth}")
    private String cookiePath;
    @Value("${cookie.name:refreshToken}")
    private String cookieName;

    /**
     * Adds the refresh token cookie to the response. The cookie is HTTP-only and lives until the given expiration.
     *
     * @param response the HTTP response to add the cookie to
     * @param token the token to store in the cookie
     * @param expiration the time the cookie should expire
     */
    public void setCookie(HttpServletResponse response, String token, LocalDateTime expiration){
        long maxAgeSeconds = Duration.between(LocalDateTime.now(), expiration).getSeconds();

        Cookie cookie = new Cookie(cookieName, token);
        cookie.setPath(cookiePath);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge((int) maxAgeSeconds);
        response.addCookie(cookie);
    }

    /**
     * Removes the refresh token cookie by adding an empty cookie with max age 0 to the response.
     *
     * @param response the HTTP response to add the cookie to
     */
    public void removeCookie(HttpServletResponse response){
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setPath(cookiePath);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    /**
     * Finds the refresh token cookie in a request.
     *
     * @param req the incoming HTTP request
     * @return the cookie value, or empty if the request has no such cookie
     */
    public Optional<String> findCookie(HttpServletRequest req){
        if ( req.getCookies() == null) return Optional.empty();

        return Arrays.stream(req.getCookies())
                .filter(c -> c.getName().equals(cookieName))
                .map(Cookie::getValue)
                .findFirst();
    }
}

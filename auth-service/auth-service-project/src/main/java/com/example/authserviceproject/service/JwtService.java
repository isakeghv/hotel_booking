package com.example.authserviceproject.service;

import com.example.authserviceproject.model.Session;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * Service for creating and parsing JWT access and refresh tokens. Each token type is signed with its own HMAC secret, configured through {@code access.secret} and {@code refresh.secret}.
 */
@Service
public class JwtService {
    @Value("${access.secret}")
    private String accessSecret;
    @Value("${refresh.secret}")
    private String refreshSecret;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    /**
     * Decodes the Base64 secrets and builds the signing keys. Runs once after construction.
     */
    @PostConstruct
    private void init(){
        accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
    }

    /**
     * Builds a signed token for a session. The subject is the user id, and the claims contain the session id and permission level.
     *
     * @param session the session the token is for
     * @param expiresAt the time the token expires
     * @param permission the user's permission level
     * @param key the key used to sign the token
     * @return the compact signed token
     */
    private String createToken(Session session, LocalDateTime expiresAt, int permission, SecretKey key){
        Date issuedAt = Date.from(session.getIssuedAt().atZone(ZoneId.systemDefault()).toInstant());
        Date expiration = Date.from(expiresAt.atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder()
                .subject(session.getUserId().toString())
                .claim("sessionId", session.getSessionId().toString())
                .claim("permissionLevel", permission)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    /**
     * Creates a refresh token that expires when the session expires.
     *
     * @param session the session the token is for
     * @param permissionLevel the user's permission level
     * @return the compact signed refresh token
     */
    public String getRefreshToken(Session session, int permissionLevel){
        return createToken(session, session.getExpiresAt(), permissionLevel, refreshKey);
    }

    /**
     * Creates an access token that expires 15 minutes after the session was issued.
     *
     * @param session the session the token is for
     * @param permissionLevel the user's permission level
     * @return the compact signed access token
     */
    public String getAccessToken(Session session, int permissionLevel){
        LocalDateTime expiration = LocalDateTime.now().plusMinutes(15);
        return createToken(session, expiration, permissionLevel, accessKey);
    }

    /**
     * Verifies a refresh token and returns its claims.
     *
     * @param token the compact refresh token
     * @return the claims in the token
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired or has an invalid signature
     */
    public Claims parseRefreshToken(String token){
        return Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Verifies an access token and returns its claims.
     *
     * @param token the compact access token
     * @return the claims in the token
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired or has an invalid signature
     */
    public Claims parseAccessToken(String token){
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

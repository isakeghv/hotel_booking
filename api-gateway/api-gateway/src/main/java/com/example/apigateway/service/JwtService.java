package com.example.apigateway.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

/**
 * Service for validating and reading JWT access tokens, using the HMAC
 * secret configured in {@code ACCESS_SECRET}.
 */
@Service
public class JwtService {
    @Value("${ACCESS_SECRET}")
    private String accessSecret;

    private SecretKey key;


    /**
     * Decodes the Base64 access secret and builds the signing key. Runs once
     * after construction.
     */
    @PostConstruct
    private void init(){
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
    }

    /**
     * Verifies the signature of an access token and returns its claims.
     *
     * @param token the compact JWT string, without the {@code Bearer } prefix
     * @return the claims contained in the token
     * @throws io.jsonwebtoken.JwtException if the token is malformed, expired,
     *         or has an invalid signature
     */
    public Claims parseAccessToken(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

package com.example.apigateway.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

/**
 * Service for tracking revoked (signed-out) sessions in Redis, so that
 * access tokens for those sessions are rejected until they would have
 * expired anyway.
 */
@Service
public class TokenBlocklistService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    /**
     * Creates the service with the Redis template used for the blocklist.
     *
     * @param redisTemplate the template backed by the blocklist Redis instance
     */
    public TokenBlocklistService(@Qualifier("blocklistRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate){
        this.redisTemplate = redisTemplate;
    }

    /**
     * Checks whether a session has been revoked.
     *
     * @param sessionId the session id to check
     * @return a Mono emitting true if the session is blocklisted, false otherwise
     */
    public Mono<Boolean> isRevoked(String sessionId){
        return redisTemplate.hasKey(sessionId);
    }

    /**
     * Adds a session to the blocklist. The entry expires when the access token
     * would have expired, so the blocklist doesn't grow indefinitely.
     *
     * @param sessionId  the session id to revoke
     * @param expiration the expiration time of the access token
     * @return a Mono emitting true if the entry was stored
     */
    public Mono<Boolean> blockSession(String sessionId, Date expiration){
        long ttlSeconds = Math.max(0, (expiration.getTime() - System.currentTimeMillis()) / 1000);
        return redisTemplate.opsForValue().set(sessionId, "revoked", Duration.ofSeconds(ttlSeconds));
    }
}

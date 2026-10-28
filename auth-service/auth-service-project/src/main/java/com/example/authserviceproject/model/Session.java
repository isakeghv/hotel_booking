package com.example.authserviceproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a signed-in session, stored in the {@code user_sessions} table. A session is valid for 7 days after it is created.
 */
@Entity
@Table (name = "user_sessions")
public class Session {
    @Id
    private UUID sessionId;
    private UUID userId;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;

    /**
     * Creates an empty session. Required by JPA.
     */
    public Session(){}

    /**
     * Creates a new session for a user with a random session id. The issue time is now and the expiration is 7 days later.
     *
     * @param userId the id of the user the session belongs to
     */
    public Session(UUID userId){
        this.sessionId = UUID.randomUUID();
        this.userId = userId;
        this.issuedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(7);
    }

    /**
     * Sets the id of the user the session belongs to.
     *
     * @param userId the user id
     */
    public void setUserId(UUID userId) { this.userId = userId;  }

    /**
     * Returns the id of the user the session belongs to.
     *
     * @return the user id
     */
    public UUID getUserId() { return userId; }

    /**
     * Returns the unique id of the session.
     *
     * @return the session id
     */
    public UUID getSessionId() { return sessionId; }

    /**
     * Returns the time the session was issued.
     *
     * @return the issue time
     */
    public LocalDateTime getIssuedAt() { return issuedAt; }

    /**
     * Returns the time the session expires.
     *
     * @return the expiration time
     */
    public LocalDateTime getExpiresAt() { return expiresAt; }
}

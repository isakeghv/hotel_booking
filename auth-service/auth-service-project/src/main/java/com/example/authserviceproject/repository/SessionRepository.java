package com.example.authserviceproject.repository;

import com.example.authserviceproject.model.Session;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Session} entities.
 */
public interface SessionRepository extends JpaRepository<Session, UUID> {

    /**
     * Finds a session by its id, restricted to a specific user.
     *
     * @param sessionId the session id
     * @param userId the id of the user the session must belong to
     * @return the session, or empty if none matches both ids
     */
    Optional<Session> findBySessionIdAndUserId(UUID sessionId, UUID userId);

    /**
     * Finds all sessions belonging to a user.
     *
     * @param userId the user id
     * @return the user's sessions, empty if there are none
     */
    List<Session> findByUserId(UUID userId);

    /**
     * Deletes a session by its id, restricted to a specific user.
     *
     * @param sessionId the session id
     * @param userId the id of the user the session must belong to
     * @return the number of deleted sessions
     */
    @Transactional
    int deleteBySessionIdAndUserId(UUID sessionId, UUID userId);

    /**
     * Deletes all sessions belonging to a user.
     *
     * @param userId the user id
     * @return the number of deleted sessions
     */
    @Transactional
    int deleteByUserId(UUID userId);
}

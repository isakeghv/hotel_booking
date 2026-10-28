package com.example.authserviceproject.repository;

import com.example.authserviceproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entities.
 */
public interface UserRepository extends JpaRepository<User, UUID>{
    /**
     * Finds a user by email address.
     *
     * @param email the email address
     * @return the user, or empty if none has that email
     */
    Optional<User> findByCredentials_Email(String email);

    /**
     * Finds a user by id.
     *
     * @param userId the user id
     * @return the user, or empty if none exists
     */
    Optional<User> findByUserId(UUID userId);

    /**
     * Checks whether a user with the given email address exists.
     *
     * @param email the email address
     * @return true if a user with that email exists
     */
    boolean existsByCredentials_Email(String email);
}

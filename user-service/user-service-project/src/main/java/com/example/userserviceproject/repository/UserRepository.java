package com.example.userserviceproject.repository;

import com.example.userserviceproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link User} entities, keyed by the user's {@link UUID}.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Finds a user by their user id.
     *
     * @param userId the id of the user to look up
     * @return an {@link Optional} containing the user, or empty if no user has
     *         the given id
     */
    Optional<User> findByUserId(UUID userId);

    /**
     * To find several users by userId
     *
     * @param userIds the id's of the user to find
     * @return a {@link List} containing the found users
     */
    List<User> findAllByUserIdIn(List<UUID> userIds);
}

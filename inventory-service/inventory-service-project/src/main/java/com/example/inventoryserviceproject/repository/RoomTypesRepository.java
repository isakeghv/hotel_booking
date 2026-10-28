package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link RoomType} entities.
 */
public interface RoomTypesRepository extends JpaRepository<RoomType, UUID> {

    /**
     * Finds a room type by its id.
     *
     * @param typeId the id of the room type
     * @return the room type, or empty if none exists
     */
    Optional<RoomType> findByTypeId(UUID typeId);
}

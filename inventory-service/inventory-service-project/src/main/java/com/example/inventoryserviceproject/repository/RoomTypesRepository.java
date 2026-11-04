package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link RoomType} entities.
 */
public interface RoomTypesRepository extends JpaRepository<RoomType, UUID> {

    /**
     * Finds a room type by its id.
     *
     * @param roomTypeId the id of the room type
     * @return the room type, or empty if none exists
     */
    Optional<RoomType> findByRoomTypeId(UUID roomTypeId);

    /**
     * Finds all active room types.
     *
     * @return the active room types, or an empty list if there are none
     */
    List<RoomType> findAllByIsActiveTrue();
}

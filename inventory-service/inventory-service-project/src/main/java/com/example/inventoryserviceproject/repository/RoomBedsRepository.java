package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.RoomBed;
import com.example.inventoryserviceproject.model.RoomBedsId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link RoomBed} entities, which link beds to room types.
 */
public interface RoomBedsRepository extends JpaRepository<RoomBed, RoomBedsId> {
    /**
     * Finds all beds links for a room type, matched on the room type id in the
     * composite key.
     *
     * @param roomId the id of the room type
     * @return the bed links for the room type, or an empty list if there are none
     */
    List<RoomBed> findByIdTypeId(UUID roomId);
}

package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;
import java.util.UUID;

/**
 * Repository for {@link Room} entities.
 */
public interface RoomRepository extends JpaRepository<Room, UUID> {

    /**
     * Checks whether a room with the given room number exists.
     *
     * @param roomNumber the room number
     * @return true if such a room exists, false otherwise
     */
    boolean existsByRoomNumber(int roomNumber);

    /**
     * Finds the active rooms of a room type.
     *
     * @param roomTypeId the id of the room type
     * @return the rooms sorted by room number, or an empty list if there are none
     */
    List<Room> findAllByRoomTypeIdAndIsActiveTrueOrderByRoomNumberAsc(UUID roomTypeId);

    /**
     * Finds the active rooms of the given room types.
     *
     * @param roomTypeIds the ids of the room types
     * @return the active rooms, or an empty list if there are none
     */
    List<Room> findAllByRoomTypeIdInAndIsActiveTrue(Collection<UUID> roomTypeIds);
}

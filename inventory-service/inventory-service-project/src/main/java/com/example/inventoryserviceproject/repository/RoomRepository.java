package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

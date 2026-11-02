package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for {@link Room} entities. Only the standard CRUD operations
 * are used.
 */
public interface RoomRepository extends JpaRepository<Room, UUID> {
}

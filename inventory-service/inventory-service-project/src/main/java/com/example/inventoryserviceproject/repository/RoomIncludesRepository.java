package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.RoomBed;
import com.example.inventoryserviceproject.model.RoomBedsId;
import com.example.inventoryserviceproject.model.RoomIncludes;
import com.example.inventoryserviceproject.model.RoomIncludesId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link RoomIncludes} entities, which link extras to room
 * types. Only the standard CRUD operations are used.
 */
public interface RoomIncludesRepository  extends JpaRepository<RoomIncludes, RoomIncludesId> {
}

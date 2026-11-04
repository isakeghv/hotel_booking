package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.RoomInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link RoomInformation} entities.
 */
public interface RoomInformationRepository extends JpaRepository<RoomInformation, UUID> {

    /**
     * Finds the main descriptions of the given room types. Each room type has at most one.
     *
     * @param roomTypeIds the ids of the room types
     * @return the main descriptions, or an empty list if there are none
     */
    List<RoomInformation> findAllByRoomTypeIdInAndIsMainDescriptionTrue(Collection<UUID> roomTypeIds);
}

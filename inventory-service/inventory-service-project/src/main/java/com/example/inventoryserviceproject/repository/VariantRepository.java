package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.RoomTypeVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link RoomTypeVariant} entities.
 */
public interface VariantRepository extends JpaRepository<RoomTypeVariant, UUID> {

    /**
     * Checks whether a room type already has a variant with the given name.
     *
     * @param variantName the name of the variant
     * @param roomTypeId      the id of the room type
     * @return true if such a variant exists, false otherwise
     */
    boolean existsByVariantNameAndRoomTypeId(String variantName, UUID roomTypeId);

    /**
     * Finds a variant by its id, with its room type fetched in the same query.
     *
     * @param variantId the id of the variant
     * @return the variant, or empty if none exists
     */
    @EntityGraph(attributePaths = {"roomType"})
    Optional<RoomTypeVariant> findWithRoomTypeByVariantId(UUID variantId);

    /**
     * Finds the active variants of the given room types.
     *
     * @param roomTypeIds the ids of the room types
     * @return the active variants, or an empty list if there are none
     */
    List<RoomTypeVariant> findAllByRoomTypeIdInAndIsActiveTrue(Collection<UUID> roomTypeIds);

    /**
     * Finds the active variants of a room type.
     *
     * @param roomTypeId the id of the room type
     * @return the active variants, or an empty list if there are none
     */
    List<RoomTypeVariant> findAllByRoomTypeIdAndIsActiveTrue(UUID roomTypeId);
}

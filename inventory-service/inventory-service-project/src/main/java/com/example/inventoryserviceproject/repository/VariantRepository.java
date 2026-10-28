package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.RoomTypeVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for {@link RoomTypeVariant} entities.
 */
public interface VariantRepository extends JpaRepository<RoomTypeVariant, UUID> {

    /**
     * Checks whether a room type already has a variant with the given name.
     *
     * @param variantName the name of the variant
     * @param typeId      the id of the room type
     * @return true if such a variant exists, false otherwise
     */
    boolean existsByVariantNameAndTypeId(String variantName, UUID typeId);
}

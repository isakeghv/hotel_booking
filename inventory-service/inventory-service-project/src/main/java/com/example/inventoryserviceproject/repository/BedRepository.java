package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.Bed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Bed} entities.
 */
public interface BedRepository extends JpaRepository<Bed, UUID> {
    /**
     * Finds a bed type by its id.
     *
     * @param typeId the id of the bed type
     * @return the bed type, or empty if none exists
     */
    Optional<Bed> findByBedId(UUID typeId);
}

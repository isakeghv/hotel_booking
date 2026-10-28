package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.Extra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link Extra} entities.
 */
public interface ExtraRepository extends JpaRepository<Extra, UUID> {
    /**
     * Finds an extra by its id.
     *
     * @param extraId the id of the extra
     * @return the extra, or empty if none exists
     */
    Optional<Extra> findExtraByExtraId(UUID extraId);
}

package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.VariantInformation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link VariantInformation} entities.
 */
public interface VariantInformationRepository extends JpaRepository<VariantInformation, UUID> {

    /**
     * Finds the main descriptions of the given variants. Each variant has at most one.
     *
     * @param variantIds the ids of the variants
     * @return the main descriptions, or an empty list if there are none
     */
    List<VariantInformation> findAllByVariantIdInAndIsMainDescriptionTrue(Collection<UUID> variantIds);
}

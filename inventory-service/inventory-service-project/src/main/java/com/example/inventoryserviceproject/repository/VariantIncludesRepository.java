package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.VariantIncludes;
import com.example.inventoryserviceproject.model.VariantIncludesId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link VariantIncludes} entities, which link extras to room
 * type variants.
 */
public interface VariantIncludesRepository extends JpaRepository<VariantIncludes, VariantIncludesId> {

    /**
     * Finds the extras included with the given variants, matched on the
     * variant id in the composite key.
     *
     * @param variantIds the ids of the variants
     * @return the links to the included extras, or an empty list if there are none
     */
    List<VariantIncludes> findAllByIdVariantIdIn(Collection<UUID> variantIds);
}

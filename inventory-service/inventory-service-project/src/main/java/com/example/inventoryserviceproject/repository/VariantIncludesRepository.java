package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.VariantIncludes;
import com.example.inventoryserviceproject.model.VariantIncludesId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link VariantIncludes} entities, which link extras to room
 * type variants. Only the standard CRUD operations are used.
 */
public interface VariantIncludesRepository extends JpaRepository<VariantIncludes, VariantIncludesId> {
}

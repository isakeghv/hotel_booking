package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.ImageData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link ImageData} entities.
 */
public interface ImageRepository extends JpaRepository<ImageData, UUID> {

    /**
     * Finds all images belonging to a room type.
     *
     * @param roomTypeId the id of the room type
     * @return the images for the room type, or an empty list if there are none
     */
    List<ImageData> findAllByTypeId(UUID roomTypeId);
}

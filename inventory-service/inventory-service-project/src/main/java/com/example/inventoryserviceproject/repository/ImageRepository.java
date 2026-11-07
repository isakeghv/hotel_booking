package com.example.inventoryserviceproject.repository;

import com.example.inventoryserviceproject.model.ImageData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for {@link ImageData} entities.
 */
public interface ImageRepository extends JpaRepository<ImageData, UUID> {

    /**
     * Finds all images belonging to a room type, with the primary image first.
     *
     * @param roomTypeId the id of the room type
     * @return the images for the room type, or an empty list if there are none
     */
    List<ImageData> findAllByRoomTypeIdOrderByIsPrimaryDesc(UUID roomTypeId);

    /**
     * Finds the primary image of each of the given room types. Room types
     * without images are not included.
     *
     * @param roomTypeIds the ids of the room types
     * @return the primary images, at most one per room type
     */
    List<ImageData> findAllByRoomTypeIdInAndIsPrimaryTrue(Collection<UUID> roomTypeIds);

    /**
     * Finds the primary image of a room type.
     *
     * @param roomTypeId the id of the room type
     * @return the primary image, or empty if the room type has no images
     */
    Optional<ImageData> findByRoomTypeIdAndIsPrimaryTrue(UUID roomTypeId);

    /**
     * Finds an image, only if it belongs to the given room type.
     *
     * @param imageId    the id of the image
     * @param roomTypeId the id of the room type
     * @return the image, or empty if it does not exist or belongs to another room type
     */
    Optional<ImageData> findByImageIdAndRoomTypeId(UUID imageId, UUID roomTypeId);

    /**
     * Checks whether a room type has any images.
     *
     * @param roomTypeId the id of the room type
     * @return true if the room type has at least one image
     */
    boolean existsByRoomTypeId(UUID roomTypeId);
}

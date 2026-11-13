package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.request.ReviewImageData;
import com.example.inventoryserviceproject.dto.response.PrimaryImageResponse;
import com.example.inventoryserviceproject.exception.ConflictException;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.exception.UnsupportedMediaException;
import com.example.inventoryserviceproject.mapper.PrimaryImageResponseMapper;
import com.example.inventoryserviceproject.model.ImageData;
import com.example.inventoryserviceproject.model.RoomType;
import com.example.inventoryserviceproject.repository.ImageRepository;
import com.example.inventoryserviceproject.repository.RoomTypesRepository;
import com.example.inventoryserviceproject.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for uploading room type images. The image file is sent to the
 * image microservice, while its metadata is stored in this service's
 * database.
 *
 * <p>Each room type with images has exactly one primary image, shown when the
 * room type is listed. The first image of a room type is always made primary.
 * Making another image primary unsets the previous one. The database allows
 * at most one primary image per room type, so two requests changing it at the
 * same moment cannot both succeed: the second is refused with a conflict.
 */
@Service
public class ImageService {
    RoomTypesRepository roomTypesRepository;
    ImageRepository imageRepository;
    RestTemplate restTemplate;
    TransactionTemplate transactionTemplate;
    PrimaryImageResponseMapper primaryImageResponseMapper;
    @Value("${image.service.url}") String imageServiceUrl;

    /**
     * Creates the service with the repositories and HTTP client it uses.
     *
     * @param roomTypesRepository repository for room types
     * @param imageRepository     repository for image metadata
     * @param restTemplate        HTTP client used to call the image service
     * @param transactionTemplate runs the database changes in one transaction, without the call to the image service
     */
    public ImageService(
            RoomTypesRepository roomTypesRepository,
            ImageRepository imageRepository,
            RestTemplate restTemplate,
            TransactionTemplate transactionTemplate,
            PrimaryImageResponseMapper primaryImageResponseMapper
    ){
        this.roomTypesRepository = roomTypesRepository;
        this.imageRepository = imageRepository;
        this.restTemplate = restTemplate;
        this.transactionTemplate = transactionTemplate;
        this.primaryImageResponseMapper = primaryImageResponseMapper;
    }

    /**
     * Sends an image file to the image microservice as a multipart request.
     *
     * @param filename the file name to store the image under
     * @param newImage the image file to send
     * @throws InternalServerException if the image service rejects the upload
     *         or cannot be reached
     */
    private void sendToImageMicroService(String filename, MultipartFile newImage){
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("filename", filename);
        body.add("image", newImage.getResource());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(imageServiceUrl, requestEntity, void.class);
        } catch (RestClientResponseException e){
            throw new InternalServerException("Failed to upload image: "+ e.getResponseBodyAsString());
        } catch (RestClientException e){
            throw new InternalServerException("Could not reach services");
        }
    }

    /**
     * Uploads a new image for a room type. Verifies that the room type exists
     * and that the image type is supported, sends the file to the image
     * microservice, and then stores the metadata.
     *
     * <p>The image becomes primary if requested, or if it is the room type's
     * first image (even if not requested), so a room type with images always
     * has a primary image. The previous primary image is then unset.
     *
     * @param newImgData the validated image metadata
     * @param newImage   the image file to upload
     * @param roomTypeId     the id of the room type the image belongs to
     * @return a message confirming the upload
     * @throws NotFoundException         if the room type does not exist
     * @throws UnsupportedMediaException if the image type is not supported
     * @throws ConflictException         if another request changed the primary image at the same moment
     * @throws InternalServerException   if the upload or the saving of metadata fails
     */
    public String uploadNewImage(
            ReviewImageData newImgData,
            MultipartFile newImage,
            UUID roomTypeId
    ){

        Optional<RoomType> optRoomType = roomTypesRepository.findByRoomTypeId(roomTypeId);
        if (optRoomType.isEmpty()) throw new NotFoundException("Room-type not found");

        String imageExtension = ImageUtils.getImageExtension(newImage);
        if ( imageExtension == null) throw new UnsupportedMediaException("Image-type not supported");

        // Generates the file name, sent to the image service before the metadata is saved
        ImageData imageData = new ImageData(newImgData, imageExtension, roomTypeId, false);

        // Outside the transaction, so no database connection is held during the HTTP call.
        // If saving the metadata fails afterwards, the file is left in the image service (see notes/01-images.md)
        sendToImageMicroService(imageData.getFileName(), newImage);

        try {
            transactionTemplate.executeWithoutResult(status -> {
                // The first image of a room type is always primary, so a room type with images always has one
                boolean primary = newImgData.primary() || !imageRepository.existsByRoomTypeId(roomTypeId);
                if (primary) unsetPrimaryImage(roomTypeId);

                imageData.setIsPrimary(primary);
                imageRepository.save(imageData);
            });
            return "Image was successfully uploaded.";
        } catch (DataIntegrityViolationException e){
            // Another request made an image of this room type primary at the same moment
            throw new ConflictException("The image could not be saved, please try again");
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error while storing image data");
        }
    }

    /**
     * Makes an image the primary image of its room type, and unsets the
     * previous primary image. Does nothing if the image is already primary.
     *
     * @param roomTypeId the id of the room type
     * @param imageId    the id of the image to make primary
     * @throws NotFoundException       if the image does not exist, or belongs to another room type
     * @throws ConflictException       if another request changed the primary image at the same moment
     * @throws InternalServerException if the change cannot be saved
     */
    public void setPrimaryImage(UUID roomTypeId, UUID imageId){
        try {
            transactionTemplate.executeWithoutResult(status -> {
                ImageData image = imageRepository.findByImageIdAndRoomTypeId(imageId, roomTypeId)
                        .orElseThrow(() -> new NotFoundException("Image not found for this room type"));
                if (image.getIsPrimary()) return;

                unsetPrimaryImage(roomTypeId);
                image.setIsPrimary(true);
                imageRepository.save(image);
            });
        } catch (DataIntegrityViolationException e){
            // Another request made an image of this room type primary at the same moment
            throw new ConflictException("The primary image could not be changed, please try again");
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error while changing the primary image");
        }
    }

    /**
     * Unsets the room type's current primary image, if it has one. Must run
     * inside a transaction, before another image is made primary.
     *
     * <p>The change is flushed right away: Hibernate otherwise writes new rows
     * before updates, so the database would briefly see two primary images
     * and refuse the new one.
     *
     * @param roomTypeId the id of the room type
     */
    private void unsetPrimaryImage(UUID roomTypeId){
        imageRepository.findByRoomTypeIdAndIsPrimaryTrue(roomTypeId).ifPresent(current -> {
            current.setIsPrimary(false);
            imageRepository.saveAndFlush(current);
        });
    }

    /**
     * Retrieves the metadata of all images for a room type, with the primary
     * image first.
     *
     * @param roomTypeId the id of the room type
     * @return the image metadata, or an empty list if there are none
     */
    public List<ImageData> getImageData(UUID roomTypeId){
        return imageRepository.findAllByRoomTypeIdOrderByIsPrimaryDesc(roomTypeId);
    }

    public PrimaryImageResponse getPrimaryImage(UUID roomTypeId){
        Optional<ImageData> optImageData = imageRepository.findByRoomTypeIdAndIsPrimaryTrue(roomTypeId);
        if (optImageData.isEmpty()){
            throw new NotFoundException("Image not found");
        }

        PrimaryImageResponse imageResponse = primaryImageResponseMapper.toResponse(optImageData.get(), roomTypeId);

        return imageResponse;
    }

    /**
     * Retrieves the primary image of each given room type, in one database
     * query. A room type without a primary image (no images, or the room type
     * does not exist) is still returned, with the path and alt-text null.
     *
     * @param roomTypeIds the ids of the room types; duplicates are returned once
     * @return one response per distinct room type id, in the order given
     */
    public List<PrimaryImageResponse> getPrimaryImages(List<UUID> roomTypeIds){
        // Keeps the request order, without duplicates
        Set<UUID> distinctIds = new LinkedHashSet<>(roomTypeIds);

        Map<UUID, ImageData> primaryImageByType = imageRepository.findAllByRoomTypeIdInAndIsPrimaryTrue(distinctIds)
                .stream()
                .collect(Collectors.toMap(ImageData::getRoomTypeId, Function.identity()));

        return distinctIds.stream()
                .map(id -> {
                    ImageData image = primaryImageByType.get(id);
                    return image == null
                            ? primaryImageResponseMapper.toEmptyResponse(id)
                            : primaryImageResponseMapper.toResponse(image, id);
                })
                .toList();
    }
}

package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.ReviewImageData;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.exception.UnsupportedMediaException;
import com.example.inventoryserviceproject.model.ImageData;
import com.example.inventoryserviceproject.model.RoomType;
import com.example.inventoryserviceproject.repository.ImageRepository;
import com.example.inventoryserviceproject.repository.RoomTypesRepository;
import com.example.inventoryserviceproject.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for uploading room type images. The image file is sent to the
 * image microservice, while its metadata is stored in this service's
 * database.
 */
@Service
public class ImageService {
    RoomTypesRepository roomTypesRepository;
    ImageRepository imageRepository;
    RestTemplate restTemplate;
    @Value("${image.service.url}") String imageServiceUrl;

    /**
     * Creates the service with the repositories and HTTP client it uses.
     *
     * @param roomTypesRepository repository for room types
     * @param imageRepository     repository for image metadata
     * @param restTemplate        HTTP client used to call the image service
     */
    public ImageService(RoomTypesRepository roomTypesRepository, ImageRepository imageRepository, RestTemplate restTemplate){
        this.roomTypesRepository = roomTypesRepository;
        this.imageRepository = imageRepository;
        this.restTemplate = restTemplate;
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
     * @param newImgData the validated image metadata
     * @param newImage   the image file to upload
     * @param typeId     the id of the room type the image belongs to
     * @return a message confirming the upload
     * @throws NotFoundException         if the room type does not exist
     * @throws UnsupportedMediaException if the image type is not supported
     * @throws InternalServerException   if the upload or the saving of metadata fails
     */
    public String uploadNewImage(
            ReviewImageData newImgData,
            MultipartFile newImage,
            UUID typeId
    ){

        Optional<RoomType> optRoomType = roomTypesRepository.findByTypeId(typeId);
        if (optRoomType.isEmpty()) throw new NotFoundException("Room-type not found");

        String imageExtension = ImageUtils.getImageExtension(newImage);
        if ( imageExtension == null) throw new UnsupportedMediaException("Image-type not supported");
        ImageData imageData = new ImageData(newImgData, imageExtension, typeId);

        sendToImageMicroService(imageData.getFileName(), newImage);

        try {
            imageRepository.save(imageData);
            return "Image was successfully uploaded.";
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error while storing image data");
        }
    }

    /**
     * Retrieves the metadata of all images for a room type.
     *
     * @param roomTypeId the id of the room type
     * @return the image metadata, or an empty list if there are none
     */
    public List<ImageData> getImageData(UUID roomTypeId){
        return imageRepository.findAllByTypeId(roomTypeId);
    }
}

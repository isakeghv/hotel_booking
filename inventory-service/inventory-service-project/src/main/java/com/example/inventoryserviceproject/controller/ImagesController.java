package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.ReviewImageData;
import com.example.inventoryserviceproject.service.ImageService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for uploading images that belong to room types.
 */
@RestController
@RequestMapping("/inventory")
public class ImagesController {
    private final ImageService imageService;

    /**
     * Creates the controller with the service it delegates to.
     *
     * @param imageService service handling image storage and metadata
     */
    public ImagesController(ImageService imageService){
        this.imageService = imageService;
    }

    /**
     * Uploads an image and attaches it to a room type. The request must be
     * multipart, with the metadata in the {@code data} part and the file in
     * the {@code image} part.
     *
     * @param imgData   the validated metadata describing the image
     * @param newImage  the image file to upload
     * @param roomTypeId the id of the room type the image belongs to
     * @return a response with a message describing the result of the upload
     */
    @PostMapping("/roomtypes/{roomTypeId}/images")
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestPart("data") @Valid ReviewImageData imgData,
            @RequestPart("image") MultipartFile newImage,
            @PathVariable UUID roomTypeId){

        String response = imageService.uploadNewImage(imgData, newImage, roomTypeId);
        return ClientResponse.okMsg(response);
    }
}

package com.example.imageservice.controller;

import com.example.imageservice.service.UploadService;
import com.example.imageservice.utils.ClientResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST controller for uploading image files to storage.
 */
@RestController
@RequestMapping("/images")
public class UploadController {
    private final UploadService uploadService;

    /**
     * Creates the controller with the service used to store images.
     *
     * @param uploadService the service that writes image files to storage
     */
    public UploadController(UploadService uploadService){
        this.uploadService = uploadService;
    }

    /**
     * Uploads a new image under the given filename. An existing file with the
     * same name is replaced.
     *
     * @param filename the name to store the image under
     * @param newImage the uploaded image file
     * @return a 200 response with a body of the form {@code {"success": true}}
     */
    // For uploading an image
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Boolean>> uploadNewImage(
            @RequestPart("filename") String filename,
            @RequestPart("image") MultipartFile newImage
    ){
        boolean success = uploadService.uploadNewImage(filename, newImage);
        return ClientResponse.ok("success", success);
    }
}

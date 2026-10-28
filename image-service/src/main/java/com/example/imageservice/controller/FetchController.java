package com.example.imageservice.controller;

import com.example.imageservice.model.ImageResponse;
import com.example.imageservice.service.FetchService;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for retrieving stored image files.
 */
@RestController
@RequestMapping("/images")
public class FetchController {
    private final FetchService fetchService;

    /**
     * Creates the controller with the service used to fetch images.
     *
     * @param fetchService the service that loads image files from storage
     */
    public FetchController(FetchService fetchService){
        this.fetchService = fetchService;
    }

    /**
     * Returns an image file by its filename, with the content type set from
     * the file itself.
     *
     * @param filename the name of the stored image file
     * @return a 200 response containing the image as the body
     */
    // For fetching an img file
    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getImageFile(@PathVariable String filename){
        ImageResponse response = fetchService.fetchImage(filename);

        return ResponseEntity.ok()
                .contentType(response.getContentType())
                .body(response.getFile());
    }
}

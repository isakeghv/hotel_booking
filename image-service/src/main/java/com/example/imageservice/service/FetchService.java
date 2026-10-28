package com.example.imageservice.service;

import com.example.imageservice.exception.InternalServerException;
import com.example.imageservice.exception.NotFoundException;
import com.example.imageservice.model.ImageResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for loading image files from the configured storage directory
 * ({@code image.storage.path}).
 */
@Service
public class FetchService {
    @Value("${image.storage.path}")
    private String storagePath;

    /**
     * Loads an image file from storage and determines its content type.
     *
     * @param filename the name of the stored image file
     * @return the file together with its content type
     * @throws NotFoundException       if the file does not exist or is not a
     *                                 regular file
     * @throws InternalServerException if the file location is malformed or the
     *                                 content type cannot be determined
     */
    public ImageResponse fetchImage(String filename)  {
        Path filePath = Paths.get(storagePath, filename);
        try {
            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);

            if (!resource.exists() || !resource.isFile()) {
                throw new NotFoundException("Requested file not found");
            }

            return new ImageResponse(contentType, resource);

        } catch (MalformedURLException e){
            throw new InternalServerException("Internal server error: " + e.getLocalizedMessage());
        } catch (IOException e) {
            throw new InternalServerException("Internal runtime server error: " + e.getLocalizedMessage());
        }
    }
}

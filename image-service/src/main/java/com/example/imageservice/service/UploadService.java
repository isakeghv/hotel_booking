package com.example.imageservice.service;

import com.example.imageservice.exception.InternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service for writing uploaded image files to the configured storage
 * directory ({@code image.storage.path}).
 */
@Service
public class UploadService {
    @Value("${image.storage.path}")
    private String storagePath;

    /**
     * Saves an uploaded image to storage under the given filename, replacing
     * any existing file with the same name.
     *
     * @param filename the name to store the image under
     * @param newImage the uploaded image file
     * @return true if the image was saved
     * @throws InternalServerException if the file could not be written
     */
    public boolean uploadNewImage(String filename, MultipartFile newImage){
        try {
            Path targetPath = Paths.get(storagePath, filename);
            Files.copy(newImage.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e){
            throw new InternalServerException(e.getMessage());
        }
    }
}

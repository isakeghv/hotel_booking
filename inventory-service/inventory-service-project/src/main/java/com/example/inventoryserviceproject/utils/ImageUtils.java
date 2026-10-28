package com.example.inventoryserviceproject.utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Utility methods for working with uploaded images.
 */
public class ImageUtils {

    private static final Map<String, String> extensions = Map.of(
            "image/png", "png",
            "image/jpeg", "jpg",
            "image/webp", "webp"
    );

    /**
     * Determines the file extension for an image from its content type.
     * Supported types are PNG, JPEG and WebP.
     *
     * @param image the uploaded image
     * @return the extension without a dot, or null if the content type is
     *         not supported
     */
    public static String getImageExtension(MultipartFile image){
        return extensions.get(image.getContentType());
    }
}

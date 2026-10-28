package com.example.imageservice.model;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

/**
 * Holder for an image file and its media type, returned by the fetch
 * service to the controller.
 */
public class ImageResponse {
    private final MediaType contentType;
    private final Resource file;

    /**
     * Creates a response from a content type string and a file.
     *
     * @param contentType the content type, e.g. {@code image/png}
     * @param file        the image file resource
     * @throws org.springframework.http.InvalidMediaTypeException if the content
     *         type string is not a valid media type
     */
    public ImageResponse(String contentType, Resource file){
        this.contentType = MediaType.parseMediaType(contentType);
        this.file = file;
    }

    /**
     * Returns the image file.
     *
     * @return the image file resource
     */
    public Resource getFile() {
        return file;
    }

    /**
     * Returns the media type of the image.
     *
     * @return the content type, e.g. {@code image/png}
     */
    public MediaType getContentType() {
        return contentType;
    }
}

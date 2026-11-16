package com.example.bookingserviceproject.dto.response;

/**
 * An image from the inventory-service. Both fields are null if there is no
 * image, or it could not be fetched, so the property itself is never missing.
 *
 * @param filePath the full URL of the image, or null
 * @param altText  the text describing the image, or null
 */
public record ImageResponse(String filePath, String altText) {

    /** No image: both fields null. */
    public static final ImageResponse NONE = new ImageResponse(null, null);
}

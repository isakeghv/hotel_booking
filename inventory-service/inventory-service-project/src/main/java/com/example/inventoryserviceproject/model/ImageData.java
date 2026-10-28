package com.example.inventoryserviceproject.model;

import com.example.inventoryserviceproject.dto.ReviewImageData;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity for the metadata of an image belonging to a room type. The image
 * file itself is stored by the image service.
 */
@Entity
@Table(name = "room_images")
public class ImageData {
    @Id
    private UUID imageId;
    private UUID typeId;
    private String fileName;
    private String imageText;
    private String altText;

    /**
     * Creates an empty image record. Required by JPA.
     */
    public ImageData(){}

    /**
     * Creates a new image record from validated metadata. A random id is
     * generated, and the file name is built from that id and the file extension.
     *
     * @param imgData       the validated image metadata
     * @param fileExtension the file extension without a dot, e.g. {@code png}
     * @param typeId        the id of the room type the image belongs to
     */
    public ImageData(ReviewImageData imgData, String fileExtension, UUID typeId){
        UUID imageId = UUID.randomUUID();

        this.imageId = imageId;
        this.typeId = typeId;
        this.fileName = imageId + "." + fileExtension;
        this.imageText = imgData.imageText();
        this.altText = imgData.altText();
    }

    /**
     * @return the unique id of the image
     */
    public UUID getImageId() { return imageId; }

    /**
     * @param imageId the unique id of the image
     */
    public void setImageId(UUID imageId) { this.imageId = imageId; }

    /**
     * @return the id of the room type the image belongs to
     */
    public UUID getTypeId() { return typeId; }

    /**
     * @param typeId the id of the room type the image belongs to
     */
    public void setTypeId(UUID typeId) { this.typeId = typeId; }

    /**
     * @return the stored file name, including extension
     */
    public String getFileName() { return fileName; }

    /**
     * @param fileName the stored file name, including extension
     */
    public void setFileName(String fileName) { this.fileName = fileName; }

    /**
     * @return the caption text shown with the image
     */
    public String getImageText() { return imageText; }

    /**
     * @param imageText the caption text shown with the image
     */
    public void setImageText(String imageText) { this.imageText = imageText; }

    /**
     * @return the alternative text describing the image
     */
    public String getAltText() { return altText; }

    /**
     * @param altText the alternative text describing the image
     */
    public void setAltText(String altText) { this.altText = altText; }
}

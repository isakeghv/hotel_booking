package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.request.ReviewImageData;
import com.example.inventoryserviceproject.dto.response.Envelopes;
import com.example.inventoryserviceproject.service.ImageService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST controller for uploading images that belong to room types, and for
 * choosing which image is a room type's primary image.
 */
@Tag(name = "Images")
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
    @Operation(
            summary = "Upload an image of a room type",
            description = """
                    **Access: Manager.**

                    Multipart request: the metadata as JSON in the `data` part, and the file in the `image` part. \
                    The file is stored by the image-service. One image per request.

                    **Primary image:** each room type with images has exactly one primary image, shown when the room type \
                    is listed (e.g. in the availability search).
                    - `primary: true` makes the new image primary, and the previous primary image is unset.
                    - `primary` is optional and defaults to `false`.
                    - **The first image of a room type is always made primary**, even with `primary: false`.

                    **Response key:** `message`.
                    """)
    @ApiResponse(responseCode = "200", description = "The image was uploaded")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type does not exist")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "Another request changed the room type's primary image at the same moment. Try again")
    @ApiResponse(responseCode = "415", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The file type is not supported")
    @PostMapping("/roomtypes/{roomTypeId}/images")
    public ResponseEntity<Envelopes.MessageResult> uploadImage(
            @RequestPart("data") @Valid ReviewImageData imgData,
            @RequestPart("image") MultipartFile newImage,
            @PathVariable UUID roomTypeId){

        String response = imageService.uploadNewImage(imgData, newImage, roomTypeId);
        return ClientResponse.ok(new Envelopes.MessageResult(response));
    }

    /**
     * Makes an image the primary image of its room type. The previous primary
     * image is unset.
     *
     * @param roomTypeId the id of the room type
     * @param imageId    the id of the image to make primary
     * @return an empty response
     */
    @Operation(
            summary = "Make an image the room type's primary image",
            description = """
                    **Access: Manager.**

                    The primary image is shown when the room type is listed (e.g. in the availability search). \
                    The previous primary image is unset automatically. If the image is already primary, nothing changes.

                    There is no way to unset a primary image without choosing another one: a room type with images \
                    always has a primary image.
                    """)
    @ApiResponse(responseCode = "204", content = @Content, description = "The image is now the primary image")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The image does not exist, or belongs to another room type")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "Another request changed the primary image at the same moment. Try again")
    @PutMapping("/roomtypes/{roomTypeId}/images/{imageId}/primary")
    public ResponseEntity<Void> setPrimaryImage(@PathVariable UUID roomTypeId, @PathVariable UUID imageId){
        imageService.setPrimaryImage(roomTypeId, imageId);
        return ClientResponse.okEmpty();
    }
}

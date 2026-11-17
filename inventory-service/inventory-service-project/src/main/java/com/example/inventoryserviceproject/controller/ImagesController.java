package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.request.ReviewImageData;
import com.example.inventoryserviceproject.dto.request.ReviewPrimaryImagesQuery;
import com.example.inventoryserviceproject.dto.response.Envelopes;
import com.example.inventoryserviceproject.dto.response.PrimaryImageResponse;
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

import java.util.List;
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

    /**
     * Returns the primary image of a room type.
     *
     * @param roomTypeId the id of the room type
     * @return a response with the image-path and alt-text
     */
    @Operation(
            summary = "Get the primary image of a room type",
            description = """
                    **Access: Internal.**

                    The primary image is the one shown when the room type is listed. \
                    For several room types at once, use `POST /inventory/roomtypes/images/primary`.

                    **Response key:** `primaryImageResponse`.
                    """)
    @ApiResponse(responseCode = "200", description = "The primary image")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type has no images, or does not exist")
    @GetMapping("/roomtypes/{roomTypeId}/image/primary")
    public ResponseEntity<Envelopes.PrimaryImageResult> getPrimaryImage(@PathVariable UUID roomTypeId){
        PrimaryImageResponse imageResponse = imageService.getPrimaryImage(roomTypeId);

        return ClientResponse.ok(new Envelopes.PrimaryImageResult(imageResponse));
    }

    /**
     * Returns the primary image of each given room type. Room types without a
     * primary image are included, with the image-path and alt-text null.
     *
     * @param request the validated room type ids, at most 100
     * @return a response with one entry per distinct room type id
     */
    @Operation(
            summary = "Get the primary image of several room types",
            description = """
                    **Access: Internal.**

                    Takes up to 100 room type ids in the body. Returns one entry per distinct id, in the order given.

                    A room type without a primary image, or one that does not exist, is **not an error**: \
                    it is returned with `filePath` and `altText` set to `null`.

                    **Response key:** `primaryImages`.
                    """)
    @ApiResponse(responseCode = "200", description = "The primary images")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input: no ids, more than 100, or an id that is null or not a valid UUID")
    @PostMapping("/roomtypes/images/primary")
    public ResponseEntity<Envelopes.PrimaryImagesResult> getPrimaryImages(@Valid @RequestBody ReviewPrimaryImagesQuery request){
        List<PrimaryImageResponse> images = imageService.getPrimaryImages(request.roomTypeIds());

        return ClientResponse.ok(new Envelopes.PrimaryImagesResult(images));
    }
}

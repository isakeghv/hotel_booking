package com.example.inventoryserviceproject.controller;


import com.example.inventoryserviceproject.dto.request.ReviewLinkExtraToVariant;
import com.example.inventoryserviceproject.dto.request.ReviewNewRoomVariant;
import com.example.inventoryserviceproject.dto.request.ReviewVariantQuery;
import com.example.inventoryserviceproject.dto.response.RoomTypeVariantsResponse;
import com.example.inventoryserviceproject.dto.response.Envelopes;
import com.example.inventoryserviceproject.service.VariantService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for managing room type variants.
 */
@Tag(name = "Variants")
@RestController
@RequestMapping("/inventory")
public class VariantController {
    private final VariantService variantService;

    /**
     * Creates the controller with the service it delegates to.
     *
     * @param variantService service containing the variant business logic
     */
    public VariantController(VariantService variantService){
        this.variantService = variantService;
    }

    /**
     * Creates a new variant of an existing room type.
     *
     * @param request    the validated details of the new variant
     * @param roomTypeId the id of the room type the variant belongs to
     * @return an empty successful response
     */
    @Operation(
            summary = "Create a variant of a room type",
            description = """
                    **Access: Manager.**

                    Adds an option to a room type, e.g. "With breakfast". `extraPrice` is added to the room's price: \
                    per night, or once per stay if `isPriceFixed` is true. The name must be unique within the room type.
                    """)
    @ApiResponse(responseCode = "204", content = @Content, description = "The variant was created")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type does not exist")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type already has a variant with this name")
    @PostMapping("/roomtypes/{roomTypeId}/variants")
    public ResponseEntity<Void> createRoomTypeVariant(
            @Valid @RequestBody ReviewNewRoomVariant request,
            @PathVariable UUID roomTypeId
    ){
        variantService.createRoomTypeVariant(request, roomTypeId);

        return ClientResponse.okEmpty();
    }

    /**
     * Links an existing extra to a variant, so it is included with the variant.
     *
     * @param request   the validated amount and visibility of the extra
     * @param variantId the id of the variant
     * @param extrasId  the id of the extra to link
     * @return an empty successful response
     */
    @Operation(
            summary = "Include an extra in a variant",
            description = """
                    **Access: Manager.**

                    Includes an extra in a variant, e.g. breakfast in "With breakfast". `showAmount` decides whether \
                    customers see the amount ("2 × Parking") or only the name ("Breakfast").
                    """)
    @ApiResponse(responseCode = "204", content = @Content, description = "The extra was included")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The variant or the extra does not exist")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The extra is already included in the variant")
    @PostMapping("/variants/{variantId}/extras/{extrasId}")
    public ResponseEntity<Void> linkExtraToVariant(
            @Valid @RequestBody ReviewLinkExtraToVariant request,
            @PathVariable UUID variantId,
            @PathVariable UUID extrasId
    ){
        variantService.linkExtraToVariant(request, variantId, extrasId);

        return ClientResponse.okEmpty();
    }

    /**
     * Retrieves the active variants of a room type, priced for a stay and
     * number of rooms. Public endpoint: customers use it to choose a variant
     * after choosing a room type in the search.
     *
     * @param roomTypeId the id of the room type
     * @param query      the validated query: number of rooms, check-in and check-out dates
     * @return a response with the variants, lowest total first
     */
    @Operation(
            summary = "List the variants of a room type, with prices",
            description = """
                    **Access: Public.**

                    Lists the options a customer can choose between after picking a room type in the search, priced \
                    for the stay and number of rooms. Only what is specific to each variant is returned; the room type \
                    itself comes from the room type and search endpoints.

                    `total` is the full price for all rooms for the whole stay, room and variant together. \
                    `pricePerNight` is `total / nights`, rounded up, so it adds up also when a variant has a price \
                    per stay. Sorted by `total`, cheapest first.

                    **Response key:** `variants`.
                    """)
    @ApiResponse(responseCode = "200", description = "The variants, cheapest first")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type does not exist, is not active, or has no active variants")
    @GetMapping("/roomtypes/{roomTypeId}/variants")
    public ResponseEntity<Envelopes.VariantsResult> getRoomTypeVariants(
            @PathVariable UUID roomTypeId,
            @Valid @ParameterObject @ModelAttribute ReviewVariantQuery query
    ){
        RoomTypeVariantsResponse variants = variantService.getRoomTypeVariants(roomTypeId, query);
        return ClientResponse.ok(new Envelopes.VariantsResult(variants));
    }
}

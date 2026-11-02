package com.example.inventoryserviceproject.controller;


import com.example.inventoryserviceproject.dto.request.ReviewLinkExtraToVariant;
import com.example.inventoryserviceproject.dto.request.ReviewNewRoomVariant;
import com.example.inventoryserviceproject.service.VariantService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing room type variants.
 */
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
    @PostMapping("/roomtypes/{roomTypeId}/variants")
    public ResponseEntity<Map<String, String>> createRoomTypeVariant(
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
    @PostMapping("/variants/{variantId}/extras/{extrasId}")
    public ResponseEntity<Map<String, String>> linkExtraToVariant(
            @Valid @RequestBody ReviewLinkExtraToVariant request,
            @PathVariable UUID variantId,
            @PathVariable UUID extrasId
    ){
        variantService.linkExtraToVariant(request, variantId, extrasId);

        return ClientResponse.okEmpty();
    }
}

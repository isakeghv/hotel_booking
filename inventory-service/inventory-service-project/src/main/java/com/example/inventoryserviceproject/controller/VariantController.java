package com.example.inventoryserviceproject.controller;


import com.example.inventoryserviceproject.dto.ReviewNewRoomVariant;
import com.example.inventoryserviceproject.service.VariantService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
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
}

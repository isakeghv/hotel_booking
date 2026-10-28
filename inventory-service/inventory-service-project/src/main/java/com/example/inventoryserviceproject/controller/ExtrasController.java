package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.ReviewNewExtra;
import com.example.inventoryserviceproject.model.Extra;
import com.example.inventoryserviceproject.service.ExtrasService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing extras, the add-on items that can be
 * offered together with a room.
 */
@RestController
@RequestMapping("/inventory")
public class ExtrasController {
    private final ExtrasService extrasService;

    /**
     * Creates the controller with the service it delegates to.
     *
     * @param extrasService service containing the extras business logic
     */
    public ExtrasController(ExtrasService extrasService){
        this.extrasService = extrasService;
    }

    /**
     * Adds a new extra to the inventory.
     *
     * @param request the validated details of the new extra
     * @return a response with the created extra
     */
    @PostMapping("/extras")
    public ResponseEntity<Map<String, Extra>> createExtraController(@Valid @RequestBody ReviewNewExtra request){
        Extra newExtra = extrasService.createNewExtra(request);
        return ClientResponse.created("extra", newExtra);
    }

    /**
     * Retrieves all available extras.
     *
     * @return a response containing the list of extras
     */
    @GetMapping("/extras")
    public ResponseEntity<Map<String, List<Extra>>> getAllExtras(){
        List<Extra> extras = extrasService.getAllExtras();
        return ClientResponse.ok("extras", extras);
    }
}

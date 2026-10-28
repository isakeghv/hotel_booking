package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.ReviewNewBedType;
import com.example.inventoryserviceproject.model.Bed;
import com.example.inventoryserviceproject.service.BedService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing the hotel's bed types, such as creating a new
 * bed type and listing the available ones.
 */
@RestController
@RequestMapping("/inventory")
public class BedController {
    private final BedService bedService;

    /**
     * Creates the controller with the service it delegates to.
     *
     * @param bedService service containing the bed business logic
     */
    public BedController(BedService bedService){
        this.bedService = bedService;
    }

    /**
     * Adds a new bed type to the inventory.
     *
     * @param request the validated details of the new bed type
     * @return a response with the created bed type
     */
    @PostMapping("/beds")
    public ResponseEntity<Map<String, Bed>> createBedController(@Valid @RequestBody ReviewNewBedType request){
        Bed bed = bedService.createNewBed(request);
        return ClientResponse.created("created", bed);
    }

    /**
     * Retrieves all available bed types.
     *
     * @return a response containing the list of bed types
     */
    @GetMapping("/beds")
    public ResponseEntity<Map<String, List<Bed>>> getAllBedsController(){
        List<Bed> beds = bedService.getAllBeds();
        return ClientResponse.ok("bedTypes", beds);
    }
}

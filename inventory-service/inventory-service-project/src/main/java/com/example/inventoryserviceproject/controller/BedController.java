package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.request.ReviewNewBedType;
import com.example.inventoryserviceproject.model.Bed;
import com.example.inventoryserviceproject.dto.response.Envelopes;
import com.example.inventoryserviceproject.service.BedService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing the hotel's bed types, such as creating a new
 * bed type and listing the available ones.
 */
@Tag(name = "Beds")
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
    @Operation(
            summary = "Create a bed type",
            description = """
                    **Access: Manager.**

                    Adds a bed type, e.g. "Single bed" sleeping 1 person, that room types can be furnished with.

                    **Response key:** `created`.
                    """)
    @ApiResponse(responseCode = "201", description = "The created bed type")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @PostMapping("/beds")
    public ResponseEntity<Envelopes.CreatedBedResult> createBedController(@Valid @RequestBody ReviewNewBedType request){
        Bed bed = bedService.createNewBed(request);
        return ClientResponse.created(new Envelopes.CreatedBedResult(bed));
    }

    /**
     * Retrieves all available bed types.
     *
     * @return a response containing the list of bed types
     */
    @Operation(
            summary = "List bed types",
            description = """
                    **Access: Manager.**

                    **Response key:** `bedTypes`.
                    """)
    @ApiResponse(responseCode = "200", description = "All bed types")
    @GetMapping("/beds")
    public ResponseEntity<Envelopes.BedTypesResult> getAllBedsController(){
        List<Bed> beds = bedService.getAllBeds();
        return ClientResponse.ok(new Envelopes.BedTypesResult(beds));
    }
}

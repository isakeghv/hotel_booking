package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.request.ReviewNewExtra;
import com.example.inventoryserviceproject.model.Extra;
import com.example.inventoryserviceproject.dto.response.Envelopes;
import com.example.inventoryserviceproject.service.ExtrasService;
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
 * REST controller for managing extras, the add-on items that can be
 * offered together with a room.
 */
@Tag(name = "Extras")
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
    @Operation(
            summary = "Create an extra",
            description = """
                    **Access: Manager.**

                    Adds an extra, e.g. breakfast. `addonPossible` decides whether customers can buy it separately, \
                    or only get it included in rooms. `isPriceFixed` decides whether the price is per stay or per night.

                    **Response key:** `extra`.
                    """)
    @ApiResponse(responseCode = "201", description = "The created extra")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @PostMapping("/extras")
    public ResponseEntity<Envelopes.CreatedExtraResult> createExtraController(@Valid @RequestBody ReviewNewExtra request){
        Extra newExtra = extrasService.createNewExtra(request);
        return ClientResponse.created(new Envelopes.CreatedExtraResult(newExtra));
    }

    /**
     * Retrieves all available extras.
     *
     * @return a response containing the list of extras
     */
    @Operation(
            summary = "List extras",
            description = """
                    **Access: Customer.**

                    **Response key:** `extras`.
                    """)
    @ApiResponse(responseCode = "200", description = "All extras")
    @GetMapping("/extras")
    public ResponseEntity<Envelopes.ExtrasResult> getAllExtras(){
        List<Extra> extras = extrasService.getAllExtras();
        return ClientResponse.ok(new Envelopes.ExtrasResult(extras));
    }
}

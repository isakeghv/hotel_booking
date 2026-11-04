package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.request.ReviewAvailabilityQuery;
import com.example.inventoryserviceproject.dto.request.ReviewExtraQuery;
import com.example.inventoryserviceproject.dto.request.ReviewSearchQuery;
import com.example.inventoryserviceproject.dto.response.AvailabilityResponse;
import com.example.inventoryserviceproject.dto.response.AvailableRoomTypeResponse;
import com.example.inventoryserviceproject.dto.response.ExtraAvailabilityResponse;
import com.example.inventoryserviceproject.dto.response.Envelopes;
import com.example.inventoryserviceproject.service.AvailabilityService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing availability.
 * Contains endpoints used internally in the docker network by the booking-service,
 * and the public search for available rooms.
 */
@Tag(name = "Availability")
@RestController
@RequestMapping("/inventory")
public class AvailabilityController {
    private final AvailabilityService availabilityService;

    /**
     * Creates the controller with the service it delegates to.
     *
     * @param availabilityService service containing the availability business logic
     */
    public AvailabilityController(AvailabilityService availabilityService){
        this.availabilityService = availabilityService;
    }

    /**
     * Checks whether rooms of a variant are available for the given dates, and
     * if so, holds them in the user's cart for up to 30 minutes. Called by the
     * booking-service when a user adds rooms to their cart.
     *
     * @param variantId the id of the chosen variant
     * @param query     the validated query: amount of rooms, check-in date and check-out date
     * @param userId    the id of the user whose cart the rooms are added to
     * @return a response with the reserved rooms and their prices
     */
    @Operation(
            summary = "Hold rooms of a variant in the user's cart",
            description = """
                    **Access: Internal** (booking-service).

                    Checks that `amount` rooms of the variant's room type are free for the dates, and if so holds them \
                    in the user's cart, so no one else can book them. Either all rooms are held, or none. Rooms are \
                    picked as close to each other as possible, preferably on the same floor as rooms already in the cart.

                    The cart, and everything in it, expires **30 minutes after its first room was added**. Adding more \
                    rooms does not extend it. Each held room gets an `itemId`: store it, as it is used to remove the \
                    room from the cart, and to cancel it after the booking is completed.

                    **Response key:** `reservation`. Prices are only for the rooms held by this request.
                    """)
    @ApiResponse(responseCode = "200", description = "The rooms were held")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The variant, or its room type, does not exist or is not active")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "Not enough rooms are available, or the cart is about to expire")
    @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "Reservations are temporarily unavailable")
    @GetMapping("/availability/variants/{variantId}")
    public ResponseEntity<Envelopes.ReservationResult> reserveVariant(
            @PathVariable UUID variantId,
            @Valid @ParameterObject @ModelAttribute ReviewAvailabilityQuery query,
            @Parameter(description = "The id of the user whose cart the rooms are added to") @RequestHeader("X-User-Id") UUID userId
    ){
        AvailabilityResponse reservation = availabilityService.reserveRooms(userId, variantId, query);
        return ClientResponse.ok(new Envelopes.ReservationResult(reservation));
    }

    /**
     * Checks that an extra can be added to a booking, and prices it for the
     * stay in the user's cart. Nothing is added to the cart. Called by the
     * booking-service when a user adds an extra to their cart.
     *
     * @param extraId the id of the extra
     * @param query   the validated query: amount of the extra
     * @param userId  the id of the user whose cart the stay is read from
     * @return a response with the extra and its price for the stay
     */
    @Operation(
            summary = "Price an extra for the stay in the user's cart",
            description = """
                    **Access: Internal** (booking-service).

                    Checks that the extra can be bought, and prices it for the stay in the user's cart: from the \
                    earliest check-in to the latest check-out. Extras with a fixed price cost `price × amount`, \
                    others `price × amount × nights`. Nothing is stored, as extras are not limited.

                    If the cart's rooms change later, the price of an extra per night may no longer be correct, so it \
                    should be requested again.

                    **Response key:** `extra`.
                    """)
    @ApiResponse(responseCode = "200", description = "The extra, priced for the stay")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The extra does not exist or is not active")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The extra cannot be bought separately, or the cart has no rooms")
    @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "Extras are temporarily unavailable")
    @GetMapping("/availability/extras/{extraId}")
    public ResponseEntity<Envelopes.ExtraPriceResult> verifyExtra(
            @PathVariable UUID extraId,
            @Valid @ParameterObject @ModelAttribute ReviewExtraQuery query,
            @Parameter(description = "The id of the user whose cart the stay is read from") @RequestHeader("X-User-Id") UUID userId
    ){
        ExtraAvailabilityResponse extra = availabilityService.verifyExtra(userId, extraId, query);
        return ClientResponse.ok(new Envelopes.ExtraPriceResult(extra));
    }

    /**
     * Searches for room types a customer can book for a stay, with prices.
     * Public endpoint: nothing is reserved, so no user is needed. Rooms are
     * reserved later through the booking-service.
     *
     * @param query the validated query: guests, minimum number of rooms, check-in and check-out dates
     * @return a response with the available room types, cheapest total first
     */
    @Operation(
            summary = "Search for available room types",
            description = """
                    **Access: Public.**

                    Finds the room types a customer can book for a stay. A room type is included when enough of its \
                    rooms are free for the whole stay to fit the search on their own: at least `rooms` rooms, and \
                    enough beds for all `guests` (e.g. 4 guests in rooms for 2 needs 2 rooms). Nothing is reserved.

                    Prices are for the room type's cheapest option (variant), so no room type can be booked for less. \
                    Results are sorted by `total`, cheapest first.

                    **Response key:** `roomTypes`. An empty list means nothing is available.
                    """)
    @ApiResponse(responseCode = "200", description = "The available room types, cheapest first")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "Searching is temporarily unavailable")
    @GetMapping("/availability")
    public ResponseEntity<Envelopes.AvailableRoomTypesResult> searchAvailableRoomTypes(
            @Valid @ParameterObject @ModelAttribute ReviewSearchQuery query
    ){
        List<AvailableRoomTypeResponse> roomTypes = availabilityService.searchAvailableRoomTypes(query);
        return ClientResponse.ok(new Envelopes.AvailableRoomTypesResult(roomTypes));
    }
}

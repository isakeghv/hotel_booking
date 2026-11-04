package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.request.ReviewAvailabilityQuery;
import com.example.inventoryserviceproject.dto.request.ReviewExtraQuery;
import com.example.inventoryserviceproject.dto.request.ReviewSearchQuery;
import com.example.inventoryserviceproject.dto.response.AvailabilityResponse;
import com.example.inventoryserviceproject.dto.response.AvailableRoomTypeResponse;
import com.example.inventoryserviceproject.dto.response.ExtraAvailabilityResponse;
import com.example.inventoryserviceproject.service.AvailabilityService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing availability.
 * Contains endpoints used internally in the docker network by the booking-service,
 * and the public search for available rooms.
 */
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
    @GetMapping("/availability/variants/{variantId}")
    public ResponseEntity<Map<String, AvailabilityResponse>> reserveVariant(
            @PathVariable UUID variantId,
            @Valid @ModelAttribute ReviewAvailabilityQuery query,
            @RequestHeader("X-User-Id") UUID userId
    ){
        AvailabilityResponse reservation = availabilityService.reserveRooms(userId, variantId, query);
        return ClientResponse.ok("reservation", reservation);
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
    @GetMapping("/availability/extras/{extraId}")
    public ResponseEntity<Map<String, ExtraAvailabilityResponse>> verifyExtra(
            @PathVariable UUID extraId,
            @Valid @ModelAttribute ReviewExtraQuery query,
            @RequestHeader("X-User-Id") UUID userId
    ){
        ExtraAvailabilityResponse extra = availabilityService.verifyExtra(userId, extraId, query);
        return ClientResponse.ok("extra", extra);
    }

    /**
     * Searches for room types a customer can book for a stay, with prices.
     * Public endpoint: nothing is reserved, so no user is needed. Rooms are
     * reserved later through the booking-service.
     *
     * @param query the validated query: guests, minimum number of rooms, check-in and check-out dates
     * @return a response with the available room types, cheapest total first
     */
    @GetMapping("/availability")
    public ResponseEntity<Map<String, List<AvailableRoomTypeResponse>>> searchAvailableRoomTypes(
            @Valid @ModelAttribute ReviewSearchQuery query
    ){
        List<AvailableRoomTypeResponse> roomTypes = availabilityService.searchAvailableRoomTypes(query);
        return ClientResponse.ok("roomTypes", roomTypes);
    }
}

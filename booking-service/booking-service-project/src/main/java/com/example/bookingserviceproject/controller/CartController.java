package com.example.bookingserviceproject.controller;

import com.example.bookingserviceproject.dto.request.ReviewAddRooms;
import com.example.bookingserviceproject.dto.request.ReviewExtraAmount;
import com.example.bookingserviceproject.dto.response.CartResponse;
import com.example.bookingserviceproject.dto.response.Envelopes;
import com.example.bookingserviceproject.service.CartService;
import com.example.bookingserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for the signed-in user's cart: the rooms the user is about
 * to book. The user is always taken from the {@code X-User-Id} header set by
 * the gateway, never from the body or path.
 */
@RestController
@RequestMapping("/booking/cart")
public class CartController {
    private final CartService cartService;

    /**
     * Creates the controller with the service it delegates to.
     *
     * @param cartService service containing the cart's business logic
     */
    public CartController(CartService cartService){
        this.cartService = cartService;
    }

    /**
     * Adds rooms of one variant to the user's cart. The rooms are held by the
     * inventory-service, so no one else can book them while they are in the cart.
     *
     * @param request the validated request: variant, amount of rooms and dates
     * @param userId  the id of the signed-in user, set by the gateway
     * @return a response with the whole cart after the rooms were added
     */
    @PostMapping("/rooms")
    public ResponseEntity<Envelopes.CartResult> addRooms(
            @Valid @RequestBody ReviewAddRooms request,
            @RequestHeader("X-User-Id") UUID userId
    ){
        CartResponse cart = cartService.addRooms(userId, request);
        return ClientResponse.ok(new Envelopes.CartResult(cart));
    }

    /**
     * Retrieves the user's cart. A missing or expired cart is returned as an
     * empty cart, not an error.
     *
     * @param userId the id of the signed-in user, set by the gateway
     * @return a response with the cart
     */
    @GetMapping
    public ResponseEntity<Envelopes.CartResult> getCart(@RequestHeader("X-User-Id") UUID userId){
        CartResponse cart = cartService.getCart(userId);
        return ClientResponse.ok(new Envelopes.CartResult(cart));
    }

    /**
     * Removes one room, and the extras on it, from the user's cart. The room
     * is released in the inventory-service, so others can book it again.
     *
     * @param itemId the id of the room in the cart
     * @param userId the id of the signed-in user, set by the gateway
     * @return a response with the whole cart after the room was removed
     */
    @DeleteMapping("/rooms/{itemId}")
    public ResponseEntity<Envelopes.CartResult> removeRoom(
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") UUID userId
    ){
        CartResponse cart = cartService.removeRoom(userId, itemId);
        return ClientResponse.ok(new Envelopes.CartResult(cart));
    }

    /**
     * Sets how many of an extra a room in the user's cart has. Adds the extra
     * to the room if it does not have it yet, otherwise replaces the amount.
     *
     * @param itemId  the id of the room in the cart
     * @param extraId the id of the extra
     * @param request the validated request: how many of the extra the room should have
     * @param userId  the id of the signed-in user, set by the gateway
     * @return a response with the whole cart after the extra was set
     */
    @PatchMapping("/rooms/{itemId}/extras/{extraId}")
    public ResponseEntity<Envelopes.CartResult> setExtra(
            @PathVariable UUID itemId,
            @PathVariable UUID extraId,
            @Valid @RequestBody ReviewExtraAmount request,
            @RequestHeader("X-User-Id") UUID userId
    ){
        CartResponse cart = cartService.setExtra(userId, itemId, extraId, request.amount());
        return ClientResponse.ok(new Envelopes.CartResult(cart));
    }

    /**
     * Removes an extra from a room in the user's cart.
     *
     * @param itemId  the id of the room in the cart
     * @param extraId the id of the extra
     * @param userId  the id of the signed-in user, set by the gateway
     * @return a response with the whole cart after the extra was removed
     */
    @DeleteMapping("/rooms/{itemId}/extras/{extraId}")
    public ResponseEntity<Envelopes.CartResult> removeExtra(
            @PathVariable UUID itemId,
            @PathVariable UUID extraId,
            @RequestHeader("X-User-Id") UUID userId
    ){
        CartResponse cart = cartService.removeExtra(userId, itemId, extraId);
        return ClientResponse.ok(new Envelopes.CartResult(cart));
    }
}

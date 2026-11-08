package com.example.bookingserviceproject.controller;

import com.example.bookingserviceproject.dto.request.ReviewConfirmBooking;
import com.example.bookingserviceproject.dto.response.BookingResponse;
import com.example.bookingserviceproject.dto.response.Envelopes;
import com.example.bookingserviceproject.service.CheckoutService;
import com.example.bookingserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for the signed-in user's bookings. The user is always taken
 * from the {@code X-User-Id} header set by the gateway, never from the body or path.
 */
@RestController
@RequestMapping("/booking")
public class BookingController {
    private final CheckoutService checkoutService;

    /**
     * Creates the controller with the service it delegates to.
     *
     * @param checkoutService service that confirms bookings
     */
    public BookingController(CheckoutService checkoutService){
        this.checkoutService = checkoutService;
    }

    /**
     * Pays for the user's cart and saves it as a booking. What is booked, and
     * the price, always come from the cart.
     *
     * @param request the validated request: the card to pay with
     * @param userId  the id of the signed-in user, set by the gateway
     * @return a 201 response with the confirmed booking
     */
    @PostMapping("/confirm")
    public ResponseEntity<Envelopes.BookingResult> confirm(
            @Valid @RequestBody ReviewConfirmBooking request,
            @RequestHeader("X-User-Id") UUID userId
    ){
        BookingResponse booking = checkoutService.confirm(userId, request.card());
        return ClientResponse.created(new Envelopes.BookingResult(booking));
    }
}

package com.example.bookingserviceproject.controller;

import com.example.bookingserviceproject.dto.request.ReviewConfirmBooking;
import com.example.bookingserviceproject.dto.response.BookingResponse;
import com.example.bookingserviceproject.dto.response.Envelopes;
import com.example.bookingserviceproject.dto.response.MyBookingsResponse;
import com.example.bookingserviceproject.service.BookingService;
import com.example.bookingserviceproject.service.CancellationService;
import com.example.bookingserviceproject.service.CheckoutService;
import com.example.bookingserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
    private final BookingService bookingService;
    private final CancellationService cancellationService;

    /**
     * Creates the controller with the services it delegates to.
     *
     * @param checkoutService     service that confirms bookings
     * @param bookingService      service that lists and looks up bookings
     * @param cancellationService service that cancels bookings
     */
    public BookingController(CheckoutService checkoutService, BookingService bookingService, CancellationService cancellationService){
        this.checkoutService = checkoutService;
        this.bookingService = bookingService;
        this.cancellationService = cancellationService;
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

    /**
     * Returns an overview of the user's bookings, in three lists of at most 10:
     * upcoming, previous and cancelled. "See more" asks for the next page.
     *
     * @param page   the page, 1 or higher (default 1): 1 gives bookings 1-10 of each list, 2 gives 11-20
     * @param userId the id of the signed-in user, set by the gateway
     * @return a response with the three lists
     */
    @GetMapping("/bookings/me")
    public ResponseEntity<Envelopes.MyBookingsResult> getMyBookings(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be 1 or higher") int page,
            @RequestHeader("X-User-Id") UUID userId
    ){
        MyBookingsResponse bookings = bookingService.getMyBookings(userId, page);
        return ClientResponse.ok(new Envelopes.MyBookingsResult(bookings));
    }

    /**
     * Returns one of the user's bookings, with its rooms, extras and what is cancelled.
     *
     * @param bookingId the id of the booking
     * @param userId    the id of the signed-in user, set by the gateway
     * @return a response with the booking
     */
    @GetMapping("/bookings/me/{bookingId}")
    public ResponseEntity<Envelopes.BookingResult> getMyBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") UUID userId
    ){
        BookingResponse booking = bookingService.getMyBooking(userId, bookingId);
        return ClientResponse.ok(new Envelopes.BookingResult(booking));
    }

    /**
     * Cancels one of the user's upcoming bookings, with all its rooms and extras.
     *
     * @param bookingId the id of the booking
     * @param userId    the id of the signed-in user, set by the gateway
     * @return a 204 response without a body
     */
    @PostMapping("/bookings/me/{bookingId}/cancel")
    public ResponseEntity<Void> cancelMyBooking(
            @PathVariable UUID bookingId,
            @RequestHeader("X-User-Id") UUID userId
    ){
        cancellationService.cancelMyBooking(userId, bookingId);
        return ClientResponse.noContent();
    }

    /**
     * Cancels one room of the user's upcoming booking, with the extras on it.
     * Cancelling the last active room cancels the whole booking.
     *
     * @param bookingId the id of the booking
     * @param itemId    the id of the room
     * @param userId    the id of the signed-in user, set by the gateway
     * @return a 204 response without a body
     */
    @PostMapping("/bookings/me/{bookingId}/items/{itemId}/cancel")
    public ResponseEntity<Void> cancelMyRoom(
            @PathVariable UUID bookingId,
            @PathVariable UUID itemId,
            @RequestHeader("X-User-Id") UUID userId
    ){
        cancellationService.cancelMyRoom(userId, bookingId, itemId);
        return ClientResponse.noContent();
    }
}

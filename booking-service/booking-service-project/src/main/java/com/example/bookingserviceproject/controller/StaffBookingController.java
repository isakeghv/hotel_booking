package com.example.bookingserviceproject.controller;

import com.example.bookingserviceproject.dto.response.Envelopes;
import com.example.bookingserviceproject.dto.response.StaffBookingOverview;
import com.example.bookingserviceproject.service.StaffBookingService;
import com.example.bookingserviceproject.utils.ClientResponse;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for the staff's views of bookings. Only staff (level 1-3)
 * get here: access is checked by the gateway.
 */
@RestController
@RequestMapping("/booking")
public class StaffBookingController {
    private final StaffBookingService staffBookingService;

    /**
     * Creates the controller with the service it delegates to.
     *
     * @param staffBookingService service for the staff's views of bookings
     */
    public StaffBookingController(StaffBookingService staffBookingService){
        this.staffBookingService = staffBookingService;
    }

    /**
     * Returns an overview of the bookings that start, end, or are ongoing on a
     * date, 20 at a time, earliest start first. "See more" asks for the next page.
     *
     * @param date      the date, ISO {@code YYYY-MM-DD}, without a time
     * @param page      the page, 1 or higher (default 1): 1 gives bookings 1-20, 2 gives 21-40
     * @param cancelled true for only bookings cancelled as a whole, false (default) for only live bookings
     * @return a response with the bookings
     */
    @GetMapping("/bookings")
    public ResponseEntity<Envelopes.StaffBookingsResult> getBookingsOn(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "Page must be 1 or higher") int page,
            @RequestParam(defaultValue = "false") boolean cancelled
    ){
        List<StaffBookingOverview> bookings = staffBookingService.getBookingsOn(date, page, cancelled);
        return ClientResponse.ok(new Envelopes.StaffBookingsResult(bookings));
    }
}

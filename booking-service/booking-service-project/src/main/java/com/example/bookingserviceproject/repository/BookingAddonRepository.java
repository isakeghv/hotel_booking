package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.BookingAddon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Extras on booked rooms. Only derived queries.
 */
public interface BookingAddonRepository extends JpaRepository<BookingAddon, UUID> {

    /**
     * @param bookingId the id of a booking
     * @return the extras on all of the booking's rooms
     */
    List<BookingAddon> findAllByBooking_BookingId(UUID bookingId);
}

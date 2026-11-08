package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.BookingItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Booked rooms. Only derived queries.
 */
public interface BookingItemRepository extends JpaRepository<BookingItem, UUID> {

    /**
     * @param bookingId the id of a booking
     * @return the booking's rooms
     */
    List<BookingItem> findAllByBooking_BookingId(UUID bookingId);
}

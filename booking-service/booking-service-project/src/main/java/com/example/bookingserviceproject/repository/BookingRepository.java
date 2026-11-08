package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Bookings. Only derived queries.
 */
public interface BookingRepository extends JpaRepository<Booking, UUID> {
}

package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.CancelledItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Cancelled rooms. Only derived queries.
 */
public interface CancelledItemRepository extends JpaRepository<CancelledItem, UUID> {

    /**
     * Loads the cancelled rooms of a booking. The room is fetched in the same
     * query, so it can be read without a transaction.
     *
     * @param bookingId the id of a booking
     * @return the booking's cancelled rooms
     */
    @EntityGraph(attributePaths = "item")
    List<CancelledItem> findAllByBooking_BookingId(UUID bookingId);
}

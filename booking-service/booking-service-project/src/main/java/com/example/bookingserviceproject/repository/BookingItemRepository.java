package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.BookingItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
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

    /**
     * Loads the rooms of several bookings in one query. The booking is fetched
     * in the same query (it is one row per room, not a collection), so the rooms
     * can be grouped by booking without loading each booking separately.
     *
     * @param bookingIds the ids of the bookings
     * @return the rooms of all those bookings
     */
    @EntityGraph(attributePaths = "booking")
    List<BookingItem> findAllByBooking_BookingIdIn(Collection<UUID> bookingIds);
}

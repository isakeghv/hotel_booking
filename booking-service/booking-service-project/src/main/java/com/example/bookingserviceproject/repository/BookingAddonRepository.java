package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.BookingAddon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Extras on booked rooms. Only derived queries.
 */
public interface BookingAddonRepository extends JpaRepository<BookingAddon, UUID> {

    /**
     * Loads the extras of a booking. The room each extra belongs to is fetched
     * in the same query, so the extras can be grouped by room without a transaction.
     *
     * @param bookingId the id of a booking
     * @return the extras on all of the booking's rooms
     */
    @EntityGraph(attributePaths = "item")
    List<BookingAddon> findAllByBooking_BookingId(UUID bookingId);

    /**
     * Loads the extras of a booking, sorted by name. The room each extra belongs
     * to is fetched in the same query, so the extras can be grouped by room
     * without a transaction.
     *
     * @param bookingId the id of a booking
     * @return the extras on all of the booking's rooms, by name
     */
    @EntityGraph(attributePaths = "item")
    List<BookingAddon> findAllByBooking_BookingIdOrderByNameAsc(UUID bookingId);

    /**
     * Finds one extra on a room. A room has each extra at most once
     * ({@code UNIQUE (item_id, extra_id)}), so these two ids identify one line.
     *
     * @param itemId  the id of the room
     * @param extraId the inventory id of the extra
     * @return the extra, or empty if the room does not have it
     */
    Optional<BookingAddon> findByItem_ItemIdAndExtraId(UUID itemId, UUID extraId);
}

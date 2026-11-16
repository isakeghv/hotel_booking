package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.CancelledAddon;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Cancelled extras. Only derived queries.
 */
public interface CancelledAddonRepository extends JpaRepository<CancelledAddon, UUID> {

    /**
     * Loads the cancelled extras of a booking. The extra is fetched in the same
     * query, so it can be read without a transaction.
     *
     * @param bookingId the id of a booking
     * @return the booking's cancelled extras
     */
    @EntityGraph(attributePaths = "addon")
    List<CancelledAddon> findAllByBooking_BookingId(UUID bookingId);

    /**
     * @param addonId the id of an extra on a room
     * @return true if the extra is cancelled on its own
     */
    boolean existsByAddon_AddonId(UUID addonId);
}

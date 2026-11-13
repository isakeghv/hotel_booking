package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.Booking;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Bookings. Only derived queries.
 * <p>
 * The customer's lists are sorted closest to today first, with the bookingId
 * as a tie-breaker, so bookings with the same start date keep the same order
 * from one page to the next. A {@code List} with a {@link Pageable} gives one
 * page without a count query.
 */
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * The user's upcoming bookings: not cancelled, starting now or later.
     *
     * @param userId   the id of the user
     * @param now      the current time
     * @param pageable the page to return
     * @return the bookings, soonest first
     */
    List<Booking> findByUserIdAndCancelledFalseAndStartTimeGreaterThanEqualOrderByStartTimeAscBookingIdAsc(
            UUID userId, LocalDateTime now, Pageable pageable);

    /**
     * The user's previous bookings: not cancelled, started before now (a stay in progress included).
     *
     * @param userId   the id of the user
     * @param now      the current time
     * @param pageable the page to return
     * @return the bookings, most recent first
     */
    List<Booking> findByUserIdAndCancelledFalseAndStartTimeLessThanOrderByStartTimeDescBookingIdAsc(
            UUID userId, LocalDateTime now, Pageable pageable);

    /**
     * The user's bookings that are cancelled as a whole.
     *
     * @param userId   the id of the user
     * @param pageable the page to return
     * @return the bookings, latest start date first
     */
    List<Booking> findByUserIdAndCancelledTrueOrderByStartTimeDescBookingIdAsc(UUID userId, Pageable pageable);
}

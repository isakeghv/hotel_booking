package com.example.bookingserviceproject.repository;

import com.example.bookingserviceproject.model.Booking;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Bookings. Only derived queries.
 * <p>
 * The lists are sorted with the bookingId as a tie-breaker, so bookings with
 * the same start date keep the same order from one page to the next. A {@code List} with a {@link Pageable} gives one
 * page without a count query.
 */
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    /**
     * Finds a booking only if it belongs to the user, so a booking that does
     * not exist and one that belongs to someone else look the same.
     *
     * @param bookingId the id of the booking
     * @param userId    the id of the user
     * @return the booking, or empty if the user has no booking with this id
     */
    Optional<Booking> findByBookingIdAndUserId(UUID bookingId, UUID userId);

    /**
     * Finds the user's booking and locks its row until the transaction ends, so
     * only one request can change it at a time. With a lock timeout of 0
     * ({@code FOR UPDATE NOWAIT}), a second request does not wait: it fails at
     * once with a {@link org.springframework.dao.PessimisticLockingFailureException}.
     * Must be called inside a transaction.
     *
     * @param bookingId the id of the booking
     * @param userId    the id of the user
     * @return the locked booking, or empty if the user has no booking with this id
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "0"))
    Optional<Booking> findLockedByBookingIdAndUserId(UUID bookingId, UUID userId);

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

    /**
     * The bookings on a date, for staff: those that start, end, or are ongoing
     * on it, i.e. starting before the day ends and ending after it begins.
     *
     * @param cancelled true for only bookings cancelled as a whole, false for only live bookings
     * @param dayEnd    the start of the next day (exclusive)
     * @param dayStart  the start of the day (exclusive for the end time)
     * @param pageable  the page to return
     * @return the bookings, earliest start first
     */
    List<Booking> findByCancelledAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeAscBookingIdAsc(
            boolean cancelled, LocalDateTime dayEnd, LocalDateTime dayStart, Pageable pageable);
}

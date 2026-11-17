package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.client.UserClient;
import com.example.bookingserviceproject.dto.response.StaffBookingOverview;
import com.example.bookingserviceproject.dto.user.BasicUserInfo;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import com.example.bookingserviceproject.mapper.StaffBookingsResMapper;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.repository.BookingItemRepository;
import com.example.bookingserviceproject.repository.BookingRepository;
import com.example.bookingserviceproject.repository.CancelledItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The staff's views of bookings.
 */
@Service
public class StaffBookingService {
    private static final Logger log = LoggerFactory.getLogger(StaffBookingService.class);

    // Bookings per page in the staff's list of bookings on a date
    private static final int PAGE_SIZE = 20;

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final CancelledItemRepository cancelledItemRepository;
    private final UserClient userClient;
    private final StaffBookingsResMapper staffBookingsResMapper;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingRepository       the bookings
     * @param bookingItemRepository   the booked rooms
     * @param cancelledItemRepository the cancelled rooms
     * @param userClient              client for the user-service, which has the users' names
     * @param staffBookingsResMapper  mapper from bookings to their overviews
     */
    public StaffBookingService(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository,
                               CancelledItemRepository cancelledItemRepository, UserClient userClient,
                               StaffBookingsResMapper staffBookingsResMapper){
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.cancelledItemRepository = cancelledItemRepository;
        this.userClient = userClient;
        this.staffBookingsResMapper = staffBookingsResMapper;
    }

    /**
     * Returns one page of the bookings that start, end, or are ongoing on a
     * date, earliest start first. Loads the bookings (one query), the rooms to
     * count (one or two queries), and the users' names (one call to the
     * user-service). If the user-service cannot be asked, the bookings are
     * returned without names.
     *
     * @param date      the date, in local hotel time
     * @param page      the page, 1 or higher: 1 gives bookings 1-20, 2 gives 21-40
     * @param cancelled true for only bookings cancelled as a whole, false for only live bookings
     * @return the overviews, empty if there are none
     */
    public List<StaffBookingOverview> getBookingsOn(LocalDate date, int page, boolean cancelled){
        List<Booking> bookings = bookingRepository.findByCancelledAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeAscBookingIdAsc(
                cancelled, date.plusDays(1).atStartOfDay(), date.atStartOfDay(), PageRequest.of(page - 1, PAGE_SIZE));
        if (bookings.isEmpty()) return List.of();

        Set<UUID> bookingIds = bookings.stream().map(Booking::getBookingId).collect(Collectors.toSet());
        Map<UUID, Long> rooms = roomCounts(bookingIds, cancelled);

        // One user can have several bookings on a page. At most 20 ids, within the user-service's limit of 30
        Map<UUID, BasicUserInfo> users = basicUsers(bookings.stream().map(Booking::getUserId).collect(Collectors.toSet()));

        return bookings.stream()
                .map(booking -> staffBookingsResMapper.toOverview(
                        booking,
                        rooms.getOrDefault(booking.getBookingId(), 0L).intValue(),
                        users.get(booking.getUserId())))
                .toList();
    }

    /**
     * Counts the rooms of several bookings. For live bookings, only the active
     * rooms: rooms cancelled on their own are not counted. For cancelled
     * bookings, every room, as none of them is active.
     *
     * @param bookingIds the ids of the bookings
     * @param cancelled  whether the bookings are cancelled as a whole
     * @return the number of rooms by booking id
     */
    private Map<UUID, Long> roomCounts(Set<UUID> bookingIds, boolean cancelled){
        Set<UUID> cancelledItemIds = cancelled ? Set.of()
                : cancelledItemRepository.findAllByBooking_BookingIdIn(bookingIds).stream()
                        .map(cancelledItem -> cancelledItem.getItem().getItemId())
                        .collect(Collectors.toSet());

        return bookingItemRepository.findAllByBooking_BookingIdIn(bookingIds).stream()
                .filter(item -> !cancelledItemIds.contains(item.getItemId()))
                .collect(Collectors.groupingBy(item -> item.getBooking().getBookingId(), Collectors.counting()));
    }

    /**
     * Fetches the users' names. They are only there to make the list easier to
     * read, so if the user-service is slow, down or fails, the list is returned
     * without them instead of failing.
     *
     * @param userIds the users, without duplicates
     * @return the users by user id, or empty if they could not be fetched
     */
    private Map<UUID, BasicUserInfo> basicUsers(Set<UUID> userIds){
        try {
            return userClient.getBasicUsers(userIds);
        } catch (ServiceUnavailableException e){
            log.warn("Bookings on a date are returned without names: {}", e.getMessage());
            return Map.of();
        }
    }
}

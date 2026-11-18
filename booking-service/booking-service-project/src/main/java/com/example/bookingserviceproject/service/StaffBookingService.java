package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.client.InventoryClient;
import com.example.bookingserviceproject.client.UserClient;
import com.example.bookingserviceproject.dto.inventory.InventoryPrimaryImage;
import com.example.bookingserviceproject.dto.inventory.InventoryVariant;
import com.example.bookingserviceproject.dto.response.StaffBookingOverview;
import com.example.bookingserviceproject.dto.response.StaffBookingResponse;
import com.example.bookingserviceproject.dto.user.BasicUserInfo;
import com.example.bookingserviceproject.exception.NotFoundException;
import com.example.bookingserviceproject.exception.PassThroughException;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import com.example.bookingserviceproject.mapper.StaffBookingResMapper;
import com.example.bookingserviceproject.mapper.StaffBookingsResMapper;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingDetails;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.repository.BookingAddonRepository;
import com.example.bookingserviceproject.repository.BookingItemRepository;
import com.example.bookingserviceproject.repository.BookingRepository;
import com.example.bookingserviceproject.repository.CancelledAddonRepository;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

/**
 * The staff's views of bookings: any booking, not only the staff member's own.
 */
@Service
public class StaffBookingService {
    private static final Logger log = LoggerFactory.getLogger(StaffBookingService.class);

    // Bookings per page in the staff's list of bookings on a date
    private static final int PAGE_SIZE = 20;

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final CancelledItemRepository cancelledItemRepository;
    private final CancelledAddonRepository cancelledAddonRepository;
    private final UserClient userClient;
    private final InventoryClient inventoryClient;
    private final BookingService bookingService;
    private final ExecutorService externalCallExecutor;
    private final StaffBookingsResMapper staffBookingsResMapper;
    private final StaffBookingResMapper staffBookingResMapper;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingRepository        the bookings
     * @param bookingItemRepository    the booked rooms
     * @param bookingAddonRepository   the extras on booked rooms
     * @param cancelledItemRepository  the cancelled rooms
     * @param cancelledAddonRepository the cancelled extras
     * @param userClient               client for the user-service, which has the users' names
     * @param inventoryClient          client for the inventory-service, which has the variants
     * @param bookingService           service that fetches the room types' images
     * @param externalCallExecutor     executor for calling other services in parallel
     * @param staffBookingsResMapper   mapper from bookings to their overviews
     * @param staffBookingResMapper    mapper from a booking to its response
     */
    public StaffBookingService(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository,
                               BookingAddonRepository bookingAddonRepository, CancelledItemRepository cancelledItemRepository,
                               CancelledAddonRepository cancelledAddonRepository, UserClient userClient,
                               InventoryClient inventoryClient, BookingService bookingService,
                               ExecutorService externalCallExecutor, StaffBookingsResMapper staffBookingsResMapper,
                               StaffBookingResMapper staffBookingResMapper){
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.bookingAddonRepository = bookingAddonRepository;
        this.cancelledItemRepository = cancelledItemRepository;
        this.cancelledAddonRepository = cancelledAddonRepository;
        this.userClient = userClient;
        this.inventoryClient = inventoryClient;
        this.bookingService = bookingService;
        this.externalCallExecutor = externalCallExecutor;
        this.staffBookingsResMapper = staffBookingsResMapper;
        this.staffBookingResMapper = staffBookingResMapper;
    }

    /**
     * Returns any booking, with every room, its variant and image, the extras,
     * the totals and the guest. Loads the booking and everything that belongs
     * to it (one query per table, sorted in the database), and calls the
     * user-service (name) and inventory (variants, images) in parallel, so the
     * slowest call decides the wait instead of their sum. If one of them cannot
     * be asked, the booking is returned without what it would have given.
     *
     * @param bookingId the id of the booking
     * @return the booking
     * @throws NotFoundException if there is no booking with this id
     */
    public StaffBookingResponse getBooking(UUID bookingId){
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        // Only needs the booking, so it runs while the rest is loaded
        CompletableFuture<BasicUserInfo> user = CompletableFuture.supplyAsync(() -> basicUser(booking.getUserId()), externalCallExecutor);

        BookingDetails details = new BookingDetails(
                booking,
                bookingItemRepository.findAllByBooking_BookingIdOrderByFromTimeAscRoomNumberAsc(bookingId),
                bookingAddonRepository.findAllByBooking_BookingIdOrderByNameAsc(bookingId),
                cancelledItemRepository.findAllByBooking_BookingId(bookingId),
                cancelledAddonRepository.findAllByBooking_BookingId(bookingId));

        Set<UUID> variantIds = details.items().stream().map(BookingItem::getVariantId).collect(Collectors.toSet());
        Set<UUID> roomTypeIds = details.items().stream().map(BookingItem::getRoomTypeId).collect(Collectors.toSet());
        CompletableFuture<Map<UUID, InventoryVariant>> variants =
                CompletableFuture.supplyAsync(() -> variants(variantIds), externalCallExecutor);
        CompletableFuture<Map<UUID, InventoryPrimaryImage>> images =
                CompletableFuture.supplyAsync(() -> bookingService.primaryImages(roomTypeIds), externalCallExecutor);

        // Each call falls back on its own, so waiting for them never fails because another service did
        return staffBookingResMapper.toResponse(details, user.join(), variants.join(), images.join());
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
     * Fetches one user's name. If the user-service does not know the user, the
     * user is shown as unknown. If it is slow, down or fails, the booking is
     * returned without the name instead of failing.
     *
     * @param userId the user
     * @return the user, {@link BasicUserInfo#UNKNOWN} if not known, or null if it could not be fetched
     */
    private BasicUserInfo basicUser(UUID userId){
        try {
            return userClient.getBasicUser(userId).orElse(BasicUserInfo.UNKNOWN);
        } catch (ServiceUnavailableException e){
            log.warn("A booking is returned without the name of user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Fetches the variants, for what they include. If inventory is slow, down
     * or refuses, the booking is returned with every {@code includes} empty
     * instead of failing.
     *
     * @param variantIds the variants
     * @return the variants found by variant id, or empty if there are none or they could not be fetched
     */
    private Map<UUID, InventoryVariant> variants(Set<UUID> variantIds){
        if (variantIds.isEmpty()) return Map.of();
        // Never more than inventory accepts. Variants beyond the limit are shown with includes empty
        Set<UUID> requested = variantIds.size() <= InventoryClient.MAX_IDS_PER_REQUEST ? variantIds
                : variantIds.stream().limit(InventoryClient.MAX_IDS_PER_REQUEST).collect(Collectors.toSet());
        try {
            return inventoryClient.getVariants(requested);
        } catch (ServiceUnavailableException | PassThroughException e){
            log.warn("A booking is returned without its variants' includes: {}", e.getMessage());
            return Map.of();
        }
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

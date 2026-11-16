package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.client.InventoryClient;
import com.example.bookingserviceproject.dto.inventory.InventoryPrimaryImage;
import com.example.bookingserviceproject.dto.response.BookingResponse;
import com.example.bookingserviceproject.dto.response.MyBookingsResponse;
import com.example.bookingserviceproject.exception.NotFoundException;
import com.example.bookingserviceproject.exception.PassThroughException;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import com.example.bookingserviceproject.mapper.BookingMapper;
import com.example.bookingserviceproject.mapper.BookingResMapper;
import com.example.bookingserviceproject.mapper.MyBookingsResMapper;
import com.example.bookingserviceproject.model.BookingDetails;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.repository.BookingAddonRepository;
import com.example.bookingserviceproject.repository.BookingItemRepository;
import com.example.bookingserviceproject.repository.BookingRepository;
import com.example.bookingserviceproject.repository.CancelledAddonRepository;
import com.example.bookingserviceproject.repository.CancelledItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Saving and loading bookings, and the user's booking overviews.
 */
@Service
public class BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    // Bookings per list per page in the user's overviews
    private static final int PAGE_SIZE = 10;
    // Inventory's limit of room types per request for primary images
    private static final int MAX_IMAGES_PER_REQUEST = 30;
    // A booking's image is the primary image of its first room's room type
    private static final Comparator<BookingItem> FIRST_ROOM = Comparator.comparing(BookingItem::getFromTime)
            .thenComparing(BookingItem::getRoomTypeName);

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final CancelledItemRepository cancelledItemRepository;
    private final CancelledAddonRepository cancelledAddonRepository;
    private final BookingMapper bookingMapper;
    private final InventoryClient inventoryClient;
    private final MyBookingsResMapper myBookingsResMapper;
    private final BookingResMapper bookingResMapper;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingRepository      the bookings
     * @param bookingItemRepository  the booked rooms
     * @param bookingAddonRepository   the extras on booked rooms
     * @param cancelledItemRepository  the cancelled rooms
     * @param cancelledAddonRepository the cancelled extras
     * @param bookingMapper            mapper from a cart to the entities
     * @param inventoryClient          client for the inventory-service, which has the room types' images
     * @param myBookingsResMapper      mapper from bookings to their overviews
     * @param bookingResMapper         mapper from a booking to its response
     */
    public BookingService(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository,
                          BookingAddonRepository bookingAddonRepository, CancelledItemRepository cancelledItemRepository,
                          CancelledAddonRepository cancelledAddonRepository, BookingMapper bookingMapper,
                          InventoryClient inventoryClient, MyBookingsResMapper myBookingsResMapper,
                          BookingResMapper bookingResMapper){
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.bookingAddonRepository = bookingAddonRepository;
        this.cancelledItemRepository = cancelledItemRepository;
        this.cancelledAddonRepository = cancelledAddonRepository;
        this.bookingMapper = bookingMapper;
        this.inventoryClient = inventoryClient;
        this.myBookingsResMapper = myBookingsResMapper;
        this.bookingResMapper = bookingResMapper;
    }

    /**
     * Saves a paid cart as a booking, with all its rooms and extras, in one
     * transaction: either everything is saved, or nothing. The booking's id is
     * the cart's bookingId, so the same cart can never be saved twice.
     *
     * @param userId the id of the user who booked
     * @param cart   the paid cart, with at least one room
     * @return the saved booking, rooms and extras
     * @throws org.springframework.dao.DataAccessException if it could not be saved
     */
    @Transactional
    public BookingDetails saveFromCart(UUID userId, CartContents cart){
        Booking booking = bookingRepository.save(bookingMapper.toBooking(userId, cart, Instant.now()));

        Map<UUID, BookingItem> itemsById = new LinkedHashMap<>();
        for (CartItem item : cart.items()){
            itemsById.put(item.itemId(), bookingMapper.toItem(item, booking));
        }
        List<BookingItem> items = bookingItemRepository.saveAll(itemsById.values());

        // The cart only holds extras whose room is in it, so every extra finds its room here
        List<BookingAddon> addons = bookingAddonRepository.saveAll(cart.addons().stream()
                .map(addon -> bookingMapper.toAddon(addon, booking, itemsById.get(addon.itemId())))
                .toList());

        // A new booking has no cancellations
        return new BookingDetails(booking, items, addons, List.of(), List.of());
    }

    /**
     * Loads a booking with its rooms, extras and cancellations, one query per table.
     *
     * @param bookingId the id of the booking
     * @return the booking with everything that belongs to it, or empty if there is no such booking
     */
    public Optional<BookingDetails> findDetails(UUID bookingId){
        return bookingRepository.findById(bookingId).map(this::loadDetails);
    }

    /**
     * Returns one of the user's bookings, with the primary image of each room's
     * room type. If inventory cannot be reached, the booking is returned without images.
     *
     * @param userId    the id of the user
     * @param bookingId the id of the booking
     * @return the booking
     * @throws NotFoundException if the user has no booking with this id (it does not exist, or belongs to someone else)
     */
    public BookingResponse getMyBooking(UUID userId, UUID bookingId){
        BookingDetails details = bookingRepository.findByBookingIdAndUserId(bookingId, userId)
                .map(this::loadDetails)
                .orElseThrow(() -> new NotFoundException("Booking not found"));

        Set<UUID> roomTypeIds = details.items().stream().map(BookingItem::getRoomTypeId).collect(Collectors.toSet());
        return bookingResMapper.toResponse(details, primaryImages(roomTypeIds));
    }

    /**
     * Loads everything that belongs to a booking, one query per table. Each
     * query fetches what the mappers read from it (e.g. an extra's room), so no
     * transaction has to stay open, e.g. while inventory is called afterwards.
     *
     * @param booking the booking
     * @return the booking with its rooms, extras and cancellations
     */
    private BookingDetails loadDetails(Booking booking){
        UUID bookingId = booking.getBookingId();
        return new BookingDetails(
                booking,
                bookingItemRepository.findAllByBooking_BookingId(bookingId),
                bookingAddonRepository.findAllByBooking_BookingId(bookingId),
                cancelledItemRepository.findAllByBooking_BookingId(bookingId),
                cancelledAddonRepository.findAllByBooking_BookingId(bookingId));
    }

    /**
     * Returns one page of the user's booking overviews, in three lists of at
     * most 10: upcoming, previous and cancelled. Loads the bookings (one query
     * per list), the rooms of all of them (one query), and their images (one
     * call to inventory). If inventory cannot be reached, the overviews are
     * returned without images.
     *
     * @param userId the id of the user
     * @param page   the page, 1 or higher: 1 gives bookings 1-10 of each list, 2 gives 11-20
     * @return the overviews
     */
    public MyBookingsResponse getMyBookings(UUID userId, int page){
        Pageable pageable = PageRequest.of(page - 1, PAGE_SIZE);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> upcoming = bookingRepository
                .findByUserIdAndCancelledFalseAndStartTimeGreaterThanEqualOrderByStartTimeAscBookingIdAsc(userId, now, pageable);
        List<Booking> previous = bookingRepository
                .findByUserIdAndCancelledFalseAndStartTimeLessThanOrderByStartTimeDescBookingIdAsc(userId, now, pageable);
        List<Booking> cancelled = bookingRepository
                .findByUserIdAndCancelledTrueOrderByStartTimeDescBookingIdAsc(userId, pageable);

        Set<UUID> bookingIds = Stream.of(upcoming, previous, cancelled)
                .flatMap(List::stream)
                .map(Booking::getBookingId)
                .collect(Collectors.toSet());
        Map<UUID, List<BookingItem>> activeRoomsByBooking = activeRooms(bookingIds);

        // The first active room's room type of each booking: at most 30 (3 lists of 10)
        Map<UUID, UUID> roomTypeByBooking = activeRoomsByBooking.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream().min(FIRST_ROOM).orElseThrow().getRoomTypeId()));
        Map<UUID, InventoryPrimaryImage> images = primaryImages(Set.copyOf(roomTypeByBooking.values()));

        Function<Booking, MyBookingsResponse.Overview> toOverview = booking -> myBookingsResMapper.toOverview(
                booking,
                activeRoomsByBooking.getOrDefault(booking.getBookingId(), List.of()).size(),
                images.get(roomTypeByBooking.get(booking.getBookingId())));

        return new MyBookingsResponse(
                upcoming.stream().map(toOverview).toList(),
                previous.stream().map(toOverview).toList(),
                cancelled.stream().map(toOverview).toList());
    }

    /**
     * Loads the active rooms of several bookings, one query for the rooms and
     * one for the cancelled rooms. A room is active if it was not cancelled on
     * its own. A booking cancelled as a whole keeps the rooms it had when it was
     * cancelled (the umbrella adds no rows).
     * <p>
     * If every room of a booking was cancelled one by one (its last room cancelled
     * the booking), all its rooms are used instead, so the cancelled list never
     * shows a booking with 0 rooms and no image.
     *
     * @param bookingIds the ids of the bookings
     * @return the active rooms by booking id
     */
    private Map<UUID, List<BookingItem>> activeRooms(Set<UUID> bookingIds){
        if (bookingIds.isEmpty()) return Map.of();

        Map<UUID, List<BookingItem>> roomsByBooking = bookingItemRepository.findAllByBooking_BookingIdIn(bookingIds).stream()
                .collect(Collectors.groupingBy(item -> item.getBooking().getBookingId()));
        Set<UUID> cancelledItemIds = cancelledItemRepository.findAllByBooking_BookingIdIn(bookingIds).stream()
                .map(cancelled -> cancelled.getItem().getItemId())
                .collect(Collectors.toSet());

        return roomsByBooking.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    List<BookingItem> active = entry.getValue().stream()
                            .filter(item -> !cancelledItemIds.contains(item.getItemId()))
                            .toList();
                    return active.isEmpty() ? entry.getValue() : active;
                }));
    }

    /**
     * Fetches the primary images of the given room types. The images are only
     * decoration in the overviews, so if inventory is slow or down, the
     * overviews are returned without them instead of failing.
     *
     * @param roomTypeIds the room types
     * @return the images by room type id, or empty if there are none or they could not be fetched
     */
    private Map<UUID, InventoryPrimaryImage> primaryImages(Set<UUID> roomTypeIds){
        if (roomTypeIds.isEmpty()) return Map.of();
        // Never more than inventory accepts. Room types beyond the limit are shown without an image
        Set<UUID> requested = roomTypeIds.size() <= MAX_IMAGES_PER_REQUEST ? roomTypeIds
                : roomTypeIds.stream().limit(MAX_IMAGES_PER_REQUEST).collect(Collectors.toSet());
        try {
            return inventoryClient.getPrimaryImages(requested).stream()
                    .filter(image -> image.roomTypeId() != null)
                    .collect(Collectors.toMap(InventoryPrimaryImage::roomTypeId, Function.identity(), (first, second) -> first));
        } catch (ServiceUnavailableException | PassThroughException e){
            log.warn("Booking overviews are returned without images: {}", e.getMessage());
            return Map.of();
        }
    }
}

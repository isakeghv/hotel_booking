package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.client.InventoryClient;
import com.example.bookingserviceproject.dto.inventory.InventoryPrimaryImage;
import com.example.bookingserviceproject.dto.response.MyBookingsResponse;
import com.example.bookingserviceproject.exception.PassThroughException;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import com.example.bookingserviceproject.mapper.BookingMapper;
import com.example.bookingserviceproject.mapper.MyBookingsResMapper;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.repository.BookingAddonRepository;
import com.example.bookingserviceproject.repository.BookingItemRepository;
import com.example.bookingserviceproject.repository.BookingRepository;
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
    // A booking's image is the primary image of its first room's room type
    private static final Comparator<BookingItem> FIRST_ROOM = Comparator.comparing(BookingItem::getFromTime)
            .thenComparing(BookingItem::getRoomTypeName);

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final BookingMapper bookingMapper;
    private final InventoryClient inventoryClient;
    private final MyBookingsResMapper myBookingsResMapper;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingRepository      the bookings
     * @param bookingItemRepository  the booked rooms
     * @param bookingAddonRepository the extras on booked rooms
     * @param bookingMapper          mapper from a cart to the entities
     * @param inventoryClient        client for the inventory-service, which has the room types' images
     * @param myBookingsResMapper    mapper from bookings to their overviews
     */
    public BookingService(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository,
                          BookingAddonRepository bookingAddonRepository, BookingMapper bookingMapper,
                          InventoryClient inventoryClient, MyBookingsResMapper myBookingsResMapper){
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.bookingAddonRepository = bookingAddonRepository;
        this.bookingMapper = bookingMapper;
        this.inventoryClient = inventoryClient;
        this.myBookingsResMapper = myBookingsResMapper;
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

        return new BookingDetails(booking, items, addons);
    }

    /**
     * Loads a booking with its rooms and extras, one query per table.
     *
     * @param bookingId the id of the booking
     * @return the booking, rooms and extras, or empty if there is no such booking
     */
    @Transactional(readOnly = true)
    public Optional<BookingDetails> findDetails(UUID bookingId){
        // Rooms are loaded before extras, so the extras point to the loaded rooms
        return bookingRepository.findById(bookingId).map(booking -> new BookingDetails(
                booking,
                bookingItemRepository.findAllByBooking_BookingId(bookingId),
                bookingAddonRepository.findAllByBooking_BookingId(bookingId)));
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
        Map<UUID, List<BookingItem>> roomsByBooking = bookingIds.isEmpty() ? Map.of()
                : bookingItemRepository.findAllByBooking_BookingIdIn(bookingIds).stream()
                        .collect(Collectors.groupingBy(item -> item.getBooking().getBookingId()));

        // The first room's room type of each booking. At most 30 (3 lists of 10), inventory's limit per request
        Map<UUID, UUID> roomTypeByBooking = roomsByBooking.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().stream().min(FIRST_ROOM).orElseThrow().getRoomTypeId()));
        Map<UUID, InventoryPrimaryImage> images = primaryImages(Set.copyOf(roomTypeByBooking.values()));

        Function<Booking, MyBookingsResponse.Overview> toOverview = booking -> myBookingsResMapper.toOverview(
                booking,
                roomsByBooking.getOrDefault(booking.getBookingId(), List.of()).size(),
                images.get(roomTypeByBooking.get(booking.getBookingId())));

        return new MyBookingsResponse(
                upcoming.stream().map(toOverview).toList(),
                previous.stream().map(toOverview).toList(),
                cancelled.stream().map(toOverview).toList());
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
        try {
            return inventoryClient.getPrimaryImages(roomTypeIds).stream()
                    .filter(image -> image.roomTypeId() != null)
                    .collect(Collectors.toMap(InventoryPrimaryImage::roomTypeId, Function.identity(), (first, second) -> first));
        } catch (ServiceUnavailableException | PassThroughException e){
            log.warn("Booking overviews are returned without images: {}", e.getMessage());
            return Map.of();
        }
    }
}

package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.mapper.BookingMapper;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.repository.BookingAddonRepository;
import com.example.bookingserviceproject.repository.BookingItemRepository;
import com.example.bookingserviceproject.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Saving and loading bookings in the database.
 */
@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final BookingAddonRepository bookingAddonRepository;
    private final BookingMapper bookingMapper;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingRepository      the bookings
     * @param bookingItemRepository  the booked rooms
     * @param bookingAddonRepository the extras on booked rooms
     * @param bookingMapper          mapper from a cart to the entities
     */
    public BookingService(BookingRepository bookingRepository, BookingItemRepository bookingItemRepository,
                          BookingAddonRepository bookingAddonRepository, BookingMapper bookingMapper){
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.bookingAddonRepository = bookingAddonRepository;
        this.bookingMapper = bookingMapper;
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
}

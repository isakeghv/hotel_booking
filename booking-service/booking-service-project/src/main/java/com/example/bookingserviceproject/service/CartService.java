package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.cache.BookingCart;
import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.client.InventoryClient;
import com.example.bookingserviceproject.dto.inventory.InventoryReservation;
import com.example.bookingserviceproject.dto.request.ReviewAddRooms;
import com.example.bookingserviceproject.dto.response.CartResponse;
import com.example.bookingserviceproject.exception.ConflictException;
import com.example.bookingserviceproject.exception.InternalServerException;
import com.example.bookingserviceproject.mapper.CartItemMapper;
import com.example.bookingserviceproject.mapper.CartResMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Business logic for the user's cart: the rooms the user is about to book.
 * The rooms themselves are held by the inventory-service. This service keeps
 * the booking-service's own cart, with names and prices, in the booking cache.
 */
@Service
public class CartService {
    private static final Logger log = LoggerFactory.getLogger(CartService.class);

    // The booking cart expires this long before inventory's, so rooms are never paid for after inventory released them
    private static final Duration EXPIRY_MARGIN = Duration.ofMinutes(5);
    // With less time left, rooms added now cannot be paid for in time (the same limit as the inventory-service)
    private static final Duration MIN_TIME_LEFT = Duration.ofMinutes(1);

    private final BookingCart bookingCart;
    private final InventoryClient inventoryClient;
    private final CartItemMapper cartItemMapper;
    private final CartResMapper cartResMapper;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingCart     access to the carts in the booking cache
     * @param inventoryClient client for the inventory-service, which holds the rooms
     * @param cartItemMapper  mapper from rooms held by inventory to cart items
     * @param cartResMapper   mapper from a cart to its response
     */
    public CartService(BookingCart bookingCart, InventoryClient inventoryClient, CartItemMapper cartItemMapper, CartResMapper cartResMapper){
        this.bookingCart = bookingCart;
        this.inventoryClient = inventoryClient;
        this.cartItemMapper = cartItemMapper;
        this.cartResMapper = cartResMapper;
    }

    /**
     * Adds rooms of one variant to the user's cart. The inventory-service
     * checks that they are free and holds them. The first room added starts
     * the cart, which expires 5 minutes before inventory's cart and is never extended.
     *
     * @param userId  the id of the user
     * @param request the validated request: variant, amount of rooms and dates
     * @return the whole cart after the rooms were added
     * @throws ConflictException if the cart is about to expire, or inventory refuses (passed on)
     */
    public CartResponse addRooms(UUID userId, ReviewAddRooms request){
        // Checked before calling inventory: rooms added to a cart that is about to expire cannot be paid for in time
        Optional<Instant> currentExpiry = fromCache(() -> bookingCart.getExpiry(userId));
        if (currentExpiry.isPresent() && hasTooLittleTimeLeft(currentExpiry.get())){
            throw new ConflictException("Your cart is about to expire. Please complete your booking, or try again when it has expired");
        }

        InventoryReservation reservation = inventoryClient.reserveRooms(
                userId, request.variantId(), request.amount(), request.from(), request.to());
        List<UUID> itemIds = reservation.rooms().stream().map(InventoryReservation.Room::itemId).toList();

        // Inventory's cart outlives this by 5 minutes. If this is expired within that time, inventory returns its old expiry
        // To account for "near-limit" edge cases
        Instant expiresAt = reservation.expiresAt().minus(EXPIRY_MARGIN);
        if (hasTooLittleTimeLeft(expiresAt)){
            log.warn("Rooms {} held for user {} cannot be added: inventory's cart expires at {}", itemIds, userId, reservation.expiresAt());
            throw new ConflictException("Your previous cart has just expired. Please try again in a few minutes");
        }

        List<CartItem> items = reservation.rooms().stream()
                .map(room -> cartItemMapper.toCartItem(room, reservation))
                .toList();
        try {
            Instant cartExpiry = bookingCart.getOrStartExpiry(userId, expiresAt);
            bookingCart.addItems(userId, items, cartExpiry);
        } catch (DataAccessException e){
            // TODO : Release the rooms in inventory (reservation-item-remove) when the Kafka producer is added.
            //  Until then they stay held until inventory's cart expires
            log.error("Could not store rooms {} held for user {} in the booking cache", itemIds, userId, e);
            throw new InternalServerException("The rooms could not be added to your cart. Please try again");
        }

        CartContents cart = fromCache(() -> bookingCart.getCart(userId));
        return cartResMapper.toResponse(cart);
    }

    /**
     * @param expiresAt when the cart expires
     * @return true if there is too little time left to add rooms and still pay for them
     */
    private boolean hasTooLittleTimeLeft(Instant expiresAt){
        return Duration.between(Instant.now(), expiresAt).compareTo(MIN_TIME_LEFT) < 0;
    }

    /**
     * Runs a read from the booking cache, and turns a Redis failure into an
     * error the client understands.
     *
     * @param read the read to run
     * @param <T>  the type of the result
     * @return the result of the read
     * @throws InternalServerException if Redis cannot be reached
     */
    private <T> T fromCache(Supplier<T> read){
        try {
            return read.get();
        } catch (DataAccessException e){
            log.error("Could not read from the booking cache", e);
            throw new InternalServerException("Your cart is temporarily unavailable. Please try again");
        }
    }
}

package com.example.bookingserviceproject.service;

import com.example.bookingserviceproject.cache.BookingCart;
import com.example.bookingserviceproject.cache.CartAddon;
import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.client.InventoryClient;
import com.example.bookingserviceproject.dto.inventory.InventoryExtraPrice;
import com.example.bookingserviceproject.dto.inventory.InventoryReservation;
import com.example.bookingserviceproject.dto.request.ReviewAddRooms;
import com.example.bookingserviceproject.dto.response.CartResponse;
import com.example.bookingserviceproject.exception.ConflictException;
import com.example.bookingserviceproject.exception.EventPublishException;
import com.example.bookingserviceproject.exception.InternalServerException;
import com.example.bookingserviceproject.exception.NotFoundException;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import com.example.bookingserviceproject.mapper.CartAddonMapper;
import com.example.bookingserviceproject.mapper.CartItemMapper;
import com.example.bookingserviceproject.mapper.CartResMapper;
import com.example.bookingserviceproject.messaging.InventoryEventPublisher;
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
    private static final String BOOKING_IN_PROGRESS = "Your booking is being completed, so the cart cannot be changed right now";

    private final BookingCart bookingCart;
    private final InventoryClient inventoryClient;
    private final CartItemMapper cartItemMapper;
    private final CartAddonMapper cartAddonMapper;
    private final CartResMapper cartResMapper;
    private final InventoryEventPublisher inventoryEventPublisher;

    /**
     * Creates the service with its dependencies.
     *
     * @param bookingCart     access to the carts in the booking cache
     * @param inventoryClient client for the inventory-service, which holds the rooms
     * @param cartItemMapper  mapper from rooms held by inventory to cart items
     * @param cartAddonMapper mapper from extras priced by inventory to cart addons
     * @param cartResMapper   mapper from a cart to its response
     * @param inventoryEventPublisher sends the events the inventory-service listens to
     */
    public CartService(BookingCart bookingCart, InventoryClient inventoryClient, CartItemMapper cartItemMapper,
                       CartAddonMapper cartAddonMapper, CartResMapper cartResMapper, InventoryEventPublisher inventoryEventPublisher){
        this.bookingCart = bookingCart;
        this.inventoryClient = inventoryClient;
        this.cartItemMapper = cartItemMapper;
        this.cartAddonMapper = cartAddonMapper;
        this.cartResMapper = cartResMapper;
        this.inventoryEventPublisher = inventoryEventPublisher;
    }

    /**
     * Adds rooms of one variant to the user's cart. The inventory-service
     * checks that they are free and holds them. The first room added starts
     * the cart, which expires 5 minutes before inventory's cart and is never extended.
     *
     * @param userId  the id of the user
     * @param request the validated request: variant, amount of rooms and dates
     * @return the whole cart after the rooms were added
     * @throws ConflictException if the booking is being completed, the cart is about to expire, or inventory refuses (passed on)
     */
    public CartResponse addRooms(UUID userId, ReviewAddRooms request){
        if (fromCache(() -> bookingCart.isCompleted(userId))) throw new ConflictException(BOOKING_IN_PROGRESS);

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
     * Retrieves the user's cart. Nothing is asked from the inventory-service:
     * names and prices were stored when the rooms were added.
     *
     * @param userId the id of the user
     * @return the cart, or an empty cart if the user has none or it has expired
     */
    public CartResponse getCart(UUID userId){
        CartContents cart = fromCache(() -> bookingCart.getCart(userId));
        return cartResMapper.toResponse(cart);
    }

    /**
     * Removes one room, and the extras on it, from the user's cart, and tells the
     * inventory-service to release it. Removing the last room deletes the whole cart.
     *
     * @param userId the id of the user
     * @param itemId the id of the room in the cart
     * @return the cart after the room was removed, or an empty cart if it was the last room
     * @throws ConflictException           if the booking is being completed
     * @throws NotFoundException           if the room is not in the user's cart
     * @throws ServiceUnavailableException if inventory could not be told, in which case the cart is not changed
     */
    public CartResponse removeRoom(UUID userId, UUID itemId){
        CartContents cart = fromCache(() -> bookingCart.getCart(userId));
        if (cart.completed()) throw new ConflictException(BOOKING_IN_PROGRESS);
        // Also covers an expired cart, and stops events for rooms that are not this user's
        if (!cart.contains(itemId)) throw new NotFoundException("The room is not in your cart");

        // Inventory is told FIRST, and our cart is only changed after Kafka has confirmed the event (waits at most about 5 s).
        // If the event cannot be sent, the room stays in our cart and the user is asked to try again.
        // Removing it anyway would leave the room held in inventory but missing from our cart, and the two carts
        // would drift further apart . Sending the event twice is safe: inventory ignores repeats
        try {
            inventoryEventPublisher.reservationItemRemove(userId, itemId);
        } catch (EventPublishException e){
            log.error("Could not tell inventory to release room {} for user {}. The cart was not changed", itemId, userId, e);
            throw new ServiceUnavailableException("The room could not be removed right now. Please try again later");
        }

        try {
            bookingCart.removeItem(userId, itemId);
        } catch (DataAccessException e){
            // Inventory has already released the room, but it is still in our cart. Trying again fixes it:
            // the room is found, the event is sent again (ignored by inventory) and the room is removed
            log.error("Inventory released room {} for user {}, but it could not be removed from the booking cache", itemId, userId, e);
            throw new InternalServerException("The room could not be removed from your cart. Please try again");
        }

        return getCart(userId);
    }

    /**
     * Sets how many of an extra a room in the user's cart has. A new extra is
     * priced by the inventory-service. For an extra the room already has, the
     * amount is replaced and the total recalculated from the stored unit price,
     * without asking inventory.
     *
     * @param userId  the id of the user
     * @param itemId  the id of the room in the cart
     * @param extraId the id of the extra
     * @param amount  how many of the extra the room should have
     * @return the cart after the extra was set
     * @throws ConflictException if the booking is being completed, or inventory refuses (passed on)
     * @throws NotFoundException if the room is not in the user's cart, or inventory does not know the extra (passed on)
     */
    public CartResponse setExtra(UUID userId, UUID itemId, UUID extraId, int amount){
        CartContents cart = fromCache(() -> bookingCart.getCart(userId));
        if (cart.completed()) throw new ConflictException(BOOKING_IN_PROGRESS);
        // Also covers an expired cart, and stops extras on rooms that are not this user's
        CartItem item = cart.findItem(itemId).orElseThrow(() -> new NotFoundException("The room is not in your cart"));

        // Known limit: an existing line is not checked with inventory again, so its amount can still be changed
        // if inventory deactivated the extra meanwhile. Acceptable, since a cart only lives about 25 minutes
        CartAddon addon = cart.findAddon(itemId, extraId)
                .map(existing -> cartAddonMapper.withAmount(existing, amount))
                .orElseGet(() -> {
                    InventoryExtraPrice price = inventoryClient.priceExtra(userId, extraId, amount);
                    return cartAddonMapper.toCartAddon(price, item, amount);
                });

        try {
            // Replaces the line: the amount is set, not added, so the last request wins
            bookingCart.putAddon(userId, addon, cart.expiresAt());
        } catch (DataAccessException e){
            log.error("Could not store extra {} on room {} for user {} in the booking cache", extraId, itemId, userId, e);
            throw new InternalServerException("The extra could not be added to your cart. Please try again");
        }

        return getCart(userId);
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

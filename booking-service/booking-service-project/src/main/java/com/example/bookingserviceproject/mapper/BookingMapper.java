package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.cache.CartAddon;
import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.utils.PriceUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

/**
 * Converts a cart from the cache into the entities saved when the booking is confirmed.
 */
@Component
public class BookingMapper {

    /**
     * Converts a cart to a booking. The booking's id is the cart's bookingId,
     * and its stay runs from the earliest check-in to the latest check-out of its rooms.
     *
     * @param userId    the id of the user who booked
     * @param cart      the cart, with at least one room
     * @param createdAt when the booking was made
     * @return the booking
     */
    public Booking toBooking(UUID userId, CartContents cart, Instant createdAt){
        LocalDateTime start = cart.items().stream().map(CartItem::from).min(Comparator.naturalOrder()).orElseThrow();
        LocalDateTime end = cart.items().stream().map(CartItem::to).max(Comparator.naturalOrder()).orElseThrow();
        return new Booking(cart.bookingId(), userId, start, end, cart.total(), PriceUtils.CURRENCY, createdAt);
    }

    /**
     * Converts a room in the cart to a booked room. The item id stays the same,
     * as inventory uses it for the room's occupation.
     *
     * @param item    the room in the cart
     * @param booking the booking it belongs to
     * @return the booked room
     */
    public BookingItem toItem(CartItem item, Booking booking){
        return new BookingItem(
                item.itemId(),
                booking,
                item.roomNumber(),
                item.roomTypeId(),
                item.roomTypeName(),
                item.variantId(),
                item.variantName(),
                item.from(),
                item.to(),
                item.total());
    }

    /**
     * Converts an extra in the cart to an extra on a booked room.
     *
     * @param addon   the extra in the cart
     * @param booking the booking it belongs to
     * @param item    the booked room it belongs to
     * @return the booked extra
     */
    public BookingAddon toAddon(CartAddon addon, Booking booking, BookingItem item){
        return new BookingAddon(booking, item, addon.extraId(), addon.name(), addon.amount(), addon.total());
    }
}

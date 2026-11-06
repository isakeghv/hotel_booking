package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.dto.inventory.InventoryReservation;
import org.springframework.stereotype.Component;

/**
 * Converts rooms held by the inventory-service into {@link CartItem}s for the booking cache.
 */
@Component
public class CartItemMapper {

    /**
     * Converts a room held by the inventory-service to an item in the cart.
     *
     * @param room        the held room
     * @param reservation the reservation it belongs to, with the room type, variant and dates
     * @return the cart item
     */
    public CartItem toCartItem(InventoryReservation.Room room, InventoryReservation reservation){
        return new CartItem(
                room.itemId(),
                room.roomNumber(),
                reservation.roomType().roomTypeId(),
                reservation.roomType().name(),
                reservation.variant().variantId(),
                reservation.variant().name(),
                reservation.from(),
                reservation.to(),
                reservation.nights(),
                room.total());
    }
}

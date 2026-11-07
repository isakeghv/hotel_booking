package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.cache.CartAddon;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.dto.inventory.InventoryExtraPrice;
import com.example.bookingserviceproject.utils.PriceUtils;
import org.springframework.stereotype.Component;

/**
 * Converts extras priced by the inventory-service into {@link CartAddon}s for the booking cache.
 */
@Component
public class CartAddonMapper {

    /**
     * Converts an extra priced by the inventory-service to an extra on a room in the cart.
     *
     * @param price the priced extra
     * @param item  the room the extra is added to
     * @return the cart addon, with inventory's total
     */
    public CartAddon toCartAddon(InventoryExtraPrice price, CartItem item){
        return new CartAddon(
                item.itemId(),
                price.extra().extraId(),
                price.extra().name(),
                price.amount(),
                price.extra().unitPrice(),
                price.extra().priceFixed(),
                item.nights(),
                price.total());
    }

    /**
     * Returns the extra with a new amount, and its total calculated from the
     * stored unit price, without asking the inventory-service.
     *
     * @param addon  the extra in the cart
     * @param amount the new amount
     * @return the extra with the new amount and total
     */
    public CartAddon withAmount(CartAddon addon, int amount){
        return new CartAddon(
                addon.itemId(),
                addon.extraId(),
                addon.name(),
                amount,
                addon.unitPrice(),
                addon.priceFixed(),
                addon.nights(),
                PriceUtils.extraTotal(addon.unitPrice(), addon.priceFixed(), amount, addon.nights()));
    }
}

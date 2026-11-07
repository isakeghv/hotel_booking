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
     * Only inventory's unit price is used: the total is calculated for the room's own nights,
     * as an extra belongs to its room (inventory's total is for the whole stay in the cart).
     *
     * @param price  the priced extra
     * @param item   the room the extra is added to
     * @param amount how many of the extra
     * @return the cart addon, with its total for the room's stay
     */
    public CartAddon toCartAddon(InventoryExtraPrice price, CartItem item, int amount){
        InventoryExtraPrice.Extra extra = price.extra();
        return new CartAddon(
                item.itemId(),
                extra.extraId(),
                extra.name(),
                amount,
                extra.unitPrice(),
                extra.priceFixed(),
                item.nights(),
                PriceUtils.extraTotal(extra.unitPrice(), extra.priceFixed(), amount, item.nights()));
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

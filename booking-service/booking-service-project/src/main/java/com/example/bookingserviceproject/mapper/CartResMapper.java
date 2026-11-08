package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.cache.CartAddon;
import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.dto.response.CartResponse;
import com.example.bookingserviceproject.utils.PriceUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts a cart from the cache into a {@link CartResponse}.
 */
@Component
public class CartResMapper {
    // Redis does not keep the order of a hash, so the rooms are sorted for a stable order
    private static final Comparator<CartItem> ITEM_ORDER = Comparator.comparing(CartItem::from)
            .thenComparing(CartItem::roomTypeName)
            .thenComparing(CartItem::variantName)
            .thenComparing(CartItem::itemId);
    private static final Comparator<CartAddon> ADDON_ORDER = Comparator.comparing(CartAddon::name)
            .thenComparing(CartAddon::extraId);

    /**
     * Builds the response for a cart. An empty or expired cart gives an empty
     * response, not an error.
     *
     * @param cart the cart's contents
     * @return the response object
     */
    public CartResponse toResponse(CartContents cart){
        if (cart.isEmpty()) return new CartResponse(null, PriceUtils.CURRENCY, List.of(), 0);

        Map<UUID, List<CartAddon>> addonsByItem = cart.addons().stream()
                .collect(Collectors.groupingBy(CartAddon::itemId));
        List<CartResponse.Item> items = cart.items().stream()
                .sorted(ITEM_ORDER)
                .map(item -> toItem(item, addonsByItem.getOrDefault(item.itemId(), List.of())))
                .toList();

        return new CartResponse(cart.expiresAt(), PriceUtils.CURRENCY, items, cart.total());
    }

    /**
     * Builds the response for one room in the cart, with its extras.
     *
     * @param item   the room
     * @param addons the extras on the room
     * @return the response object
     */
    private CartResponse.Item toItem(CartItem item, List<CartAddon> addons){
        return new CartResponse.Item(
                item.itemId(),
                item.roomTypeName(),
                item.variantName(),
                item.from(),
                item.to(),
                item.nights(),
                item.total(),
                addons.stream().sorted(ADDON_ORDER).map(this::toExtra).toList());
    }

    /**
     * Builds the response for one extra on a room.
     *
     * @param addon the extra
     * @return the response object
     */
    private CartResponse.Extra toExtra(CartAddon addon){
        return new CartResponse.Extra(addon.extraId(), addon.name(), addon.amount(), addon.total());
    }
}

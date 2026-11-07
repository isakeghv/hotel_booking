package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.cache.CartContents;
import com.example.bookingserviceproject.cache.CartItem;
import com.example.bookingserviceproject.dto.response.CartResponse;
import com.example.bookingserviceproject.utils.PriceUtils;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

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

    /**
     * Builds the response for a cart. An empty or expired cart gives an empty
     * response, not an error.
     *
     * @param cart the cart's contents
     * @return the response object
     */
    public CartResponse toResponse(CartContents cart){
        if (cart.isEmpty()) return new CartResponse(null, PriceUtils.CURRENCY, List.of(), 0);

        List<CartResponse.Item> items = cart.items().stream()
                .sorted(ITEM_ORDER)
                .map(this::toItem)
                .toList();

        return new CartResponse(
                cart.expiresAt(),
                PriceUtils.CURRENCY,
                items,
                cart.items().stream().mapToInt(CartItem::total).sum());
    }

    /**
     * Builds the response for one room in the cart.
     *
     * @param item the room
     * @return the response object
     */
    private CartResponse.Item toItem(CartItem item){
        return new CartResponse.Item(
                item.itemId(),
                item.roomTypeName(),
                item.variantName(),
                item.from(),
                item.to(),
                item.nights(),
                item.total(),
                // Filled from the cart's addon fields when the extras endpoints are added
                List.of());
    }
}

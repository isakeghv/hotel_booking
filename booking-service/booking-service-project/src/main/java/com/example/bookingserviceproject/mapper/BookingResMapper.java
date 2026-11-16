package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.dto.inventory.InventoryPrimaryImage;
import com.example.bookingserviceproject.dto.response.BookingResponse;
import com.example.bookingserviceproject.dto.response.ImageResponse;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.model.BookingDetails;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts a booking with its rooms, extras and cancellations into a {@link BookingResponse}.
 */
@Component
public class BookingResMapper {
    private static final Comparator<BookingItem> ITEM_ORDER = Comparator.comparing(BookingItem::getFromTime)
            .thenComparing(BookingItem::getRoomTypeName)
            .thenComparing(BookingItem::getVariantName)
            .thenComparing(BookingItem::getItemId);

    /**
     * Builds the response for a booking.
     *
     * @param details            the booking, its rooms, extras and cancellations
     * @param imagesByRoomType   the primary images by room type id. Empty gives every room an image with null fields
     * @return the response object
     */
    public BookingResponse toResponse(BookingDetails details, Map<UUID, InventoryPrimaryImage> imagesByRoomType){
        Booking booking = details.booking();
        Map<UUID, List<BookingAddon>> addonsByItem = details.addons().stream()
                .collect(Collectors.groupingBy(addon -> addon.getItem().getItemId()));
        Set<UUID> cancelledItemIds = details.cancelledItems().stream()
                .map(cancelled -> cancelled.getItem().getItemId())
                .collect(Collectors.toSet());
        Set<UUID> cancelledAddonIds = details.cancelledAddons().stream()
                .map(cancelled -> cancelled.getAddon().getAddonId())
                .collect(Collectors.toSet());

        // A cancelled booking is an umbrella: its rooms and extras count as cancelled, and so do the extras of a cancelled room
        List<BookingResponse.Item> items = details.items().stream()
                .sorted(ITEM_ORDER)
                .map(item -> {
                    boolean itemCancelled = booking.isCancelled() || cancelledItemIds.contains(item.getItemId());
                    return toItem(
                            item,
                            itemCancelled,
                            imagesByRoomType.get(item.getRoomTypeId()),
                            addonsByItem.getOrDefault(item.getItemId(), List.of()).stream()
                                    .sorted(Comparator.comparing(BookingAddon::getName))
                                    .map(addon -> toExtra(addon, itemCancelled || cancelledAddonIds.contains(addon.getAddonId())))
                                    .toList());
                })
                .toList();

        return new BookingResponse(
                booking.getBookingId(),
                booking.getStartTime(),
                booking.getNights(),
                booking.getCurrency(),
                booking.getTotalPrice(),
                booking.isCancelled(),
                items);
    }

    /**
     * Builds the response for one booked room.
     *
     * @param item      the booked room
     * @param cancelled true if the room is cancelled
     * @param image     the room type's primary image, or null if there is none or it could not be fetched
     * @param extras    the room's extras, already converted
     * @return the response object
     */
    private BookingResponse.Item toItem(BookingItem item, boolean cancelled, InventoryPrimaryImage image, List<BookingResponse.Extra> extras){
        return new BookingResponse.Item(
                item.getItemId(),
                item.getRoomTypeId(),
                item.getRoomTypeName(),
                item.getVariantName(),
                item.getFromTime(),
                item.getNights(),
                item.getPrice(),
                cancelled,
                image == null ? ImageResponse.NONE : new ImageResponse(image.filePath(), image.altText()),
                extras);
    }

    /**
     * Builds the response for one extra on a booked room.
     *
     * @param addon     the extra
     * @param cancelled true if the extra is cancelled
     * @return the response object
     */
    private BookingResponse.Extra toExtra(BookingAddon addon, boolean cancelled){
        return new BookingResponse.Extra(addon.getName(), addon.getAmount(), addon.getPrice(), cancelled);
    }
}

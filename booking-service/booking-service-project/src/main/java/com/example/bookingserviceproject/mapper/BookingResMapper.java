package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.dto.response.BookingResponse;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.service.BookingDetails;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts a booking with its rooms and extras into a {@link BookingResponse}.
 */
@Component
public class BookingResMapper {
    private static final Comparator<BookingItem> ITEM_ORDER = Comparator.comparing(BookingItem::getFromTime)
            .thenComparing(BookingItem::getRoomNumber);

    /**
     * Builds the response for a booking.
     *
     * @param details the booking, its rooms and extras
     * @return the response object
     */
    public BookingResponse toResponse(BookingDetails details){
        Booking booking = details.booking();
        Map<UUID, List<BookingAddon>> addonsByItem = details.addons().stream()
                .collect(Collectors.groupingBy(addon -> addon.getItem().getItemId()));

        List<BookingResponse.Item> items = details.items().stream()
                .sorted(ITEM_ORDER)
                .map(item -> toItem(item, addonsByItem.getOrDefault(item.getItemId(), List.of())))
                .toList();

        return new BookingResponse(
                booking.getBookingId(),
                booking.getCreatedAt(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getCurrency(),
                booking.getTotalPrice(),
                items);
    }

    /**
     * Builds the response for one booked room, with its extras included in its total.
     *
     * @param item   the booked room
     * @param addons the extras on the room
     * @return the response object
     */
    private BookingResponse.Item toItem(BookingItem item, List<BookingAddon> addons){
        return new BookingResponse.Item(
                item.getItemId(),
                item.getRoomNumber(),
                item.getRoomTypeName(),
                item.getVariantName(),
                item.getFromTime(),
                item.getToTime(),
                item.getNights(),
                addons.stream().map(addon -> new BookingResponse.Extra(addon.getName(), addon.getAmount())).toList(),
                item.getPrice() + addons.stream().mapToInt(BookingAddon::getPrice).sum());
    }
}

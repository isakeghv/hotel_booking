package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.dto.event.BookingConfirmedNotification;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.model.BookingDetails;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts a confirmed booking into the notification the confirmation email is made from.
 */
@Component
public class BookingNotificationMapper {
    private static final Comparator<BookingItem> ITEM_ORDER = Comparator.comparing(BookingItem::getFromTime)
            .thenComparing(BookingItem::getRoomTypeName)
            .thenComparing(BookingItem::getVariantName);

    /**
     * Builds the notification for a booking.
     *
     * @param details the booking, its rooms and extras
     * @return the notification
     */
    public BookingConfirmedNotification toNotification(BookingDetails details){
        Booking booking = details.booking();
        Map<UUID, List<BookingAddon>> addonsByItem = details.addons().stream()
                .collect(Collectors.groupingBy(addon -> addon.getItem().getItemId()));

        List<BookingConfirmedNotification.Room> rooms = details.items().stream()
                .sorted(ITEM_ORDER)
                .map(item -> toRoom(item, addonsByItem.getOrDefault(item.getItemId(), List.of())))
                .toList();

        return new BookingConfirmedNotification(
                booking.getUserId(),
                booking.getBookingId(),
                booking.getCreatedAt(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getCurrency(),
                rooms,
                booking.getTotalPrice());
    }

    /**
     * Builds one room of the notification, with its extras included in its total.
     *
     * @param item   the booked room
     * @param addons the extras on the room
     * @return the room
     */
    private BookingConfirmedNotification.Room toRoom(BookingItem item, List<BookingAddon> addons){
        return new BookingConfirmedNotification.Room(
                item.getRoomTypeName(),
                item.getVariantName(),
                item.getFromTime(),
                item.getToTime(),
                item.getNights(),
                addons.stream().map(addon -> new BookingConfirmedNotification.Extra(addon.getName(), addon.getAmount())).toList(),
                item.getPrice() + addons.stream().mapToInt(BookingAddon::getPrice).sum());
    }
}

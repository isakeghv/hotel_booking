package com.example.bookingserviceproject.mapper;

import com.example.bookingserviceproject.dto.inventory.InventoryPrimaryImage;
import com.example.bookingserviceproject.dto.inventory.InventoryVariant;
import com.example.bookingserviceproject.dto.response.ImageResponse;
import com.example.bookingserviceproject.dto.response.StaffBookingResponse;
import com.example.bookingserviceproject.dto.user.BasicUserInfo;
import com.example.bookingserviceproject.model.Booking;
import com.example.bookingserviceproject.model.BookingAddon;
import com.example.bookingserviceproject.model.BookingDetails;
import com.example.bookingserviceproject.model.BookingItem;
import com.example.bookingserviceproject.model.CancelledAddon;
import com.example.bookingserviceproject.model.CancelledBy;
import com.example.bookingserviceproject.model.CancelledItem;
import com.example.bookingserviceproject.utils.RefundUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Converts a booking with its rooms, extras and cancellations into the
 * {@link StaffBookingResponse} shown to staff.
 * <p>
 * <b>Totals:</b> a room or extra cancelled on its own, at least
 * {@link RefundUtils#REFUND_DEADLINE} before the booking starts, is refunded,
 * and left out of the totals. An extra on such a room is too. Cancelling the
 * whole booking does not change the totals, so staff can still see what the
 * booking was worth.
 */
@Component
public class StaffBookingResMapper {
    private final UserResMapper userResMapper;

    /**
     * Creates the mapper with the mapper it uses for the guest.
     *
     * @param userResMapper mapper from the user-service's user information to the guest
     */
    public StaffBookingResMapper(UserResMapper userResMapper){
        this.userResMapper = userResMapper;
    }

    /**
     * Builds the response for a booking. The rooms and extras keep the order they are given in.
     *
     * @param details          the booking, its rooms (sorted), extras (sorted) and cancellations
     * @param user             the user's basic information, or null if the user-service could not be asked
     * @param variants         the variants by variant id. A missing variant gets {@code includes} empty
     * @param imagesByRoomType the primary images by room type id. A missing one gets an image with null fields
     * @return the response object
     */
    public StaffBookingResponse toResponse(BookingDetails details, BasicUserInfo user,
                                           Map<UUID, InventoryVariant> variants,
                                           Map<UUID, InventoryPrimaryImage> imagesByRoomType){
        Booking booking = details.booking();
        // Grouping keeps the extras' order (by name) within each room
        Map<UUID, List<BookingAddon>> addonsByItem = details.addons().stream()
                .collect(Collectors.groupingBy(addon -> addon.getItem().getItemId()));
        Map<UUID, CancelledItem> cancelledItems = details.cancelledItems().stream()
                .collect(Collectors.toMap(cancelled -> cancelled.getItem().getItemId(), Function.identity()));
        Map<UUID, CancelledAddon> cancelledAddons = details.cancelledAddons().stream()
                .collect(Collectors.toMap(cancelled -> cancelled.getAddon().getAddonId(), Function.identity()));

        List<StaffBookingResponse.Item> items = details.items().stream()
                .map(item -> toItem(
                        booking,
                        item,
                        cancelledItems.get(item.getItemId()),
                        addonsByItem.getOrDefault(item.getItemId(), List.of()),
                        cancelledAddons,
                        variants.get(item.getVariantId()),
                        imagesByRoomType.get(item.getRoomTypeId())))
                .toList();

        return new StaffBookingResponse(
                booking.getBookingId(),
                booking.getCreatedAt(),
                userResMapper.toResponse(booking.getUserId(), user),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getNights(),
                booking.getCurrency(),
                items.stream().mapToInt(StaffBookingResponse.Item::total).sum(),
                booking.isCancelled(),
                items);
    }

    /**
     * Builds the response for one booked room, with its extras and total.
     *
     * @param booking         the booking the room belongs to
     * @param item            the booked room
     * @param cancelledItem   the room's own cancellation, or null if it was not cancelled on its own
     * @param addons          the room's extras, sorted
     * @param cancelledAddons the booking's own extra cancellations, by addon id
     * @param variant         the room's variant from inventory, or null if it was not found or could not be fetched
     * @param image           the room type's primary image, or null if there is none or it could not be fetched
     * @return the response object
     */
    private StaffBookingResponse.Item toItem(Booking booking, BookingItem item, CancelledItem cancelledItem,
                                             List<BookingAddon> addons, Map<UUID, CancelledAddon> cancelledAddons,
                                             InventoryVariant variant, InventoryPrimaryImage image){
        // A cancelled booking is an umbrella: its rooms count as cancelled, without anyone recorded as cancelling them
        boolean cancelled = booking.isCancelled() || cancelledItem != null;
        CancelledBy cancelledBy = cancelledItem == null ? null : cancelledItem.getCancelledBy();
        boolean refunded = cancelledItem != null && RefundUtils.isRefunded(cancelledItem.getCancelledAt(), booking.getStartTime());

        int total = refunded ? 0 : item.getPrice();
        List<StaffBookingResponse.Extra> extras = new ArrayList<>(addons.size());
        for (BookingAddon addon : addons){
            CancelledAddon cancelledAddon = cancelledAddons.get(addon.getAddonId());
            // An extra on a room cancelled on its own is cancelled with it, by whoever cancelled the room, at the room's time
            boolean extraRefunded = refunded
                    || cancelledAddon != null && RefundUtils.isRefunded(cancelledAddon.getCancelledAt(), booking.getStartTime());
            if (!extraRefunded) total += addon.getPrice();

            extras.add(new StaffBookingResponse.Extra(
                    addon.getExtraId(),
                    addon.getName(),
                    addon.getAmount(),
                    addon.getPrice(),
                    cancelled || cancelledAddon != null,
                    cancelledAddon != null ? cancelledAddon.getCancelledBy() : cancelledBy));
        }

        return new StaffBookingResponse.Item(
                item.getItemId(),
                item.getRoomNumber(),
                item.getRoomTypeId(),
                item.getRoomTypeName(),
                image == null ? ImageResponse.NONE : new ImageResponse(image.filePath(), image.altText()),
                toVariant(item, variant),
                item.getFromTime(),
                item.getToTime(),
                item.getNights(),
                item.getPrice(),
                total,
                cancelled,
                cancelledBy,
                item.isCheckedIn(),
                item.isCheckedOut(),
                List.copyOf(extras));
    }

    /**
     * Builds a room's variant: the name as booked, and what it includes from inventory.
     *
     * @param item    the booked room
     * @param variant the variant from inventory, or null if it was not found or could not be fetched
     * @return the variant, with {@code includes} empty if inventory gave nothing
     */
    private StaffBookingResponse.Variant toVariant(BookingItem item, InventoryVariant variant){
        List<StaffBookingResponse.Include> includes = variant == null || variant.includes() == null ? List.of()
                : variant.includes().stream()
                        .map(include -> new StaffBookingResponse.Include(include.extraId(), include.name(), include.amount()))
                        .toList();
        return new StaffBookingResponse.Variant(item.getVariantId(), item.getVariantName(), includes);
    }
}

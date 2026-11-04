package com.example.inventoryserviceproject.dto.response;

import java.util.List;
import java.util.UUID;

/**
 * A room type that is available for a customer's search, with what they need
 * to decide on it. Variants are not included: the customer picks a variant
 * after choosing the room type.
 *
 * <p>Prices are based on the room type's cheapest active variant, as no room
 * type can be booked for less than that.
 *
 * @param roomTypeId     the id of the room type
 * @param name           the name of the room type
 * @param description    the room type's main description, empty (null title, no lines) if it has none
 * @param capacity       how many people one room of this type sleeps
 * @param beds           the beds in one room of this type
 * @param image          the room type's first image, or null if it has none
 * @param availableRooms how many rooms of this type are free for the whole stay
 * @param roomsToBook    how many rooms of this type the customer needs to book to fit
 *                       their search: at least the minimum number of rooms they asked for,
 *                       and enough for all guests (e.g. 4 guests in rooms for 2 people
 *                       needs 2 rooms). {@code total} is the price for this many rooms, and
 *                       it is the amount the booking-service sends when reserving the rooms
 * @param nights         the number of nights in the stay
 * @param pricePerNight  the average price per night for one room ({@code totalPerRoom} / nights, rounded up),
 *                       so it adds up to the total also when the variant has a price per stay
 * @param totalPerRoom   the price of one room for the whole stay, including a variant price per stay
 * @param total          the price of {@code roomsToBook} rooms for the whole stay
 * @param currency       the currency of all prices, e.g. "USD"
 */
public record AvailableRoomTypeResponse(
        UUID roomTypeId,
        String name,
        DescriptionResponse description,
        int capacity,
        List<BedInfo> beds,
        Image image,
        int availableRooms,
        int roomsToBook,
        int nights,
        int pricePerNight,
        int totalPerRoom,
        int total,
        String currency
) {
    /**
     * A bed type in the room, e.g. "2 x Single bed, 1 person".
     */
    public record BedInfo(String name, int amount, int capacity) {}

    /**
     * An image of the room type.
     */
    public record Image(String filePath, String altText) {}
}

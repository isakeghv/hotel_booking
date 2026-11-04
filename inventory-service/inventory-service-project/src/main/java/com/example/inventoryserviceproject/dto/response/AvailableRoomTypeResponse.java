package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "A room type available for the search, priced with its cheapest variant")
public record AvailableRoomTypeResponse(
        @Schema(description = "Id of the room type")
        UUID roomTypeId,
        @Schema(description = "Name of the room type", example = "Deluxe Double")
        String name,
        @Schema(description = "Main description of the room type")
        DescriptionResponse description,
        @Schema(description = "How many people one room sleeps", example = "2")
        int capacity,
        @Schema(description = "Beds in one room, e.g. shown as \"2 x Single bed, 1 person\"")
        List<BedInfo> beds,
        @Schema(description = "First image of the room type, or null if it has none")
        Image image,
        @Schema(description = "How many rooms of this type are free for the whole stay", example = "5")
        int availableRooms,
        @Schema(description = "How many rooms of this type to book to fit the search: at least the minimum number of rooms asked for, and enough beds for all guests. total is the price for this many rooms, and it is the amount to send when holding the rooms", example = "2")
        int roomsToBook,
        @Schema(description = "Number of nights in the stay", example = "3")
        int nights,
        @Schema(description = "Average price per night for one room (totalPerRoom / nights, rounded up), in USD", example = "174")
        int pricePerNight,
        @Schema(description = "Price of one room for the whole stay, in USD", example = "520")
        int totalPerRoom,
        @Schema(description = "Price of roomsToBook rooms for the whole stay, in USD", example = "1040")
        int total,
        @Schema(description = "Currency of all prices", example = "USD")
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

package com.example.bookingserviceproject.dto.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * An extra priced by the inventory-service, as returned by
 * {@code GET /inventory/availability/extras/{extraId}}. Nothing is held in
 * inventory. Only the fields the booking-service uses are read: inventory's
 * {@code amount} and {@code total} are ignored, as the total is calculated for
 * the room's own nights from the unit price.
 *
 * @param extra the extra, with its price per unit
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryExtraPrice(
        Extra extra
) {
    /**
     * The extra.
     *
     * @param extraId    the id of the extra
     * @param name       the name of the extra
     * @param unitPrice  the price of one unit, per stay if fixed, otherwise per night
     * @param priceFixed true if the price is per stay, false if per night
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Extra(UUID extraId, String name, int unitPrice, boolean priceFixed) {}

    /**
     * The body of inventory's response: {@code {"extra": {...}}}.
     *
     * @param extra the priced extra
     */
    public record Result(InventoryExtraPrice extra) {}
}

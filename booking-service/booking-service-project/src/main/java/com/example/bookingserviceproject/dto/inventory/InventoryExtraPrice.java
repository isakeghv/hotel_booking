package com.example.bookingserviceproject.dto.inventory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

/**
 * An extra priced by the inventory-service, as returned by
 * {@code GET /inventory/availability/extras/{extraId}}. Nothing is held in
 * inventory. Only the fields the booking-service uses are read.
 *
 * @param extra  the extra, with its price per unit
 * @param amount how many of the extra were priced
 * @param total  the price of the extra for the whole stay, in USD
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record InventoryExtraPrice(
        Extra extra,
        int amount,
        int total
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

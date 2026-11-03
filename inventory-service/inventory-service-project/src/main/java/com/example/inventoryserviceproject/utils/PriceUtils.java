package com.example.inventoryserviceproject.utils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Price calculations for rooms. All prices are whole USD.
 */
public final class PriceUtils {
    public static final String CURRENCY = "USD";

    private PriceUtils(){}

    /**
     * Calculates the number of nights in a stay. A stay from the 5th to the
     * 8th is 3 nights.
     *
     * @param from the check-in date
     * @param to   the check-out date
     * @return the number of nights
     */
    public static int nights(LocalDate from, LocalDate to){
        return (int) ChronoUnit.DAYS.between(from, to);
    }

    /**
     * @param pricePerNight the room type's price per night
     * @param nights        the number of nights
     * @return the price of the room for the whole stay
     */
    public static int roomTotal(int pricePerNight, int nights){
        return pricePerNight * nights;
    }

    /**
     * @param variantPrice the variant's price
     * @param isPriceFixed true if the price is per stay, false if per night
     * @param nights       the number of nights
     * @return the price of the variant for the whole stay
     */
    public static int variantTotal(int variantPrice, boolean isPriceFixed, int nights){
        return isPriceFixed ? variantPrice : variantPrice * nights;
    }

    /**
     * @param roomTotal    the price of the room for the whole stay
     * @param variantTotal the price of the variant for the whole stay
     * @return the total price of one room with its variant
     */
    public static int total(int roomTotal, int variantTotal){
        return roomTotal + variantTotal;
    }
}

package com.example.bookingserviceproject.utils;

/**
 * Price constants and calculations. All prices are whole USD, and always come
 * from the inventory-service, never from the client.
 */
public final class PriceUtils {
    public static final String CURRENCY = "USD";

    private PriceUtils(){}

    /**
     * Calculates the price of an extra, the same way the inventory-service does.
     *
     * @param unitPrice  the price of one unit
     * @param priceFixed true if the price is per stay, false if per night
     * @param amount     how many of the extra
     * @param nights     the number of nights
     * @return the price of the extra for the whole stay
     */
    public static int extraTotal(int unitPrice, boolean priceFixed, int amount, int nights){
        return priceFixed ? unitPrice * amount : unitPrice * amount * nights;
    }
}

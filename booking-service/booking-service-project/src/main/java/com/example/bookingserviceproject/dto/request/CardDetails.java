package com.example.bookingserviceproject.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.LuhnCheck;
import org.hibernate.validator.constraints.Range;

import java.time.YearMonth;

/**
 * Card details for an online card payment. Only the format is checked here,
 * to give clear errors early: the payment-service decides whether the card can
 * be charged. Card data is sensitive: it is never logged or stored, and
 * {@link #toString()} hides it.
 *
 * @param number      the card number, 12-19 digits, passing the Luhn checksum
 * @param expiryMonth the expiry month, 1-12
 * @param expiryYear  the expiry year, four digits
 * @param cvc         the security code, 3-4 digits
 * @param holderName  the cardholder's name, as printed on the card
 */
public record CardDetails(
        @NotBlank(message = "Card number is required")
        @Pattern(regexp = "\\d{12,19}", message = "Card number must be 12 to 19 digits, without spaces")
        @LuhnCheck(message = "Card number is not valid")
        String number,

        // Wrapper types so a missing value is null and caught by @NotNull, instead of silently becoming 0
        @NotNull(message = "Expiry month is required")
        @Range(min = 1, max = 12, message = "Expiry month must be between 1 and 12")
        Integer expiryMonth,

        @NotNull(message = "Expiry year is required")
        @Range(min = 2000, max = 2099, message = "Expiry year must be four digits, e.g. 2028")
        Integer expiryYear,

        @NotBlank(message = "Security code is required")
        @Pattern(regexp = "\\d{3,4}", message = "Security code must be 3 or 4 digits")
        String cvc,

        @NotBlank(message = "Cardholder name is required")
        @Size(max = 100, message = "Cardholder name can be at most 100 characters")
        String holderName
) {
    /**
     * Used by validation to refuse an expired card. A card is valid until the
     * end of its expiry month. Missing or invalid values are left to the other checks.
     *
     * @return true if the card has not expired, or if the expiry is missing or invalid
     */
    @AssertTrue(message = "The card has expired")
    public boolean isNotExpired(){
        // If expiry month/year is missing, or month is less than 1 or more than 12, 
        // it cannot check if the expiry is before current month, and therefore it returns true
        if (expiryMonth == null || expiryYear == null || expiryMonth < 1 || expiryMonth > 12) return true;
        return !YearMonth.of(expiryYear, expiryMonth).isBefore(YearMonth.now());
    }

    /**
     * @return the last 4 digits of the card number, for logs and receipts
     */
    public String last4(){
        return number == null || number.length() < 4 ? "????" : number.substring(number.length() - 4);
    }

    /**
     * Hides the card data, so it never ends up in logs. A record prints every field by default.
     *
     * @return the card with everything but the last 4 digits hidden
     */
    @Override
    public String toString(){
        return "CardDetails[number=****" + last4() + ", cvc=***, holderName=***]";
    }
}

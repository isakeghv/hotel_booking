package com.example.bookingserviceproject.client;

import com.example.bookingserviceproject.dto.payment.PaymentResult;
import com.example.bookingserviceproject.dto.request.CardDetails;
import com.example.bookingserviceproject.exception.PassThroughException;
import com.example.bookingserviceproject.exception.PaymentUnavailableException;

import java.util.UUID;

/**
 * Calls to the payment-service. A placeholder is used until the payment-service
 * exists, and the real client replaces it without changing the code that uses it.
 * See payment-service-notes.md for what the payment-service must do.
 */
public interface PaymentClient {

    /**
     * Charges a booking. The bookingId is the payment's key: if the booking was
     * already charged successfully, the same payment is returned without a new charge.
     *
     * @param userId    the id of the user who pays
     * @param bookingId the cart's bookingId
     * @param amount    the cart's total, in whole units of the currency
     * @param currency  the currency
     * @param card      the card to pay with. Never logged or stored
     * @return the successful payment
     * @throws PassThroughException        if the payment is refused: 402 declined, 400 invalid card, 409 already in progress
     * @throws PaymentUnavailableException if the payment-service does not answer in time, or fails
     */
    PaymentResult charge(UUID userId, UUID bookingId, int amount, String currency, CardDetails card);

    /**
     * Tells the payment-service that a booking could not be saved after it was
     * charged, so it refunds the payment in full. Safe to call twice.
     *
     * @param bookingId the bookingId of the payment
     */
    void refundFailedBooking(UUID bookingId);
}

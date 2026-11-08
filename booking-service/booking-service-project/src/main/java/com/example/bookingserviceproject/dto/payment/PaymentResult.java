package com.example.bookingserviceproject.dto.payment;

import java.util.UUID;

/**
 * A successful payment.
 *
 * @param paymentId the payment-service's id of the payment
 */
public record PaymentResult(UUID paymentId) {}

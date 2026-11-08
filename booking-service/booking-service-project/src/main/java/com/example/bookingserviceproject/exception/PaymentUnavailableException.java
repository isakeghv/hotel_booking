package com.example.bookingserviceproject.exception;

/**
 * Thrown when the payment-service does not answer in time, or fails (5xx).
 * The outcome of the payment is then unknown, so the call is retried once with
 * the same bookingId, and the circuit breaker counts it as a failure.
 */
public class PaymentUnavailableException extends RuntimeException{
    /**
     * Creates the exception with a message for the logs.
     *
     * @param message what went wrong
     */
    public PaymentUnavailableException(String message) {super(message); }
}

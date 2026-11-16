package com.example.bookingserviceproject.model;

/**
 * Who cancelled a room or an extra. Reported to the payment-service, which
 * decides the refund. Never shown to the customer.
 */
public enum CancelledBy {
    CUSTOMER,
    STAFF
}

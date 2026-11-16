package com.example.bookingserviceproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A cancelled extra. Only cancelled extras get a row, so an extra is cancelled
 * exactly when it has one. The database refuses a second row for the same extra.
 * Cancelling a room also cancels its extras.
 */
@Entity
@Table(name = "cancelled_addons")
public class CancelledAddon {

    // Identifies this cancellation inside the booking-service. The payment-service is told the addon_id instead
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cancellationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "addon_id", nullable = false, unique = true)
    private BookingAddon addon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private Instant cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CancelledBy cancelledBy;

    /**
     * Required by JPA.
     */
    protected CancelledAddon(){}

    /**
     * Creates the cancellation of an extra. The id is created when it is saved.
     *
     * @param addon       the cancelled extra
     * @param booking     the booking the extra belongs to
     * @param cancelledAt when it was cancelled
     * @param cancelledBy who cancelled it
     */
    public CancelledAddon(BookingAddon addon, Booking booking, Instant cancelledAt, CancelledBy cancelledBy){
        this.addon = addon;
        this.booking = booking;
        this.cancelledAt = cancelledAt;
        this.cancelledBy = cancelledBy;
    }

    public UUID getCancellationId(){
        return cancellationId;
    }

    public BookingAddon getAddon(){
        return addon;
    }

    public Booking getBooking(){
        return booking;
    }

    public Instant getCancelledAt(){
        return cancelledAt;
    }

    public CancelledBy getCancelledBy(){
        return cancelledBy;
    }
}

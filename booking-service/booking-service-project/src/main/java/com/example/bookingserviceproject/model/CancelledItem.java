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
 * A cancelled room. Only cancelled rooms get a row, so a room is cancelled
 * exactly when it has one. The database refuses a second row for the same room.
 */
@Entity
@Table(name = "cancelled_items")
public class CancelledItem {

    // Identifies this cancellation inside the booking-service. The payment-service is told the item_id instead
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID cancellationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false, unique = true)
    private BookingItem item;

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
    protected CancelledItem(){}

    /**
     * Creates the cancellation of a room. The id is created when it is saved.
     *
     * @param item        the cancelled room
     * @param booking     the booking the room belongs to
     * @param cancelledAt when it was cancelled
     * @param cancelledBy who cancelled it
     */
    public CancelledItem(BookingItem item, Booking booking, Instant cancelledAt, CancelledBy cancelledBy){
        this.item = item;
        this.booking = booking;
        this.cancelledAt = cancelledAt;
        this.cancelledBy = cancelledBy;
    }

    public UUID getCancellationId(){
        return cancellationId;
    }

    public BookingItem getItem(){
        return item;
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

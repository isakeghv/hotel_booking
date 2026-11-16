package com.example.bookingserviceproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * A paid booking. Bookings are never deleted: cancelled rooms and extras are
 * recorded in their own tables, and {@code cancelled} is set when every room
 * of the booking is cancelled.
 * <p>
 * The rooms and extras are not mapped as collections here: they are loaded
 * with their own repositories, one query per table, so paging stays in the database.
 */
@Entity
@Table(name = "bookings")
public class Booking extends AssignedIdEntity {

    // The cart's bookingId, created when the first room was added
    @Id
    private UUID bookingId;

    @Column(nullable = false)
    private UUID userId;

    // Earliest check-in of the booking's rooms (local hotel time)
    @Column(nullable = false)
    private LocalDateTime startTime;

    // Latest check-out of the booking's rooms (local hotel time)
    @Column(nullable = false)
    private LocalDateTime endTime;

    // Whole units of the currency, rooms and extras included
    @Column(nullable = false)
    private int totalPrice;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean cancelled;

    /**
     * Required by JPA.
     */
    protected Booking(){}

    /**
     * Creates a new, not cancelled booking.
     *
     * @param bookingId  the cart's bookingId
     * @param userId     the id of the user who booked
     * @param startTime  the earliest check-in of the booking's rooms
     * @param endTime    the latest check-out of the booking's rooms
     * @param totalPrice the price of the whole booking
     * @param currency   the currency of the price
     * @param createdAt  when the booking was made
     */
    public Booking(UUID bookingId, UUID userId, LocalDateTime startTime, LocalDateTime endTime,
                   int totalPrice, String currency, Instant createdAt){
        this.bookingId = bookingId;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalPrice = totalPrice;
        this.currency = currency;
        this.createdAt = createdAt;
        this.cancelled = false;
    }

    @Override
    public UUID getId(){
        return bookingId;
    }

    public UUID getBookingId(){
        return bookingId;
    }

    public UUID getUserId(){
        return userId;
    }

    public LocalDateTime getStartTime(){
        return startTime;
    }

    public LocalDateTime getEndTime(){
        return endTime;
    }

    public int getTotalPrice(){
        return totalPrice;
    }

    public String getCurrency(){
        return currency;
    }

    public Instant getCreatedAt(){
        return createdAt;
    }

    public boolean isCancelled(){
        return cancelled;
    }

    /**
     * @return the number of nights, from the earliest check-in date to the latest check-out date
     */
    public int getNights(){
        return (int) ChronoUnit.DAYS.between(startTime.toLocalDate(), endTime.toLocalDate());
    }
}

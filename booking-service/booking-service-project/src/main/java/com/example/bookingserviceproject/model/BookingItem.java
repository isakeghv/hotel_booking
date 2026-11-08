package com.example.bookingserviceproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * One booked room. The names and the room number are copies from the
 * inventory-service at booking time, so lists and emails need no calls to
 * inventory, and old bookings keep showing what was booked.
 */
@Entity
@Table(name = "booking_items")
public class BookingItem extends AssignedIdEntity {

    // The same id as inventory's occupation of the room (the cart's itemId)
    @Id
    private UUID itemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(nullable = false)
    private int roomNumber;

    // Refers to the inventory-service's database (no foreign key)
    @Column(nullable = false)
    private UUID roomTypeId;

    @Column(nullable = false, length = 40)
    private String roomTypeName;

    // Refers to the inventory-service's database (no foreign key)
    @Column(nullable = false)
    private UUID variantId;

    @Column(nullable = false, length = 50)
    private String variantName;

    // Check-in (local hotel time)
    @Column(nullable = false)
    private LocalDateTime fromTime;

    // Check-out (local hotel time)
    @Column(nullable = false)
    private LocalDateTime toTime;

    // The room's price for the whole stay, without extras
    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private boolean checkedIn;

    @Column(nullable = false)
    private boolean checkedOut;

    /**
     * Required by JPA.
     */
    protected BookingItem(){}

    /**
     * Creates a booked room that is not checked in yet.
     *
     * @param itemId       the cart's itemId
     * @param booking      the booking the room belongs to
     * @param roomNumber   the room number
     * @param roomTypeId   the id of the room type in the inventory-service
     * @param roomTypeName the name of the room type
     * @param variantId    the id of the variant in the inventory-service
     * @param variantName  the name of the variant
     * @param fromTime     check-in date and time
     * @param toTime       check-out date and time
     * @param price        the room's price for the whole stay, without extras
     */
    public BookingItem(UUID itemId, Booking booking, int roomNumber, UUID roomTypeId, String roomTypeName,
                       UUID variantId, String variantName, LocalDateTime fromTime, LocalDateTime toTime, int price){
        this.itemId = itemId;
        this.booking = booking;
        this.roomNumber = roomNumber;
        this.roomTypeId = roomTypeId;
        this.roomTypeName = roomTypeName;
        this.variantId = variantId;
        this.variantName = variantName;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.price = price;
        this.checkedIn = false;
        this.checkedOut = false;
    }

    @Override
    public UUID getId(){
        return itemId;
    }

    public UUID getItemId(){
        return itemId;
    }

    public Booking getBooking(){
        return booking;
    }

    public int getRoomNumber(){
        return roomNumber;
    }

    public UUID getRoomTypeId(){
        return roomTypeId;
    }

    public String getRoomTypeName(){
        return roomTypeName;
    }

    public UUID getVariantId(){
        return variantId;
    }

    public String getVariantName(){
        return variantName;
    }

    public LocalDateTime getFromTime(){
        return fromTime;
    }

    public LocalDateTime getToTime(){
        return toTime;
    }

    public int getPrice(){
        return price;
    }

    public boolean isCheckedIn(){
        return checkedIn;
    }

    public boolean isCheckedOut(){
        return checkedOut;
    }

    /**
     * @return the number of nights, counted from the check-in date to the check-out date
     */
    public int getNights(){
        return (int) ChronoUnit.DAYS.between(fromTime.toLocalDate(), toTime.toLocalDate());
    }
}

package com.example.bookingserviceproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * One extra on a booked room. An extra belongs to one room, and also stores
 * the booking, so all extras of a booking load with one query.
 */
@Entity
@Table(name = "booking_addons")
public class BookingAddon {

    // Created when the booking is saved: the cart identifies an extra by its room and extra id
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID addonId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private BookingItem item;

    // Refers to the inventory-service's database (no foreign key)
    @Column(nullable = false)
    private UUID extraId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int amount;

    // The extra's price for the room's whole stay
    @Column(nullable = false)
    private int price;

    /**
     * Required by JPA.
     */
    protected BookingAddon(){}

    /**
     * Creates an extra on a booked room. The id is created when it is saved.
     *
     * @param booking the booking the extra belongs to
     * @param item    the room the extra belongs to
     * @param extraId the id of the extra in the inventory-service
     * @param name    the name of the extra
     * @param amount  how many of the extra
     * @param price   the extra's price for the room's whole stay
     */
    public BookingAddon(Booking booking, BookingItem item, UUID extraId, String name, int amount, int price){
        this.booking = booking;
        this.item = item;
        this.extraId = extraId;
        this.name = name;
        this.amount = amount;
        this.price = price;
    }

    public UUID getAddonId(){
        return addonId;
    }

    public Booking getBooking(){
        return booking;
    }

    public BookingItem getItem(){
        return item;
    }

    public UUID getExtraId(){
        return extraId;
    }

    public String getName(){
        return name;
    }

    public int getAmount(){
        return amount;
    }

    public int getPrice(){
        return price;
    }
}

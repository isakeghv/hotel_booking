package com.example.inventoryserviceproject.model;


import com.example.inventoryserviceproject.dto.ReviewNewExtra;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity for an extra, an add-on item that can be linked to a room type or
 * offered as an add-on, such as breakfast or an extra towel.
 */
@Entity
@Table(name = "room_extras")
public class Extra {
    @Id
    private UUID extraId;
    private String name;
    private int price;
    private boolean addonPossible;
    private boolean isActive;
    private boolean isPriceFixed;

    /**
     * Creates an empty extra. Required by JPA.
     */
    public Extra(){}

    /**
     * Creates an extra with all values specified.
     *
     * @param extraId       the unique id of the extra
     * @param name          the display name of the extra
     * @param price         the price of the extra
     * @param addonPossible whether the extra can be added on to a booking
     * @param isActive      whether the extra is currently available
     * @param isPriceFixed  whether the price is fixed rather than depending on the amount or stay length
     */
    public Extra(UUID extraId, String name, int price, boolean addonPossible, boolean isActive, boolean isPriceFixed){
        this.extraId = extraId;
        this.name = name;
        this.price = price;
        this.addonPossible = addonPossible;
        this.isActive = isActive;
        this.isPriceFixed = isPriceFixed;
    }

    /**
     * Creates a new extra from a validated request, with a randomly generated id.
     *
     * @param request the validated details of the new extra
     */
    public Extra(ReviewNewExtra request){
        this.extraId = UUID.randomUUID();
        this.name = request.name();
        this.price = request.price();
        this.addonPossible = request.addonPossible();
        this.isActive = request.isActive();
        this.isPriceFixed = request.isPriceFixed();
    }

    /**
     * @return the unique id of the extra
     */
    public UUID getExtraId(){ return extraId; }

    /**
     * @param extraId the unique id of the extra
     */
    public void setExtraId(UUID extraId){ this.extraId = extraId; }

    /**
     * @return the display name of the extra
     */
    public String getName(){ return name; }

    /**
     * @param name the display name of the extra
     */
    public void setName(String name){ this.name = name; }

    /**
     * @return the price of the extra
     */
    public int getPrice() { return price; }

    /**
     * @return true if the extra can be added on to a booking
     */
    public Boolean getAddonPossible(){ return addonPossible; }

    /**
     * @return true if the extra is currently active
     */
    public Boolean getIsActive(){ return isActive; }

    /**
     * @return true if the price of the extra is fixed
     */
    public Boolean getIsPriceFixed(){ return isPriceFixed; }

    /**
     * Sets whether the price of the extra is fixed.
     *
     * @param isPriceFixed true if the price is fixed
     */
    public void setPriceFixed(boolean isPriceFixed){ this.isPriceFixed = isPriceFixed; }
}
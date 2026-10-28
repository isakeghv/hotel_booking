package com.example.inventoryserviceproject.model;


import com.example.inventoryserviceproject.dto.ReviewNewBedType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity for a bed type in the inventory, such as "King" or "Single",
 * with its dimensions and how many guests it sleeps.
 */
@Entity
@Table(name = "bed_types")
public class Bed {
    @Id
    private UUID bedId;
    private int width;
    private int height;
    private int capacity;
    private String name;

    /**
     * Creates an empty bed. Required by JPA.
     */
    public Bed(){}

    /**
     * Creates a bed with all values specified.
     *
     * @param bedId    the unique id of the bed type
     * @param width    the width of the bed
     * @param height   the height of the bed
     * @param capacity the number of guests the bed sleeps
     * @param name     the display name of the bed type
     */
    public Bed(UUID bedId, int width, int height, int capacity, String name){
        this.bedId = bedId;
        this.width = width;
        this.height = height;
        this.capacity = capacity;
        this.name = name;
    }

    /**
     * Creates a new bed from a validated request, with a randomly generated id.
     *
     * @param request the validated details of the new bed type
     */
    public Bed(ReviewNewBedType request){
        this.bedId = UUID.randomUUID();
        this.width = request.width();
        this.capacity = request.capacity();
        this.height = request.height();
        this.name = request.name();
    }

    /**
     * @return the unique id of the bed type
     */
    public UUID getBedId(){ return bedId; }

    /**
     * @param bedId the unique id of the bed type
     */
    public void setBedId(UUID bedId){ this.bedId = bedId; }

    /**
     * @return the width of the bed
     */
    public int getWidth(){ return width; }

    /**
     * @param width the width of the bed
     */
    public void setWidth(int width){ this.width = width; }

    /**
     * @return the height of the bed
     */
    public int getHeight(){ return height; }

    /**
     * @param height the height of the bed
     */
    public void setHeight(int height){ this.height = height; }

    /**
     * @return the number of guests the bed sleeps
     */
    public int getCapacity(){ return capacity; }

    /**
     * @param capacity the number of guests the bed sleeps
     */
    public void setCapacity(int capacity){ this.capacity = capacity; }

    /**
     * @return the display name of the bed type
     */
    public String getName(){ return name; }

    /**
     * @param name the display name of the bed type
     */
    public void setName(String name){ this.name = name; }
}
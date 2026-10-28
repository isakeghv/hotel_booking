package com.example.inventoryserviceproject.model;

import com.example.inventoryserviceproject.dto.ReviewNewRoomType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity for a type of room that can be booked, with its base price and
 * capacity. The capacity is derived from the beds linked to the room type.
 */
@Entity
@Table(name = "room_type")
public class RoomType {
    @Id
    private UUID typeId;
    private int roomPrice;
    private String name;
    private int capacity;
    private boolean isActive;

    /**
     * Creates an empty room type. Required by JPA.
     */
    public RoomType(){}

    /**
     * Creates a room type with all values specified.
     *
     * @param typeId    the unique id of the room type
     * @param roomPrice the base price of the room
     * @param name      the display name of the room type
     * @param capacity  the number of guests the room sleeps
     * @param isActive  whether the room type is currently available
     */
    public RoomType(UUID typeId, int roomPrice, String name, int capacity, boolean isActive){
        this.typeId = typeId;
        this.roomPrice = roomPrice;
        this.name = name;
        this.capacity = capacity;
        this.isActive = isActive;
    }

    /**
     * Creates a new room type from a validated request, with a randomly
     * generated id. The capacity is not set here, since it is calculated from
     * the beds added to the room.
     *
     * @param request the validated details of the new room type
     */
    public RoomType(ReviewNewRoomType request){
        this.typeId = UUID.randomUUID();
        this.roomPrice = request.roomPrice();
        this.name = request.name();
        this.isActive = request.isActive();
    }

    /**
     * @return the unique id of the room type
     */
    public UUID getTypeId(){ return typeId; }

    /**
     * @param typeId the unique id of the room type
     */
    public void setTypeId(UUID typeId) { this.typeId = typeId; }

    /**
     * @return the base price of the room
     */
    public int getRoomPrice() { return roomPrice; }

    /**
     * @param roomPrice the base price of the room
     */
    public void setRoomPrice(int roomPrice) { this.roomPrice = roomPrice; }

    /**
     * @return the display name of the room type
     */
    public String getName() { return name; }

    /**
     * @param name the display name of the room type
     */
    public void setName(String name) { this.name = name; }

    /**
     * @return true if the room type is currently available
     */
    public boolean getIsActive(){ return isActive; }

    /**
     * @param active true if the room type is currently available
     */
    public void setActive(boolean active) { isActive = active; }

    /**
     * @return the number of guests the room sleeps
     */
    public int getCapacity(){ return capacity; }

    /**
     * @param capacity the number of guests the room sleeps
     */
    public void setCapacity(int capacity){ this.capacity = capacity; }

}
package com.example.inventoryserviceproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity for a single physical room, identified by its room number and
 * belonging to a room type.
 */
@Entity
@Table(name = "room_numbers")
public class Room {
    @Id
    private UUID roomId;
    @Column(name = "type_id")
    private UUID roomTypeId;
    private int roomNumber;
    private boolean isActive;

    /**
     * Creates an empty room. Required by JPA.
     */
    public Room(){}

    /**
     * @return the unique id of the room
     */
    public UUID getRoomId() { return roomId; }

    /**
     * @return the id of the room type the room belongs to
     */
    public UUID getRoomTypeId() { return roomTypeId; }

    /**
     * @return the room number
     */
    public int getRoomNumber() { return roomNumber; }

    /**
     * @return true if the room is currently active
     */
    public boolean getIsActive() { return isActive; }
}

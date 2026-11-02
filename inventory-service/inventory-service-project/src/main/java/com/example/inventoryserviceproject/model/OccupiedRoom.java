package com.example.inventoryserviceproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity for a period in which a single room is unavailable. The period is
 * either caused by a booking, or by the room being set as unavailable, for
 * example by an employee.
 */
@Entity
@Table(name = "occupied_room")
public class OccupiedRoom {
    @Id
    private UUID occupiedId;
    private UUID roomId;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private boolean booked;

    /**
     * Creates an empty period. Required by JPA.
     */
    public OccupiedRoom(){}

    /**
     * Creates a new period for a room, with a randomly generated id.
     *
     * @param roomId   the id of the room
     * @param fromTime when the period starts
     * @param toTime   when the period ends
     * @param booked   true if caused by a booking, false if the room was set as unavailable
     */
    public OccupiedRoom(UUID roomId, LocalDateTime fromTime, LocalDateTime toTime, boolean booked){
        this.occupiedId = UUID.randomUUID();
        this.roomId = roomId;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.booked = booked;
    }

    /**
     * @return the unique id of the period
     */
    public UUID getOccupiedId() { return occupiedId; }

    /**
     * @return the id of the room
     */
    public UUID getRoomId() { return roomId; }

    /**
     * @return when the period starts
     */
    public LocalDateTime getFromTime() { return fromTime; }

    /**
     * @return when the period ends
     */
    public LocalDateTime getToTime() { return toTime; }

    /**
     * @return true if caused by a booking, false if the room was set as unavailable
     */
    public boolean getBooked() { return booked; }
}

package com.example.inventoryserviceproject.model;

import com.example.inventoryserviceproject.dto.request.ReviewNewRoom;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

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

    // Read-only relation on the same column as roomTypeId, so the room type can be
    // fetched in the same query (see OccupiedRoomRepository). roomTypeId is still what is written.
    // Ignored in JSON, as it is loaded lazily and is not part of the room's response.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private RoomType roomType;
    private int roomNumber;
    private boolean isActive;

    /**
     * Creates an empty room. Required by JPA.
     */
    public Room(){}

    /**
     * Creates a new room from a validated request, with a randomly generated id.
     *
     * @param request    the validated details of the new room
     * @param roomTypeId the id of the room type the room belongs to
     */
    public Room(ReviewNewRoom request, UUID roomTypeId){
        this.roomId = UUID.randomUUID();
        this.roomTypeId = roomTypeId;
        this.roomNumber = request.roomNumber();
        this.isActive = request.isActive();
    }

    /**
     * @return the unique id of the room
     */
    public UUID getRoomId() { return roomId; }

    /**
     * @return the id of the room type the room belongs to
     */
    public UUID getRoomTypeId() { return roomTypeId; }

    /**
     * Only set on rooms loaded from the database, not on newly created ones.
     *
     * @return the room type the room belongs to
     */
    @JsonIgnore
    public RoomType getRoomType() { return roomType; }

    /**
     * @return the room number
     */
    public int getRoomNumber() { return roomNumber; }

    /**
     * @return true if the room is currently active
     */
    public boolean getIsActive() { return isActive; }
}

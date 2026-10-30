package com.example.inventoryserviceproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for {@link RoomBed}, made up of a room type id and
 * a bed id.
 */
@Embeddable
public class RoomBedsId implements Serializable {
    @Column(name = "type_id")
    private UUID roomTypeId;
    private UUID bedId;

    /**
     * Creates an empty key. Required by JPA.
     */
    public RoomBedsId(){}

    /**
     * Creates a key from a room type id and a bed id.
     *
     * @param roomTypeId the id of the room type
     * @param bedId  the id of the bed type
     */
    public RoomBedsId(UUID roomTypeId, UUID bedId){
        this.roomTypeId = roomTypeId;
        this.bedId = bedId;
    }

    /**
     * @return the id of the room type
     */
    public UUID getRoomTypeId() { return roomTypeId; }

    /**
     * @param roomTypeId the id of the room type
     */
    public void setRoomTypeId(UUID roomTypeId) { this.roomTypeId = roomTypeId; }

    /**
     * @return the id of the bed type
     */
    public UUID getBedId() { return bedId; }

    /**
     * Compares this key to another object. Two keys are equal when both the
     * room type id and the bed id are equal.
     *
     * @param o the object to compare with
     * @return true if the object is an equal key, false otherwise
     */
    @Override
    public boolean equals(Object o){
        if ( this == o) return true;
        if ( !(o instanceof RoomBedsId that)) return false;
        return Objects.equals(roomTypeId, that.roomTypeId) && Objects.equals(bedId, that.bedId);
    }

    /**
     * Computes a hash code from the room type id and the bed id, consistent
     * with {@link #equals(Object)}.
     *
     * @return the hash code of the key
     */
    @Override
    public int hashCode(){
        return Objects.hash(roomTypeId, bedId);
    }
}

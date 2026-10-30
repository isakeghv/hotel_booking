package com.example.inventoryserviceproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for {@link RoomIncludes}, made up of a room type id
 * and an extra id.
 */
@Embeddable
public class RoomIncludesId implements Serializable {
    @Column(name = "type_id")
    private UUID roomTypeId;
    private UUID extraId;

    /**
     * Creates an empty key. Required by JPA.
     */
    public RoomIncludesId(){}

    /**
     * Creates a key from a room type id and an extra id.
     *
     * @param roomTypeId the id of the room type
     * @param extraId    the id of the extra
     */
    public RoomIncludesId(UUID roomTypeId, UUID extraId){
        this.roomTypeId = roomTypeId;
        this.extraId = extraId;
    }

    /**
     * @return the id of the room type
     */
    public UUID getRoomTypeId() { return roomTypeId; }

    /**
     * @return the id of the extra
     */
    public UUID getExtraId() { return extraId; }

    /**
     * @param roomTypeId the id of the room type
     */
    public void setRoomTypeId(UUID roomTypeId) { this.roomTypeId = roomTypeId; }

    /**
     * Compares this key to another object. Two keys are equal when both the
     * room type id and the extra id are equal.
     *
     * @param o the object to compare with
     * @return true if the object is an equal key, false otherwise
     */
    @Override
    public boolean equals(Object o){
        if ( this == o) return true;
        if ( !(o instanceof RoomIncludesId that)) return false;
        return Objects.equals(roomTypeId, that.roomTypeId) && Objects.equals(extraId, that.extraId);
    }

    /**
     * Computes a hash code from the room type id and the extra id, consistent
     * with {@link #equals(Object)}.
     *
     * @return the hash code of the key
     */
    @Override
    public int hashCode(){
        return Objects.hash(roomTypeId, extraId);
    }
}

package com.example.inventoryserviceproject.model;

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
    private UUID typeId;
    private UUID bedId;

    /**
     * Creates an empty key. Required by JPA.
     */
    public RoomBedsId(){}

    /**
     * Creates a key from a room type id and a bed id.
     *
     * @param typeId the id of the room type
     * @param bedId  the id of the bed type
     */
    public RoomBedsId(UUID typeId, UUID bedId){
        this.typeId = typeId;
        this.bedId = bedId;
    }

    /**
     * @return the id of the room type
     */
    public UUID getTypeId() { return typeId; }

    /**
     * @param typeId the id of the room type
     */
    public void setTypeId(UUID typeId) { this.typeId = typeId; }

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
        return Objects.equals(typeId, that.typeId) && Objects.equals(bedId, that.bedId);
    }

    /**
     * Computes a hash code from the room type id and the bed id, consistent
     * with {@link #equals(Object)}.
     *
     * @return the hash code of the key
     */
    @Override
    public int hashCode(){
        return Objects.hash(typeId, bedId);
    }
}

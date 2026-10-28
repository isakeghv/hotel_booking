package com.example.inventoryserviceproject.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity linking an extra to a room type, meaning the extra is included
 * with the room. Identified by a composite key of room type id and extra id.
 */
@Entity
@Table(name = "room_includes")
public class RoomIncludes {
    @EmbeddedId
    private RoomIncludesId id;

    private int amount;
    private boolean showAmount;

    /**
     * Creates an empty link. Required by JPA.
     */
    public RoomIncludes(){}

    /**
     * Creates a link between a room type and an extra.
     *
     * @param roomTypeId the id of the room type
     * @param extraId    the id of the extra
     * @param amount     the amount of the extra included with the room
     * @param showAmount whether the amount should be shown to guests
     */
    public RoomIncludes(UUID roomTypeId, UUID extraId, int amount, boolean showAmount){
        this.id = new RoomIncludesId(roomTypeId, extraId);
        this.amount = amount;
        this.showAmount = showAmount;
    }

    /**
     * @return the composite key of room type id and extra id
     */
    public RoomIncludesId getId() { return id; }

    /**
     * @return the amount of the extra included with the room
     */
    public int getAmount(){ return amount; }

    /**
     * @return true if the amount should be shown to guests
     */
    public boolean getShowAmount() { return showAmount; }

    /**
     * @return the id of the room type
     */
    public UUID getRoomTypeId(){ return id.getTypeId(); }

    /**
     * @return the id of the extra
     */
    public UUID getExtraId(){ return id.getExtraId(); }
}

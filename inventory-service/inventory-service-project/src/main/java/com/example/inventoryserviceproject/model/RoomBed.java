package com.example.inventoryserviceproject.model;

import com.example.inventoryserviceproject.dto.ReviewAddBedToRoom;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity linking a bed type to a room type, with the number of such beds
 * in the room. Identified by a composite key of room type id and bed id.
 */
@Entity
@Table(name = "room_beds")
public class RoomBed {
    @EmbeddedId
    private RoomBedsId id;

    private int amount;

    /**
     * Creates an empty link. Required by JPA.
     */
    public RoomBed(){}

    /**
     * Creates a link between a room type and a bed type from a validated request.
     *
     * @param request the validated request containing the amount
     * @param typeId  the id of the room type
     * @param bedId   the id of the bed type
     */
    public RoomBed(ReviewAddBedToRoom request,UUID typeId, UUID bedId){
        this.id = new RoomBedsId(typeId, bedId);
        this.amount = request.amount();
    }

    /**
     * @return the composite key of room type id and bed id
     */
    public RoomBedsId getId() {return id;}

    /**
     * @return the id of the bed type
     */
    public UUID getBedId() { return id.getBedId(); }

    /**
     * @return the id of the room type
     */
    public UUID getRoomTypeId() { return id.getTypeId(); }

    /**
     * @return the number of this bed type in the room
     */
    public int getAmount() { return amount; }

    /**
     * @param amount the number of this bed type in the room
     */
    public void setAmount(int amount) { this.amount = amount; }
}

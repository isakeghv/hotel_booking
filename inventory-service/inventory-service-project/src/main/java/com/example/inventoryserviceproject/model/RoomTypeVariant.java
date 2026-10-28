package com.example.inventoryserviceproject.model;


import com.example.inventoryserviceproject.dto.ReviewNewRoomVariant;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity for a variant of a room type, such as a sea-view version, with a
 * price added on top of the room's base price.
 */
@Entity
@Table(name = "room_type_variants")
public class RoomTypeVariant {
    @Id
    private UUID variantId;
    private UUID typeId;
    private String variantName;
    private int extraPrice;
    private boolean isActive;
    private boolean isPriceFixed;

    /**
     * Creates an empty variant. Required by JPA.
     */
    public RoomTypeVariant(){}

    /**
     * Creates a new variant from a validated request, with a randomly
     * generated id.
     *
     * @param variant    the validated details of the new variant
     * @param roomTypeId the id of the room type the variant belongs to
     */
    public RoomTypeVariant(ReviewNewRoomVariant variant, UUID roomTypeId){
        this.variantId = UUID.randomUUID();
        this.typeId = roomTypeId;
        this.variantName = variant.variantName();
        this.extraPrice = variant.extraPrice();
        this.isActive = variant.isActive();
        this.isPriceFixed = variant.isPriceFixed();
    }

    /**
     * @return the unique id of the variant
     */
    public UUID getVariantId() { return variantId; }

    /**
     * @return the id of the room type the variant belongs to
     */
    public UUID getRoomTypeId() { return typeId; }

    /**
     * @return the name of the variant
     */
    public String getVariantName() { return variantName; }

    /**
     * @return the price added on top of the room's base price
     */
    public int getExtraPrice() { return extraPrice; }

    /**
     * @return true if the extra price is fixed
     */
    public boolean isPriceFixed() { return isPriceFixed; }

    /**
     * @return true if the variant is currently available
     */
    public boolean isActive() { return isActive; }
}

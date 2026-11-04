package com.example.inventoryserviceproject.model;


import com.example.inventoryserviceproject.dto.request.ReviewNewRoomVariant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

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
    @Column(name = "type_id")
    private UUID roomTypeId;

    // Read-only relation on the same column as roomTypeId, so the room type can be
    // fetched in the same query (see VariantRepository). roomTypeId is still what is written.
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private RoomType roomType;

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
        this.roomTypeId = roomTypeId;
        this.variantName = variant.variantName();
        this.extraPrice = variant.extraPrice();
        this.isActive = variant.isActive();
        this.isPriceFixed = variant.isPriceFixed();
    }

    /**
     * Creates a new active variant with a randomly generated id. Used for the
     * default variant created together with a room type.
     *
     * @param roomTypeId   the id of the room type the variant belongs to
     * @param variantName  the name of the variant
     * @param extraPrice   the price added on top of the room's base price
     * @param isPriceFixed true if the extra price is per stay, false if per night
     */
    public RoomTypeVariant(UUID roomTypeId, String variantName, int extraPrice, boolean isPriceFixed){
        this.variantId = UUID.randomUUID();
        this.roomTypeId = roomTypeId;
        this.variantName = variantName;
        this.extraPrice = extraPrice;
        this.isActive = true;
        this.isPriceFixed = isPriceFixed;
    }

    /**
     * @return the unique id of the variant
     */
    public UUID getVariantId() { return variantId; }

    /**
     * @return the id of the room type the variant belongs to
     */
    public UUID getRoomTypeId() { return roomTypeId; }

    /**
     * Only set on variants loaded from the database, not on newly created ones.
     *
     * @return the room type the variant belongs to
     */
    @JsonIgnore
    public RoomType getRoomType() { return roomType; }

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

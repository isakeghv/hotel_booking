package com.example.inventoryserviceproject.model;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entity linking an extra to a room type variant, meaning the extra is
 * included with the variant. Identified by a composite key of variant id and
 * extra id.
 */
@Entity
@Table(name = "variants_include")
public class VariantIncludes {
    @EmbeddedId
    private VariantIncludesId id;

    private int amount;
    private boolean showAmount;

    /**
     * Creates an empty link. Required by JPA.
     */
    public VariantIncludes(){}

    /**
     * Creates a link between a variant and an extra.
     *
     * @param variantId  the id of the variant
     * @param extraId    the id of the extra
     * @param amount     the amount of the extra included with the variant
     * @param showAmount whether the amount should be shown to guests
     */
    public VariantIncludes(UUID variantId, UUID extraId, int amount, boolean showAmount){
        this.id = new VariantIncludesId(variantId, extraId);
        this.amount = amount;
        this.showAmount = showAmount;
    }

    /**
     * @return the composite key of variant id and extra id
     */
    public VariantIncludesId getId() { return id; }

    /**
     * @return the amount of the extra included with the variant
     */
    public int getAmount(){ return amount; }

    /**
     * @return true if the amount should be shown to guests
     */
    public boolean getShowAmount() { return showAmount; }

    /**
     * @return the id of the variant
     */
    public UUID getVariantId(){ return id.getVariantId(); }

    /**
     * @return the id of the extra
     */
    public UUID getExtraId(){ return id.getExtraId(); }
}

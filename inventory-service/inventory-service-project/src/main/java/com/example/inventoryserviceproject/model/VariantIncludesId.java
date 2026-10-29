package com.example.inventoryserviceproject.model;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Composite primary key for {@link VariantIncludes}, made up of a variant id
 * and an extra id.
 */
@Embeddable
public class VariantIncludesId implements Serializable {
    private UUID variantId;
    private UUID extraId;

    /**
     * Creates an empty key. Required by JPA.
     */
    public VariantIncludesId(){}

    /**
     * Creates a key from a variant id and an extra id.
     *
     * @param variantId the id of the variant
     * @param extraId   the id of the extra
     */
    public VariantIncludesId(UUID variantId, UUID extraId){
        this.variantId = variantId;
        this.extraId = extraId;
    }

    /**
     * @return the id of the variant
     */
    public UUID getVariantId() { return variantId; }

    /**
     * @return the id of the extra
     */
    public UUID getExtraId() { return extraId; }

    /**
     * Compares this key to another object. Two keys are equal when both the
     * variant id and the extra id are equal.
     *
     * @param o the object to compare with
     * @return true if the object is an equal key, false otherwise
     */
    @Override
    public boolean equals(Object o){
        if ( this == o) return true;
        if ( !(o instanceof VariantIncludesId that)) return false;
        return Objects.equals(variantId, that.variantId) && Objects.equals(extraId, that.extraId);
    }

    /**
     * Computes a hash code from the variant id and the extra id, consistent
     * with {@link #equals(Object)}.
     *
     * @return the hash code of the key
     */
    @Override
    public int hashCode(){
        return Objects.hash(variantId, extraId);
    }
}

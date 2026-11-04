package com.example.inventoryserviceproject.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Entity for a piece of information about a variant, with a title and up to
 * three lines. One of them can be the variant's main description, shown
 * when customers choose between the variants of a room type.
 */
@Entity
@Table(name = "variant_information")
public class VariantInformation {
    @Id
    private UUID informationId;
    private UUID variantId;
    private String title;
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] information;
    private boolean isMainDescription;

    /**
     * Creates an empty piece of information. Required by JPA.
     */
    public VariantInformation(){}

    /**
     * @return the unique id of the information
     */
    public UUID getInformationId() { return informationId; }

    /**
     * @return the id of the variant the information belongs to
     */
    public UUID getVariantId() { return variantId; }

    /**
     * @return the title of the information
     */
    public String getTitle() { return title; }

    /**
     * @return the lines of information, at most three
     */
    public String[] getInformation() { return information; }

    /**
     * @return true if this is the variant's main description
     */
    public boolean getIsMainDescription() { return isMainDescription; }
}

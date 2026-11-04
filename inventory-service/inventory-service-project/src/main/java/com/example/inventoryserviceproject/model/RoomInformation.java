package com.example.inventoryserviceproject.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Entity for a piece of information about a room type, with a title and up to
 * three lines. One of them can be the room type's main description, shown
 * when customers search for available rooms.
 */
@Entity
@Table(name = "room_information")
public class RoomInformation {
    @Id
    private UUID informationId;
    @Column(name = "type_id")
    private UUID roomTypeId;
    private String title;
    @JdbcTypeCode(SqlTypes.ARRAY)
    private String[] information;
    private boolean isMainDescription;

    /**
     * Creates an empty piece of information. Required by JPA.
     */
    public RoomInformation(){}

    /**
     * @return the unique id of the information
     */
    public UUID getInformationId() { return informationId; }

    /**
     * @return the id of the room type the information belongs to
     */
    public UUID getRoomTypeId() { return roomTypeId; }

    /**
     * @return the title of the information
     */
    public String getTitle() { return title; }

    /**
     * @return the lines of information, at most three
     */
    public String[] getInformation() { return information; }

    /**
     * @return true if this is the room type's main description
     */
    public boolean getIsMainDescription() { return isMainDescription; }
}

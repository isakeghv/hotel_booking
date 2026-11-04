package com.example.inventoryserviceproject.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * The main description of a room type or variant. When there is no main
 * description, the title is null and the information is empty, so clients
 * never have to handle a missing description object.
 *
 * @param title       the title of the description, or null if there is none
 * @param information the lines of the description, at most three, or empty if there is none
 */
@Schema(description = "Main description. When there is none, title is null and information is empty, never missing")
public record DescriptionResponse(String title, List<String> information) {

    /**
     * @return a description for when there is no main description
     */
    public static DescriptionResponse empty(){
        return new DescriptionResponse(null, List.of());
    }
}

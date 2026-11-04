package com.example.inventoryserviceproject.mapper;

import com.example.inventoryserviceproject.dto.response.DescriptionResponse;
import com.example.inventoryserviceproject.model.RoomInformation;
import com.example.inventoryserviceproject.model.VariantInformation;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Converts main descriptions of room types and variants into {@link DescriptionResponse} objects.
 */
@Component
public class DescriptionResMapper {

    /**
     * @param information the room type's main description, or null if it has none
     * @return the description, or an empty description if there is none
     */
    public DescriptionResponse toResponse(RoomInformation information){
        if (information == null) return DescriptionResponse.empty();
        return toResponse(information.getTitle(), information.getInformation());
    }

    /**
     * @param information the variant's main description, or null if it has none
     * @return the description, or an empty description if there is none
     */
    public DescriptionResponse toResponse(VariantInformation information){
        if (information == null) return DescriptionResponse.empty();
        return toResponse(information.getTitle(), information.getInformation());
    }

    /**
     * @param title the title of the description
     * @param lines the lines of the description, may be null
     * @return the description, with an empty list instead of null lines
     */
    private DescriptionResponse toResponse(String title, String[] lines){
        return new DescriptionResponse(title, lines == null ? List.of() : List.of(lines));
    }
}

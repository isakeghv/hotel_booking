package com.example.inventoryserviceproject.mapper;

import com.example.inventoryserviceproject.dto.response.OccupiedRoomResponse;
import com.example.inventoryserviceproject.model.OccupiedRoom;
import com.example.inventoryserviceproject.model.Room;
import com.example.inventoryserviceproject.model.RoomType;
import org.springframework.stereotype.Component;

/**
 * Converts occupied room entities into {@link OccupiedRoomResponse} response objects.
 */
@Component
public class OccupiedRoomResMapper {

    /**
     * Builds the response object for a single period. The period must be loaded
     * with its room and room type, e.g. through an {@code @EntityGraph}, otherwise
     * each access to them runs an extra query.
     *
     * @param occupation the period, with its room and room type loaded
     * @return the response object
     */
    public OccupiedRoomResponse toResponse(OccupiedRoom occupation){
        Room room = occupation.getRoom();
        RoomType roomType = room.getRoomType();

        return new OccupiedRoomResponse(
                occupation.getOccupiedId(),
                occupation.getFromTime(),
                occupation.getToTime(),
                new OccupiedRoomResponse.RoomInfo(room.getRoomId(), room.getRoomNumber()),
                new OccupiedRoomResponse.RoomTypeInfo(roomType.getRoomTypeId(), roomType.getName()));
    }
}

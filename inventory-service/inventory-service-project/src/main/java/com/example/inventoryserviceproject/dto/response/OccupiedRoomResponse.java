package com.example.inventoryserviceproject.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record OccupiedRoomResponse(
        UUID occupiedId,
        LocalDateTime from,
        LocalDateTime to,
        RoomInfo room,
        RoomTypeInfo roomType
) {
    /**
     * The single room that is unavailable.
     */
    public record RoomInfo(UUID roomId, int roomNumber) {}

    /**
     * The room type the unavailable room belongs to.
     */
    public record RoomTypeInfo(UUID roomTypeId, String name) {}
}

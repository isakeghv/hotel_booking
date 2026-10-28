package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.ReviewAddBedToRoom;
import com.example.inventoryserviceproject.dto.ReviewNewRoomType;
import com.example.inventoryserviceproject.model.RoomType;
import com.example.inventoryserviceproject.dto.response.RoomTypeResponse;
import com.example.inventoryserviceproject.service.RoomBedsService;
import com.example.inventoryserviceproject.service.RoomTypeService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing room types, including their beds and
 * linked extras.
 */
@RestController
@RequestMapping("/inventory")
public class RoomController {
    private final RoomTypeService roomTypeService;
    private final RoomBedsService roomBedsService;

    /**
     * Creates the controller with the services it delegates to.
     *
     * @param roomTypeService service containing the room type business logic
     * @param roomBedsService service managing the beds in each room type
     */
    public RoomController(RoomTypeService roomTypeService, RoomBedsService roomBedsService){
        this.roomTypeService = roomTypeService;
        this.roomBedsService = roomBedsService;
    }

    /**
     * Creates a new room type.
     *
     * @param request the validated details of the new room type
     * @return a response with the created room type
     */
    @PostMapping("/roomtypes")
    public ResponseEntity<Map<String, RoomType>> createRoomType(@Valid @RequestBody ReviewNewRoomType request){
        RoomType roomType = roomTypeService.createRoomType(request);
        return ClientResponse.ok("RoomType", roomType);
    }

    /**
     * Retrieves all room types.
     *
     * @return a response containing the list of room types
     */
    @GetMapping("/roomtypes")
    public ResponseEntity<Map<String, List<RoomType>>> getAllRooms(){
        List<RoomType> rooms = roomTypeService.getAllRoomTypes();
        return ClientResponse.ok("rooms", rooms);
    }

    /**
     * Adds a bed to a specific room type.
     *
     * @param request the validated details of the bed in the room, such as quantity
     * @param typeId  the id of the room type
     * @param bedId   the id of the bed type to add
     * @return an empty successful response
     */
    @PostMapping("/roomtypes/{typeId}/beds/{bedId}")
    public ResponseEntity<Map<String, String>> addBedToRoom(@Valid @RequestBody ReviewAddBedToRoom request, @PathVariable UUID typeId, @PathVariable UUID bedId){
        roomBedsService.addBedsToRoom(request, typeId, bedId);
        return ClientResponse.okEmpty();
    }

    /**
     * Retrieves a specific room type.
     *
     * @param roomTypeId the id of the room type
     * @return a response containing the room type
     */
    @GetMapping("/roomtypes/{roomTypeId}")
    public ResponseEntity<Map<String, RoomTypeResponse>> getRoomType(@PathVariable UUID roomTypeId){
        RoomTypeResponse room = roomTypeService.getRoomType(roomTypeId);
        return ClientResponse.ok("room", room);
    }

    /**
     * Links an extra to a room type.
     *
     * @param roomTypeId the id of the room type
     * @param extraId    the id of the extra to link
     * @param amount     the amount of the extra included with the room
     * @param showAmount whether the amount should be shown to guests
     * @return an empty successful response
     */
    @PostMapping("/roomtypes/{roomTypeId}/extras/{extraId}")
    public ResponseEntity<Map<String, String>> linkExtraToRoom(
            @PathVariable UUID roomTypeId,
            @PathVariable UUID extraId,
            int amount,
            boolean showAmount
    ){
        roomTypeService.linkExtraToRoom(extraId, roomTypeId, amount, showAmount);
        return ClientResponse.okEmpty();
    }
}

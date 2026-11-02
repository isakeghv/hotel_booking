package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.request.ReviewAddBedToRoom;
import com.example.inventoryserviceproject.dto.request.ReviewNewRoom;
import com.example.inventoryserviceproject.dto.request.ReviewNewRoomType;
import com.example.inventoryserviceproject.dto.request.ReviewOccupyRoom;
import com.example.inventoryserviceproject.model.Room;
import com.example.inventoryserviceproject.model.RoomType;
import com.example.inventoryserviceproject.dto.response.RoomTypeResponse;
import com.example.inventoryserviceproject.service.RoomBedsService;
import com.example.inventoryserviceproject.service.RoomService;
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
    private final RoomService roomService;

    /**
     * Creates the controller with the services it delegates to.
     *
     * @param roomTypeService service containing the room type business logic
     * @param roomBedsService service managing the beds in each room type
     * @param roomService     service containing the single room business logic
     */
    public RoomController(RoomTypeService roomTypeService, RoomBedsService roomBedsService, RoomService roomService){
        this.roomTypeService = roomTypeService;
        this.roomBedsService = roomBedsService;
        this.roomService = roomService;
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
     * @param roomTypeId  the id of the room type
     * @param bedId   the id of the bed type to add
     * @return an empty successful response
     */
    @PostMapping("/roomtypes/{roomTypeId}/beds/{bedId}")
    public ResponseEntity<Map<String, String>> addBedToRoom(@Valid @RequestBody ReviewAddBedToRoom request, @PathVariable UUID roomTypeId, @PathVariable UUID bedId){
        roomBedsService.addBedsToRoom(request, roomTypeId, bedId);
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


    /**
     * Creates a new room (e.g. a room number) belonging to a room type.
     *
     * @param request    the validated details of the new room
     * @param roomTypeId the id of the room type the room belongs to
     * @return a response with the created room
     */
    @PostMapping("/roomtypes/{roomTypeId}/room")
    public ResponseEntity<Map<String, Room>> createRoom(@Valid @RequestBody ReviewNewRoom request, @PathVariable UUID roomTypeId){
        Room room = roomService.createRoom(request, roomTypeId);
        return ClientResponse.ok("room", room);
    }

    /**
     * Sets a specific room (not room type) as unavailable for a period of
     * time. The caller's permission level decides whether the period is
     * stored as a booking or as the room being set as unavailable, see
     * {@link RoomService#occupyRoom}.
     *
     * @param request         the validated period the room is unavailable
     * @param roomId          the id of the room
     * @param permissionLevel the caller's permission level, added by the API gateway
     * @return an empty successful response
     */
    @PostMapping("/rooms/{roomId}/occupied")
    public ResponseEntity<Map<String, String>> setRoomToUnavailable(
            @Valid @RequestBody ReviewOccupyRoom request,
            @PathVariable UUID roomId,
            @RequestHeader("X-Permission-Level") int permissionLevel
    ){
        roomService.occupyRoom(request, roomId, permissionLevel);
        return ClientResponse.okEmpty();
    }

    /**
     * Makes a specific room available again, by removing a period in which it
     * was set as unavailable by an employee, manager or owner. Periods caused
     * by a booking are refused, see {@link RoomService#releaseRoom}.
     *
     * @param roomId     the id of the room
     * @param occupiedId the id of the period to remove
     * @return an empty successful response
     */
    @DeleteMapping("/rooms/{roomId}/occupied/{occupiedId}")
    public ResponseEntity<Map<String, String>> setRoomToAvailable(
            @PathVariable UUID roomId,
            @PathVariable UUID occupiedId
    ){
        roomService.releaseRoom(roomId, occupiedId);
        return ClientResponse.okEmpty();
    }
}

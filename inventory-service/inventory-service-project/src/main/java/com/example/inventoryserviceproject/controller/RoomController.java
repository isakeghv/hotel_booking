package com.example.inventoryserviceproject.controller;

import com.example.inventoryserviceproject.dto.request.ReviewAddBedToRoom;
import com.example.inventoryserviceproject.dto.request.ReviewNewRoom;
import com.example.inventoryserviceproject.dto.request.ReviewNewRoomType;
import com.example.inventoryserviceproject.dto.request.ReviewOccupyRoom;
import com.example.inventoryserviceproject.model.Room;
import com.example.inventoryserviceproject.model.RoomType;
import com.example.inventoryserviceproject.dto.response.OccupiedRoomResponse;
import com.example.inventoryserviceproject.dto.response.RoomTypeResponse;
import com.example.inventoryserviceproject.dto.response.Envelopes;
import com.example.inventoryserviceproject.service.RoomBedsService;
import com.example.inventoryserviceproject.service.RoomService;
import com.example.inventoryserviceproject.service.RoomTypeService;
import com.example.inventoryserviceproject.utils.ClientResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing room types, including their beds and
 * linked extras.
 */
@Tag(name = "Room types")
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
    @Operation(
            summary = "Create a room type",
            description = """
                    **Access: Manager.**

                    Creates a room type, together with a default variant "Standard" without extra price, as a room \
                    type can only be booked through a variant. Capacity is calculated from the beds added later.

                    **Response key:** `RoomType`.
                    """)
    @ApiResponse(responseCode = "200", description = "The created room type")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @PostMapping("/roomtypes")
    public ResponseEntity<Envelopes.CreatedRoomTypeResult> createRoomType(@Valid @RequestBody ReviewNewRoomType request){
        RoomType roomType = roomTypeService.createRoomType(request);
        return ClientResponse.ok(new Envelopes.CreatedRoomTypeResult(roomType));
    }

    /**
     * Retrieves all room types.
     *
     * @return a response containing the list of room types
     */
    @Operation(
            summary = "List all room types",
            description = """
                    **Access: Signed in.**

                    Lists all room types, each with its capacity calculated from its beds.

                    **Response key:** `rooms`.
                    """)
    @ApiResponse(responseCode = "200", description = "All room types")
    @GetMapping("/roomtypes")
    public ResponseEntity<Envelopes.RoomTypesResult> getAllRooms(){
        List<RoomType> rooms = roomTypeService.getAllRoomTypes();
        return ClientResponse.ok(new Envelopes.RoomTypesResult(rooms));
    }

    /**
     * Adds a bed to a specific room type.
     *
     * @param request the validated details of the bed in the room, such as quantity
     * @param roomTypeId  the id of the room type
     * @param bedId   the id of the bed type to add
     * @return an empty successful response
     */
    @Operation(
            summary = "Add beds to a room type",
            description = """
                    **Access: Manager.**

                    Sets how many beds of a bed type a room type has. Adding a bed type that is already in the room \
                    type changes its amount. The room type's capacity follows from its beds.
                    """)
    @ApiResponse(responseCode = "204", content = @Content, description = "The beds were added")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type or the bed type does not exist")
    @PostMapping("/roomtypes/{roomTypeId}/beds/{bedId}")
    public ResponseEntity<Void> addBedToRoom(@Valid @RequestBody ReviewAddBedToRoom request, @PathVariable UUID roomTypeId, @PathVariable UUID bedId){
        roomBedsService.addBedsToRoom(request, roomTypeId, bedId);
        return ClientResponse.okEmpty();
    }

    /**
     * Retrieves a specific room type.
     *
     * @param roomTypeId the id of the room type
     * @return a response containing the room type
     */
    @Operation(
            summary = "Get a room type",
            description = """
                    **Access: Signed in.**

                    Returns a room type with its price, capacity, beds and images.

                    **Response key:** `room`.
                    """)
    @ApiResponse(responseCode = "200", description = "The room type")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type does not exist")
    @GetMapping("/roomtypes/{roomTypeId}")
    public ResponseEntity<Envelopes.RoomTypeResult> getRoomType(@PathVariable UUID roomTypeId){
        RoomTypeResponse room = roomTypeService.getRoomType(roomTypeId);
        return ClientResponse.ok(new Envelopes.RoomTypeResult(room));
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
    @Operation(
            summary = "Include an extra in a room type",
            description = """
                    **Access: Manager.**

                    Includes an extra in every room of the room type, regardless of variant, e.g. towels or Wi-Fi. \
                    `amount` and `showAmount` are query parameters.
                    """)
    @ApiResponse(responseCode = "204", content = @Content, description = "The extra was included")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type or the extra does not exist")
    @ApiResponse(responseCode = "500", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The extra could not be included")
    @PostMapping("/roomtypes/{roomTypeId}/extras/{extraId}")
    public ResponseEntity<Void> linkExtraToRoom(
            @PathVariable UUID roomTypeId,
            @PathVariable UUID extraId,
            @Parameter(description = "How many of the extra are included, e.g. 2") int amount,
            @Parameter(description = "Whether customers see the amount (\"2 × Parking\") or only the name") boolean showAmount
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
    @Operation(
            tags = "Rooms",
            summary = "Create a room",
            description = """
                    **Access: Manager.**

                    Creates a single room (room number) of a room type. Room numbers are unique in the whole hotel. \
                    The digits before the last two are the floor, so a room number is at least 101 and cannot end in \
                    00 (e.g. 201 is room 1 on floor 2).

                    **Response key:** `room`.
                    """)
    @ApiResponse(responseCode = "200", description = "The created room")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room type does not exist")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room number is already in use")
    @PostMapping("/roomtypes/{roomTypeId}/room")
    public ResponseEntity<Envelopes.CreatedRoomResult> createRoom(@Valid @RequestBody ReviewNewRoom request, @PathVariable UUID roomTypeId){
        Room room = roomService.createRoom(request, roomTypeId);
        return ClientResponse.ok(new Envelopes.CreatedRoomResult(room));
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
    @Operation(
            tags = "Rooms",
            summary = "Set a room as unavailable",
            description = """
                    **Access: Staff.**

                    Sets a single room as unavailable for a period, e.g. for maintenance. Refused if the room is \
                    already unavailable during the period, or if a customer has it in their cart (try again later).
                    """)
    @ApiResponse(responseCode = "204", content = @Content, description = "The room is unavailable for the period")
    @ApiResponse(responseCode = "400", content = @Content(schema = @Schema(implementation = Envelopes.ValidationErrorResult.class)), description = "Invalid input. `err` lists each invalid field with a message")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room does not exist or is not active")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room is already unavailable, or being booked, during the period")
    @PostMapping("/rooms/{roomId}/occupied")
    public ResponseEntity<Void> setRoomToUnavailable(
            @Valid @RequestBody ReviewOccupyRoom request,
            @PathVariable UUID roomId,
            @Parameter(description = "The caller's permission level, added by the API gateway") @RequestHeader("X-Permission-Level") int permissionLevel
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
    @Operation(
            tags = "Rooms",
            summary = "Make a room available again",
            description = """
                    **Access: Staff.**

                    Removes a period in which staff set the room as unavailable. Periods caused by a customer's \
                    booking cannot be removed here; the booking must be cancelled instead. Get `occupiedId` from \
                    `GET /inventory/occupied/employee`.
                    """)
    @ApiResponse(responseCode = "204", content = @Content, description = "The room is available again")
    @ApiResponse(responseCode = "404", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The room is not unavailable in this period")
    @ApiResponse(responseCode = "409", content = @Content(schema = @Schema(implementation = Envelopes.ErrorResult.class)), description = "The period is a customer's booking")
    @DeleteMapping("/rooms/{roomId}/occupied/{occupiedId}")
    public ResponseEntity<Void> setRoomToAvailable(
            @PathVariable UUID roomId,
            @PathVariable UUID occupiedId
    ){
        roomService.releaseRoom(roomId, occupiedId);
        return ClientResponse.okEmpty();
    }

    /**
     * Retrieves the periods in which rooms are set as unavailable by an
     * employee, manager or owner, that are either ongoing or in the future.
     * Periods caused by bookings are not included.
     *
     * @return a response containing the periods, with their room and room type
     */
    @Operation(
            tags = "Rooms",
            summary = "List periods set by staff",
            description = """
                    **Access: Staff.**

                    Lists the periods in which staff have set rooms as unavailable that are ongoing or in the future, \
                    with the room and room type of each. Bookings are not included. Sorted by when they start.

                    **Response key:** `occupations`.
                    """)
    @ApiResponse(responseCode = "200", description = "The periods, earliest first")
    @GetMapping("/occupied/employee")
    public ResponseEntity<Envelopes.OccupationsResult> getEmployeeOccupations(){
        List<OccupiedRoomResponse> occupations = roomService.getEmployeeOccupations();
        return ClientResponse.ok(new Envelopes.OccupationsResult(occupations));
    }
}

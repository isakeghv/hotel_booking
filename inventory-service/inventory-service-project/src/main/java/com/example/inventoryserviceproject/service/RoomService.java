package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.request.ReviewNewRoom;
import com.example.inventoryserviceproject.dto.request.ReviewOccupyRoom;
import com.example.inventoryserviceproject.exception.ConflictException;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.exception.UnauthorizedException;
import com.example.inventoryserviceproject.model.OccupiedRoom;
import com.example.inventoryserviceproject.model.Room;
import com.example.inventoryserviceproject.repository.OccupiedRoomRepository;
import com.example.inventoryserviceproject.repository.RoomRepository;
import com.example.inventoryserviceproject.repository.RoomTypesRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service containing the business logic for single rooms (not room types),
 * such as their availability.
 */
@Service
public class RoomService {
    private final RoomRepository roomRepository;
    private final OccupiedRoomRepository occupiedRoomRepository;
    private final RoomTypesRepository roomTypesRepository;

    /**
     * Creates the service with the repositories it uses.
     *
     * @param roomRepository         repository for single rooms
     * @param occupiedRoomRepository repository for the periods rooms are unavailable
     * @param roomTypesRepository    repository for room types
     */
    public RoomService(
            RoomRepository roomRepository,
            OccupiedRoomRepository occupiedRoomRepository,
            RoomTypesRepository roomTypesRepository
    ){
        this.roomRepository = roomRepository;
        this.occupiedRoomRepository = occupiedRoomRepository;
        this.roomTypesRepository = roomTypesRepository;
    }

    /**
     * Creates a new room belonging to a room type. Room numbers are unique
     * across the whole hotel, not only within the room type.
     *
     * @param request    the validated details of the new room
     * @param roomTypeId the id of the room type the room belongs to
     * @return the created room
     * @throws NotFoundException       if the room type does not exist
     * @throws ConflictException       if a room with the room number already exists
     * @throws InternalServerException if the room could not be saved
     */
    public Room createRoom(ReviewNewRoom request, UUID roomTypeId){
        boolean roomTypeExists = roomTypesRepository.existsById(roomTypeId);
        if (!roomTypeExists) throw new NotFoundException("The selected room type does not exist");

        boolean roomNumberExists = roomRepository.existsByRoomNumber(request.roomNumber());
        if ( roomNumberExists) throw new ConflictException("Room number " + request.roomNumber() + " is already in use");

        // The unique constraint on room_number catches two requests creating the same room number at the same time
        try {
            Room room = new Room(request, roomTypeId);
            return roomRepository.saveAndFlush(room);
        } catch (DataIntegrityViolationException e){
            throw new ConflictException("Room number " + request.roomNumber() + " is already in use");
        } catch (DataAccessException e){
            throw new InternalServerException("Something went wrong while creating the room. Please try again");
        }
    }

    /**
     * Marks a room as unavailable for a period of time.
     *
     * <p>Used both when a customer books a room, and when an employee, manager or
     * owner sets a room as unavailable. The permission level of the caller
     * decides which of the two it is:
     * <ul>
     *     <li>0 (customer): the period is caused by a booking, so {@code booked} is true</li>
     *     <li>1, 2 or 3 (employee, manager, owner): the room is set as unavailable,
     *     so {@code booked} is false</li>
     * </ul>
     *
     * @param request         the validated period the room is unavailable
     * @param roomId          the id of the room
     * @param permissionLevel the permission level of the caller
     * @throws UnauthorizedException   if the permission level is not 0, 1, 2 or 3
     * @throws NotFoundException       if the room does not exist
     * @throws ConflictException       if the room is already unavailable during the period
     * @throws InternalServerException if the period could not be saved
     */
    public void occupyRoom(ReviewOccupyRoom request, UUID roomId, int permissionLevel){
        boolean booked = switch (permissionLevel){
            case 0 -> true;
            case 1, 2, 3 -> false;
            default -> throw new UnauthorizedException("Not permitted");
        };

        boolean roomExists = roomRepository.existsById(roomId);
        if (!roomExists) throw new NotFoundException("Room not found");

        boolean isOccupied = occupiedRoomRepository.existsByRoomIdAndFromTimeLessThanAndToTimeGreaterThan(roomId, request.to(), request.from());
        if ( isOccupied) throw new ConflictException("Room is unavailable during this period");

        // The check above gives a clear error in most cases, but two requests arriving at the same time can
        // both pass it. The exclusion constraint on the table then rejects the second one, which ends up here.
        // saveAndFlush is used so the insert happens now, and the violation is thrown and caught in this method.
        try {
            OccupiedRoom occupiedRoom = new OccupiedRoom(roomId, request.from(), request.to(), booked);
            occupiedRoomRepository.saveAndFlush(occupiedRoom);
        } catch (DataIntegrityViolationException e){
            throw new ConflictException("Room is unavailable during this period");
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error. Failed to set room as unavailable");
        }
    }
}

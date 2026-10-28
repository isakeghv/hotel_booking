package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.ReviewAddBedToRoom;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.model.*;
import com.example.inventoryserviceproject.dto.response.RoomTypeResponse;
import com.example.inventoryserviceproject.repository.RoomBedsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing which beds are in which room types.
 */
@Service
public class RoomBedsService {
    private final RoomBedsRepository roomBedsRepository;
    private final RoomTypeService roomTypeService;
    private final BedService bedService;

    /**
     * Creates the service with the repository and services it uses.
     *
     * @param roomBedsRepository repository for room-bed links
     * @param roomTypeService    service for room types
     * @param bedService         service for bed types
     */
    public RoomBedsService(RoomBedsRepository roomBedsRepository, RoomTypeService roomTypeService, BedService bedService){
        this.roomBedsRepository = roomBedsRepository;
        this.roomTypeService = roomTypeService;
        this.bedService = bedService;
    }

    /**
     * Adds beds to a room type. If the room type already has this bed type, the
     * amount is added to the existing amount, otherwise a new link is created.
     *
     * @param request the validated request containing the amount to add
     * @param typeId  the id of the room type
     * @param bedId   the id of the bed type
     * @return the updated room type
     * @throws NotFoundException if the room type or the bed type does not exist
     */
    public RoomTypeResponse addBedsToRoom(ReviewAddBedToRoom request, UUID typeId, UUID bedId){
        // Getting bed and room from service to verify existence, methods throw 404 exception if not found.
        bedService.getSingleBed(bedId);
        roomTypeService.getRoomType(typeId);

        Optional<RoomBed> optRoomBed = roomBedsRepository.findById(new RoomBedsId(typeId, bedId));

        RoomBed roomBed;

        if (optRoomBed.isPresent()){
            roomBed = optRoomBed.get();
            int newAmount = roomBed.getAmount() + request.amount();
            roomBed.setAmount(newAmount);
        } else {
            roomBed = new RoomBed(request, typeId, bedId);
        }

        roomBedsRepository.save(roomBed);

        return roomTypeService.getRoomType(roomBed.getRoomTypeId());
    }
}

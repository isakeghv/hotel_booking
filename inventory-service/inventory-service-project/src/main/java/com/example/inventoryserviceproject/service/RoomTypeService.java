package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.request.ReviewNewRoomType;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.mapper.RoomTypeResMapper;
import com.example.inventoryserviceproject.model.*;
import com.example.inventoryserviceproject.repository.BedRepository;
import com.example.inventoryserviceproject.repository.RoomBedsRepository;
import com.example.inventoryserviceproject.repository.RoomIncludesRepository;
import com.example.inventoryserviceproject.dto.response.RoomTypeResponse;
import com.example.inventoryserviceproject.repository.RoomTypesRepository;
import com.example.inventoryserviceproject.repository.VariantRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service containing the business logic for room types, including their
 * capacity, beds, images and linked extras.
 */
@Service
public class RoomTypeService {
    private static final String DEFAULT_VARIANT_NAME = "Standard";

    private final RoomBedsRepository roomBedsRepository;
    private final BedRepository bedRepository;
    private final RoomTypesRepository roomTypesRepository;
    private final ImageService imageService;
    private final RoomIncludesRepository roomIncludesRepository;
    private final ExtrasService extrasService;
    private final RoomTypeResMapper roomTypeResMapper;
    private final VariantRepository variantRepository;

    /**
     * Creates the service with the repositories, services and mapper it uses.
     *
     * @param roomBedsRepository     repository for room-bed links
     * @param bedRepository          repository for bed types
     * @param roomTypesRepository    repository for room types
     * @param imageService           service for room images
     * @param roomIncludesRepository repository for room-extra links
     * @param extrasService          service for extras
     * @param roomTypeResMapper      mapper that builds the room type response
     * @param variantRepository      repository for variants
     */
    public RoomTypeService(
            RoomBedsRepository roomBedsRepository,
            BedRepository bedRepository,
            RoomTypesRepository roomTypesRepository,
            ImageService imageService,
            RoomIncludesRepository roomIncludesRepository,
            ExtrasService extrasService,
            RoomTypeResMapper roomTypeResMapper,
            VariantRepository variantRepository
            ){
        this.roomBedsRepository = roomBedsRepository;
        this.bedRepository = bedRepository;
        this.roomTypesRepository = roomTypesRepository;
        this.imageService = imageService;
        this.roomIncludesRepository = roomIncludesRepository;
        this.extrasService = extrasService;
        this.roomTypeResMapper = roomTypeResMapper;
        this.variantRepository = variantRepository;
    }

    /**
     * Calculates the capacity of a room type as the sum of each linked bed's
     * capacity multiplied by the number of those beds.
     *
     * @param roomId the id of the room type
     * @return the total number of guests the room sleeps
     */
    private int getRoomCapacity(UUID roomId){
        List<RoomBed> roomBeds = roomBedsRepository.findByIdRoomTypeId(roomId);

        return roomBeds.stream()
                .mapToInt(rb -> bedRepository.findByBedId(rb.getBedId())
                                .map(Bed::getCapacity)
                    .orElse(0) * rb.getAmount())
                .sum();
    }

    /**
     * Retrieves the bed types linked to a room type.
     *
     * @param roomId the id of the room type
     * @return the list of bed types, possibly empty
     */
    private List<Bed> getRoomBeds(UUID roomId){
        List<RoomBed> roomBeds = roomBedsRepository.findByIdRoomTypeId(roomId);
        List<Bed> beds = new ArrayList<>();

        for ( RoomBed rb : roomBeds){
            Optional<Bed> bed = bedRepository.findByBedId(rb.getBedId());
            bed.ifPresent(beds::add);
        }

        return beds;
    }

    /**
     * Retrieves a room type with its calculated capacity, beds and images.
     *
     * @param roomId the id of the room type
     * @return the room type response
     * @throws NotFoundException if the room type does not exist
     */
    public RoomTypeResponse getRoomType(UUID roomId){

        Optional<RoomType> optRoom = roomTypesRepository.findByRoomTypeId(roomId);
        if (optRoom.isEmpty()) throw new NotFoundException("Room-type not found");

        int roomCapacity = getRoomCapacity(roomId);
        RoomType room = optRoom.get();
        room.setCapacity(roomCapacity);

        List<RoomBed> roomBeds = roomBedsRepository.findByIdRoomTypeId(room.getRoomTypeId());
        List<Bed> beds = getRoomBeds(roomId);
        List<ImageData> imageData = imageService.getImageData(roomId);

        return roomTypeResMapper.toResponse(room, beds, roomBeds, imageData);
    }

    /**
     * Retrieves all room types, each with its calculated capacity.
     *
     * @return the list of room types, possibly empty
     */
    public List<RoomType> getAllRoomTypes(){
        List<RoomType> rooms = roomTypesRepository.findAll();

        for ( RoomType room: rooms){
            int roomCapacity = getRoomCapacity(room.getRoomTypeId());
            room.setCapacity(roomCapacity);
        }

        return rooms;
    }

    /**
     * Creates and stores a new room type, together with a default variant
     * ("Standard", no extra price). A room type can only be booked through a
     * variant, so without one it would never be available to customers.
     * Both are stored in one transaction, so a room type never exists without a variant.
     *
     * @param request the validated details of the new room type
     * @return the stored room type
     * @throws InternalServerException if the room type could not be saved
     */
    @Transactional
    public RoomType createRoomType(ReviewNewRoomType request){

        try {
            RoomType roomType = new RoomType(request);
            roomTypesRepository.save(roomType);
            variantRepository.save(new RoomTypeVariant(roomType.getRoomTypeId(), DEFAULT_VARIANT_NAME, 0, false));
            return roomType;
        }catch (DataAccessException e){
            throw new InternalServerException("Internal server error. Failed to save new room-type.");
        }
    }

    /**
     * Links an extra to a room type, so it is included with the room.
     *
     * @param extraId    the id of the extra
     * @param roomTypeId the id of the room type
     * @param amount     the amount of the extra included with the room
     * @param showAmount whether the amount should be shown to guests
     * @throws NotFoundException       if the extra or the room type does not exist
     * @throws InternalServerException if the link could not be saved
     */
    public void linkExtraToRoom(UUID extraId, UUID roomTypeId, int amount, boolean showAmount){
        extrasService.getExtraById(extraId);

        Optional<RoomType> roomType = roomTypesRepository.findById(roomTypeId);
        if ( roomType.isEmpty()) throw new NotFoundException("Room-type not found");

        try {
            RoomIncludes roomIncludes = new RoomIncludes(roomTypeId, extraId, amount, showAmount);
            roomIncludesRepository.save(roomIncludes);
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error. Failed to link item to room");
        }
    }
}

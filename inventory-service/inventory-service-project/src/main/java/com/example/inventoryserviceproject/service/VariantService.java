package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.ReviewNewRoomVariant;
import com.example.inventoryserviceproject.exception.ConflictException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.model.RoomTypeVariant;
import com.example.inventoryserviceproject.repository.RoomTypesRepository;
import com.example.inventoryserviceproject.repository.VariantRepository;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service containing the business logic for room type variants.
 */
@Service
public class VariantService {
    private final VariantRepository variantRepository;
    private final RoomTypesRepository roomTypesRepository;

    /**
     * Creates the service with the repositories it uses.
     *
     * @param variantRepository   repository for variants
     * @param roomTypesRepository repository for room types
     */
    public VariantService(VariantRepository variantRepository, RoomTypesRepository roomTypesRepository){
        this.variantRepository = variantRepository;
        this.roomTypesRepository = roomTypesRepository;
    }

    /**
     * Creates a new variant for a room type. The variant name must be unique
     * within the room type.
     *
     * @param request    the validated details of the new variant
     * @param roomTypeId the id of the room type the variant belongs to
     * @throws NotFoundException  if the room type does not exist
     * @throws ConflictException  if the room type already has a variant with this name
     */
    public void createRoomTypeVariant(ReviewNewRoomVariant request, UUID roomTypeId){
        boolean roomExists = roomTypesRepository.existsById(roomTypeId);
        if (!roomExists) throw new NotFoundException("Room-type not found");

        boolean variantExists = variantRepository.existsByVariantNameAndTypeId(request.variantName(), roomTypeId);
        if ( variantExists) throw new ConflictException("Variant-name already in use");

        RoomTypeVariant variant = new RoomTypeVariant(request, roomTypeId);
        variantRepository.save(variant);

    }
}

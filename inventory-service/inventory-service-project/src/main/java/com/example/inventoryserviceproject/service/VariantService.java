package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.ReviewLinkExtraToVariant;
import com.example.inventoryserviceproject.dto.ReviewNewRoomVariant;
import com.example.inventoryserviceproject.exception.ConflictException;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.model.RoomTypeVariant;
import com.example.inventoryserviceproject.model.VariantIncludes;
import com.example.inventoryserviceproject.model.VariantIncludesId;
import com.example.inventoryserviceproject.repository.RoomTypesRepository;
import com.example.inventoryserviceproject.repository.VariantIncludesRepository;
import com.example.inventoryserviceproject.repository.VariantRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Service containing the business logic for room type variants.
 */
@Service
public class VariantService {
    private final VariantRepository variantRepository;
    private final RoomTypesRepository roomTypesRepository;
    private final VariantIncludesRepository variantIncludesRepository;
    private final ExtrasService extrasService;

    /**
     * Creates the service with the repositories and services it uses.
     *
     * @param variantRepository         repository for variants
     * @param roomTypesRepository       repository for room types
     * @param variantIncludesRepository repository for variant-extra links
     * @param extrasService             service for extras
     */
    public VariantService(
            VariantRepository variantRepository,
            RoomTypesRepository roomTypesRepository,
            VariantIncludesRepository variantIncludesRepository,
            ExtrasService extrasService
    ){
        this.variantRepository = variantRepository;
        this.roomTypesRepository = roomTypesRepository;
        this.variantIncludesRepository = variantIncludesRepository;
        this.extrasService = extrasService;
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

    /**
     * Links an extra to a variant, so it is included with the variant.
     *
     * @param request   the validated amount and visibility of the extra
     * @param variantId the id of the variant
     * @param extraId   the id of the extra
     * @throws NotFoundException       if the variant or the extra does not exist
     * @throws ConflictException       if the extra is already linked to the variant
     * @throws InternalServerException if the link could not be saved
     */
    public void linkExtraToVariant(ReviewLinkExtraToVariant request, UUID variantId, UUID extraId){
        boolean variantExists = variantRepository.existsById(variantId);
        if (!variantExists) throw new NotFoundException("Variant not found");

        extrasService.getExtraById(extraId);

        boolean linkExists = variantIncludesRepository.existsById(new VariantIncludesId(variantId, extraId));
        if ( linkExists) throw new ConflictException("Item already linked to variant");

        try {
            VariantIncludes variantIncludes = new VariantIncludes(variantId, extraId, request.amount(), request.showAmount());
            variantIncludesRepository.save(variantIncludes);
        } catch (DataAccessException e){
            throw new InternalServerException("Internal server error. Failed to link item to variant");
        }
    }
}

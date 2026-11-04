package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.dto.request.ReviewLinkExtraToVariant;
import com.example.inventoryserviceproject.dto.request.ReviewNewRoomVariant;
import com.example.inventoryserviceproject.exception.ConflictException;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.dto.request.ReviewVariantQuery;
import com.example.inventoryserviceproject.dto.response.RoomTypeVariantsResponse;
import com.example.inventoryserviceproject.mapper.VariantResMapper;
import com.example.inventoryserviceproject.model.*;
import com.example.inventoryserviceproject.repository.*;
import com.example.inventoryserviceproject.utils.PriceUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service containing the business logic for room type variants.
 */
@Service
public class VariantService {
    private final VariantRepository variantRepository;
    private final RoomTypesRepository roomTypesRepository;
    private final VariantIncludesRepository variantIncludesRepository;
    private final ExtrasService extrasService;
    private final ExtraRepository extraRepository;
    private final VariantInformationRepository variantInformationRepository;
    private final VariantResMapper variantResMapper;

    /**
     * Creates the service with the repositories, services and mapper it uses.
     *
     * @param variantRepository            repository for variants
     * @param roomTypesRepository          repository for room types
     * @param variantIncludesRepository    repository for variant-extra links
     * @param extrasService                service for extras
     * @param extraRepository              repository for extras
     * @param variantInformationRepository repository for variant information
     * @param variantResMapper             mapper that builds the variant response
     */
    public VariantService(
            VariantRepository variantRepository,
            RoomTypesRepository roomTypesRepository,
            VariantIncludesRepository variantIncludesRepository,
            ExtrasService extrasService,
            ExtraRepository extraRepository,
            VariantInformationRepository variantInformationRepository,
            VariantResMapper variantResMapper
    ){
        this.variantRepository = variantRepository;
        this.roomTypesRepository = roomTypesRepository;
        this.variantIncludesRepository = variantIncludesRepository;
        this.extrasService = extrasService;
        this.extraRepository = extraRepository;
        this.variantInformationRepository = variantInformationRepository;
        this.variantResMapper = variantResMapper;
    }

    /**
     * Retrieves the active variants of a room type that customers can choose
     * between, priced for the given stay and number of rooms. Prices include the
     * room type's price, so clients do not need to calculate anything.
     *
     * <p>Uses a fixed number of queries regardless of the number of variants:
     * everything is loaded in batches, then combined here.
     *
     * @param roomTypeId the id of the room type
     * @param query      the validated number of rooms and dates
     * @return the variants, lowest total first
     * @throws NotFoundException if the room type does not exist, is not active, or has no active variants
     */
    public RoomTypeVariantsResponse getRoomTypeVariants(UUID roomTypeId, ReviewVariantQuery query){
        RoomType roomType = roomTypesRepository.findById(roomTypeId)
                .filter(RoomType::getIsActive)
                .orElseThrow(() -> new NotFoundException("This room is not available"));

        List<RoomTypeVariant> variants = variantRepository.findAllByRoomTypeIdAndIsActiveTrue(roomTypeId);
        if (variants.isEmpty()) throw new NotFoundException("This room is not available");
        List<UUID> variantIds = variants.stream().map(RoomTypeVariant::getVariantId).toList();

        // Included extras of all variants, and the active extras they point to
        Map<UUID, List<VariantIncludes>> includesByVariant = variantIncludesRepository.findAllByIdVariantIdIn(variantIds)
                .stream()
                .collect(Collectors.groupingBy(VariantIncludes::getVariantId));
        Map<UUID, Extra> extrasById = extraRepository.findAllById(
                        includesByVariant.values().stream().flatMap(List::stream).map(VariantIncludes::getExtraId).distinct().toList())
                .stream()
                .filter(Extra::getIsActive)
                .collect(Collectors.toMap(Extra::getExtraId, Function.identity()));

        Map<UUID, VariantInformation> descriptionByVariant = variantInformationRepository.findAllByVariantIdInAndIsMainDescriptionTrue(variantIds)
                .stream()
                .collect(Collectors.toMap(VariantInformation::getVariantId, Function.identity()));

        int nights = PriceUtils.nights(query.from(), query.to());
        List<RoomTypeVariantsResponse.VariantInfo> variantInfos = variants.stream()
                .map(variant -> variantResMapper.toResponse(
                        variant,
                        roomType.getRoomPrice(),
                        descriptionByVariant.get(variant.getVariantId()),
                        includesByVariant.getOrDefault(variant.getVariantId(), List.of()),
                        extrasById,
                        nights,
                        query.rooms()))
                .sorted(Comparator.comparingInt(RoomTypeVariantsResponse.VariantInfo::total))
                .toList();

        return new RoomTypeVariantsResponse(variantInfos, nights, query.rooms(), PriceUtils.CURRENCY);
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

        boolean variantExists = variantRepository.existsByVariantNameAndRoomTypeId(request.variantName(), roomTypeId);
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

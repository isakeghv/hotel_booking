package com.example.inventoryserviceproject.service;

import com.example.inventoryserviceproject.cache.CartItem;
import com.example.inventoryserviceproject.cache.ReservedCache;
import com.example.inventoryserviceproject.dto.request.ReviewAvailabilityQuery;
import com.example.inventoryserviceproject.dto.request.ReviewExtraQuery;
import com.example.inventoryserviceproject.dto.response.AvailabilityResponse;
import com.example.inventoryserviceproject.dto.response.ExtraAvailabilityResponse;
import com.example.inventoryserviceproject.exception.ConflictException;
import com.example.inventoryserviceproject.exception.InternalServerException;
import com.example.inventoryserviceproject.exception.NotFoundException;
import com.example.inventoryserviceproject.mapper.AvailabilityResMapper;
import com.example.inventoryserviceproject.mapper.ExtraAvailabilityResMapper;
import com.example.inventoryserviceproject.model.Extra;
import com.example.inventoryserviceproject.model.OccupiedRoom;
import com.example.inventoryserviceproject.model.Room;
import com.example.inventoryserviceproject.model.RoomType;
import com.example.inventoryserviceproject.model.RoomTypeVariant;
import com.example.inventoryserviceproject.repository.ExtraRepository;
import com.example.inventoryserviceproject.repository.OccupiedRoomRepository;
import com.example.inventoryserviceproject.repository.RoomRepository;
import com.example.inventoryserviceproject.repository.VariantRepository;
import com.example.inventoryserviceproject.utils.PriceUtils;
import com.example.inventoryserviceproject.utils.RoomSelectionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service containing the business logic for checking availability of rooms,
 * and temporarily reserving them in the cache while users have them in their cart.
 */
@Service
public class AvailabilityService {
    // A hold this close to expiring could disappear before the user can complete the booking
    private static final Duration MIN_TIME_LEFT = Duration.ofMinutes(1);

    private final VariantRepository variantRepository;
    private final RoomRepository roomRepository;
    private final OccupiedRoomRepository occupiedRoomRepository;
    private final ReservedCache reservedCache;
    private final AvailabilityResMapper availabilityResMapper;
    private final ExtraRepository extraRepository;
    private final ExtraAvailabilityResMapper extraAvailabilityResMapper;
    private final LocalTime checkInTime;
    private final LocalTime checkOutTime;

    /**
     * Creates the service with the repositories, cache and mapper it uses.
     *
     * @param variantRepository     repository for variants
     * @param roomRepository        repository for single rooms
     * @param occupiedRoomRepository repository for the periods rooms are unavailable
     * @param reservedCache         cache holding rooms in users' carts
     * @param availabilityResMapper mapper that builds the availability response
     * @param extraRepository       repository for extras
     * @param extraAvailabilityResMapper mapper that builds the extra availability response
     * @param checkInTime           the hotel's check-in time, e.g. 15:00
     * @param checkOutTime          the hotel's check-out time, e.g. 12:00
     */
    public AvailabilityService(
            VariantRepository variantRepository,
            RoomRepository roomRepository,
            OccupiedRoomRepository occupiedRoomRepository,
            ReservedCache reservedCache,
            AvailabilityResMapper availabilityResMapper,
            ExtraRepository extraRepository,
            ExtraAvailabilityResMapper extraAvailabilityResMapper,
            @Value("${hotel.checkin.time}") String checkInTime,
            @Value("${hotel.checkout.time}") String checkOutTime
    ){
        this.variantRepository = variantRepository;
        this.roomRepository = roomRepository;
        this.occupiedRoomRepository = occupiedRoomRepository;
        this.reservedCache = reservedCache;
        this.availabilityResMapper = availabilityResMapper;
        this.extraRepository = extraRepository;
        this.extraAvailabilityResMapper = extraAvailabilityResMapper;
        this.checkInTime = LocalTime.parse(checkInTime);
        this.checkOutTime = LocalTime.parse(checkOutTime);
    }

    /**
     * Checks whether the requested amount of rooms of a variant are available
     * for the dates, and if so, holds them in the user's cart in the cache.
     *
     * <p>A room is available when it is free in the database (not booked or set
     * as unavailable) and not held in the cache by another cart. The rooms are
     * picked as close to each other, and to the rooms already in the cart, as
     * possible. Either all requested rooms are held, or none.
     *
     * @param userId    the id of the user whose cart the rooms are added to
     * @param variantId the id of the chosen variant
     * @param query     the validated amount of rooms and dates
     * @return the reserved rooms with their prices
     * @throws NotFoundException       if the variant or its room type does not exist or is not active
     * @throws ConflictException       if not enough rooms are available, or the cart is about to expire
     * @throws InternalServerException if the cache cannot be reached
     */
    public AvailabilityResponse reserveRooms(UUID userId, UUID variantId, ReviewAvailabilityQuery query){
        RoomTypeVariant variant = variantRepository.findWithRoomTypeByVariantId(variantId)
                .filter(v -> v.isActive() && v.getRoomType().getIsActive())
                .orElseThrow(() -> new NotFoundException("This room option is no longer available"));
        RoomType roomType = variant.getRoomType();

        // Check-in and check-out times are applied right away, so the cart holds exact times
        LocalDateTime from = query.from().atTime(checkInTime);
        LocalDateTime to = query.to().atTime(checkOutTime);
        List<LocalDate> nights = query.from().datesUntil(query.to()).toList();

        try {
            Instant expiresAt = reservedCache.getOrStartCartExpiry(userId);
            if (Duration.between(Instant.now(), expiresAt).compareTo(MIN_TIME_LEFT) < 0){
                throw new ConflictException("Your reservation is about to expire. Please complete your booking, or try again when it has expired");
            }

            // Free in the database, then remove those held by other carts
            List<Room> candidates = findFreeRoomsInDatabase(roomType.getRoomTypeId(), from, to);
            removeHeldRooms(candidates, nights);

            List<Integer> cartRoomNumbers = reservedCache.getCartItems(userId).stream().map(CartItem::roomNumber).toList();

            // A picked room can be taken by another cart between the check above and holding it.
            // If so, remove the taken rooms and pick once more before giving up
            for (int attempt = 0; attempt < 2; attempt++){
                List<Room> picked = RoomSelectionUtils.pickClosestRooms(candidates, query.amount(), cartRoomNumbers);
                if (picked.isEmpty()) throw notEnoughRooms(candidates.size());

                List<CartItem> items = holdAll(userId, picked, nights, from, to, variant, expiresAt);
                if (items != null){
                    reservedCache.addCartItems(userId, items, expiresAt);
                    return availabilityResMapper.toResponse(variant, items, nights.size(), expiresAt);
                }
                removeHeldRooms(candidates, nights);
            }
            throw new ConflictException("The rooms were just reserved by someone else. Please try again");
        } catch (RedisConnectionFailureException e){
            throw new InternalServerException("Reservations are temporarily unavailable. Please try again");
        }
    }

    /**
     * Checks that an extra can be added to a booking, and prices it for the
     * user's stay. Nothing is stored in the cache, as extras are not limited.
     *
     * <p>The stay is read from the user's cart, from the earliest check-in to the
     * latest check-out, so the booking-service does not need to send the dates.
     * Extras with a fixed price are priced per unit, others per unit per night.
     *
     * @param userId  the id of the user whose cart the stay is read from
     * @param extraId the id of the extra
     * @param query   the validated amount of the extra
     * @return the extra with its price for the stay
     * @throws NotFoundException       if the extra does not exist or is not active
     * @throws ConflictException       if the extra cannot be added to a booking, or the cart has no rooms
     * @throws InternalServerException if the cache cannot be reached
     */
    public ExtraAvailabilityResponse verifyExtra(UUID userId, UUID extraId, ReviewExtraQuery query){
        Extra extra = extraRepository.findById(extraId)
                .filter(Extra::getIsActive)
                .orElseThrow(() -> new NotFoundException("This extra is no longer available"));
        if (!extra.getAddonPossible()) throw new ConflictException("This extra cannot be added to a booking");

        try {
            // An expired cart has no items, so both are checked together
            List<CartItem> cartItems = reservedCache.getCartItems(userId);
            Optional<Instant> expiresAt = reservedCache.getCartExpiry(userId);
            if (cartItems.isEmpty() || expiresAt.isEmpty()){
                throw new ConflictException("Add a room to your booking before adding extras");
            }

            // The whole stay: earliest check-in to latest check-out, in case the rooms have different dates
            LocalDateTime from = cartItems.stream().map(CartItem::from).min(Comparator.naturalOrder()).orElseThrow();
            LocalDateTime to = cartItems.stream().map(CartItem::to).max(Comparator.naturalOrder()).orElseThrow();
            int nights = PriceUtils.nights(from.toLocalDate(), to.toLocalDate());

            int total = PriceUtils.extraTotal(extra.getPrice(), extra.getIsPriceFixed(), query.amount(), nights);
            return extraAvailabilityResMapper.toResponse(extra, query.amount(), from, to, nights, total, expiresAt.get());
        } catch (RedisConnectionFailureException e){
            throw new InternalServerException("Extras are temporarily unavailable. Please try again");
        }
    }

    /**
     * Holds all the given rooms for the nights, priced for the variant. Either
     * all rooms are held, or none: if one of them is already held by another
     * cart, the rooms held by this call are released again.
     *
     * @param userId    the id of the user
     * @param rooms     the rooms to hold
     * @param nights    the nights to hold them for
     * @param from      check-in date and time
     * @param to        check-out date and time
     * @param variant   the chosen variant, with its room type loaded
     * @param expiresAt when the holds expire, the same as the cart's expiry
     * @return the cart items for the held rooms, or null if any room was already held
     */
    private List<CartItem> holdAll(UUID userId, List<Room> rooms, List<LocalDate> nights, LocalDateTime from, LocalDateTime to, RoomTypeVariant variant, Instant expiresAt){
        RoomType roomType = variant.getRoomType();

        int roomTotal = PriceUtils.roomTotal(roomType.getRoomPrice(), nights.size());
        int variantTotal = PriceUtils.variantTotal(variant.getExtraPrice(), variant.isPriceFixed(), nights.size());
        int total = PriceUtils.total(roomTotal, variantTotal);

        List<CartItem> items = new ArrayList<>();
        for (Room room : rooms){
            UUID itemId = UUID.randomUUID();
            // Stored in each hold, so it is possible to see which cart item and room it belongs to
            String holder = userId + ":" + itemId + ":" + room.getRoomNumber();

            if (!reservedCache.tryHoldRoom(room.getRoomId(), nights, holder, expiresAt)){
                items.forEach(item -> reservedCache.releaseRoom(item.roomId(), nights));
                return null;
            }

            items.add(new CartItem(
                    itemId, room.getRoomId(), room.getRoomNumber(), roomType.getRoomTypeId(), variant.getVariantId(),
                    from, to, roomType.getRoomPrice(), variant.getExtraPrice(), variant.isPriceFixed(), total));
        }
        return items;
    }

    /**
     * Finds the active rooms of a room type that have no unavailable period
     * overlapping the given one in the database. Does not check the cache.
     *
     * @param roomTypeId the id of the room type
     * @param from       when the period starts
     * @param to         when the period ends
     * @return the free rooms sorted by room number, in a list that can be changed
     */
    private List<Room> findFreeRoomsInDatabase(UUID roomTypeId, LocalDateTime from, LocalDateTime to){
        List<Room> rooms = new ArrayList<>(roomRepository.findAllByRoomTypeIdAndIsActiveTrueOrderByRoomNumberAsc(roomTypeId));
        if (rooms.isEmpty()) return rooms;

        // One query for the overlapping periods of all the rooms, instead of one per room
        Set<UUID> occupiedRoomIds = occupiedRoomRepository
                .findAllByRoomIdInAndFromTimeLessThanAndToTimeGreaterThan(rooms.stream().map(Room::getRoomId).toList(), to, from)
                .stream()
                .map(OccupiedRoom::getRoomId)
                .collect(Collectors.toSet());

        rooms.removeIf(room -> occupiedRoomIds.contains(room.getRoomId()));
        return rooms;
    }

    /**
     * Removes the rooms held by any cart for at least one of the nights.
     *
     * @param candidates the rooms to filter, changed in place
     * @param nights     the nights to check
     */
    private void removeHeldRooms(List<Room> candidates, List<LocalDate> nights){
        Set<UUID> held = reservedCache.findHeldRoomIds(candidates.stream().map(Room::getRoomId).toList(), nights);
        candidates.removeIf(room -> held.contains(room.getRoomId()));
    }

    /**
     * @param available the number of rooms that are available
     * @return an exception telling the user how many rooms are available
     */
    private ConflictException notEnoughRooms(int available){
        if (available == 0) return new ConflictException("No rooms of this type are available for the selected dates");
        if (available == 1) return new ConflictException("Only 1 room of this type is available for the selected dates");
        return new ConflictException("Only " + available + " rooms of this type are available for the selected dates");
    }
}

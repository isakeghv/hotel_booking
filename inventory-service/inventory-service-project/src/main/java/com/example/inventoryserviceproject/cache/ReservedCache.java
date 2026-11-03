package com.example.inventoryserviceproject.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

/**
 * Access to the reserved cache (Redis), which holds rooms temporarily while
 * users have them in their cart. Stores two kinds of keys:
 * <ul>
 *     <li>{@code hold:{roomId}:{night}}: marks one room as unavailable for one
 *     night. Used to check availability and prevent two users holding the same room.</li>
 *     <li>{@code cart:{userId}}: a hash with the cart's expiry time and its items.
 *     Used to know what to store in the database when the booking is completed.</li>
 * </ul>
 * Everything in a cart expires at the same fixed time, 30 minutes after its
 * first item was added. Adding more items does not extend it.
 */
@Component
public class ReservedCache {
    private static final Duration CART_LIFETIME = Duration.ofMinutes(30);
    private static final String CART_PREFIX = "cart:";
    private static final String HOLD_PREFIX = "hold:";
    private static final String EXPIRES_AT_FIELD = "expiresAt";
    private static final String ITEM_FIELD_PREFIX = "item:";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    /**
     * Creates the cache access with the Redis client and JSON mapper it uses.
     *
     * @param redis      client for the reserved cache
     * @param jsonMapper mapper used to store cart items as JSON
     */
    public ReservedCache(StringRedisTemplate redis, JsonMapper jsonMapper){
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns when the user's cart expires. If the user has no cart, a new
     * expiry is set 30 minutes from now. An existing expiry is never changed.
     *
     * @param userId the id of the user
     * @return when the cart and everything in it expires
     */
    public Instant getOrStartCartExpiry(UUID userId){
        String key = CART_PREFIX + userId;
        Instant newExpiry = Instant.now().plus(CART_LIFETIME);

        // Only the first call sets it, also when several requests arrive at the same time
        redis.opsForHash().putIfAbsent(key, EXPIRES_AT_FIELD, String.valueOf(newExpiry.toEpochMilli()));
        Object stored = redis.opsForHash().get(key, EXPIRES_AT_FIELD);
        Instant expiresAt = stored == null ? newExpiry : Instant.ofEpochMilli(Long.parseLong((String) stored));

        // Absolute time, so setting it again does not extend it
        redis.expireAt(key, expiresAt);
        return expiresAt;
    }

    /**
     * Returns when the user's cart expires, without starting a new cart.
     *
     * @param userId the id of the user
     * @return when the cart expires, or empty if the user has no cart
     */
    public Optional<Instant> getCartExpiry(UUID userId){
        Object stored = redis.opsForHash().get(CART_PREFIX + userId, EXPIRES_AT_FIELD);
        return Optional.ofNullable(stored).map(value -> Instant.ofEpochMilli(Long.parseLong((String) value)));
    }

    /**
     * Retrieves the items in the user's cart.
     *
     * @param userId the id of the user
     * @return the items, or an empty list if the cart is empty or does not exist
     */
    public List<CartItem> getCartItems(UUID userId){
        return redis.opsForHash().entries(CART_PREFIX + userId).entrySet().stream()
                .filter(entry -> ((String) entry.getKey()).startsWith(ITEM_FIELD_PREFIX))
                .map(entry -> jsonMapper.readValue((String) entry.getValue(), CartItem.class))
                .toList();
    }

    /**
     * Finds which of the given rooms are held by any cart for at least one of
     * the given nights. Checks all rooms and nights in a single request to Redis.
     *
     * @param roomIds the ids of the rooms to check
     * @param nights  the nights to check
     * @return the ids of the rooms that are held
     */
    public Set<UUID> findHeldRoomIds(List<UUID> roomIds, List<LocalDate> nights){
        if (roomIds.isEmpty()) return Set.of();

        // Keys in the order room 1 night 1..n, room 2 night 1..n etc., to match the values back to the rooms
        List<String> keys = new ArrayList<>();
        for (UUID roomId : roomIds){
            for (LocalDate night : nights) keys.add(holdKey(roomId, night));
        }
        List<String> values = redis.opsForValue().multiGet(keys);

        Set<UUID> heldRoomIds = new HashSet<>();
        for (int i = 0; i < keys.size(); i++){
            if (values != null && values.get(i) != null) heldRoomIds.add(roomIds.get(i / nights.size()));
        }
        return heldRoomIds;
    }

    /**
     * Tries to hold a room for all the given nights. Each night is only held if
     * no other cart holds it. If one of the nights is already held, the nights
     * held by this call are released again, and nothing is held.
     *
     * @param roomId    the id of the room
     * @param nights    the nights to hold
     * @param holder    who holds the room, stored as the value of each hold
     * @param expiresAt when the holds expire, the same as the cart's expiry
     * @return true if all nights were held, false if any of them was already held
     */
    public boolean tryHoldRoom(UUID roomId, List<LocalDate> nights, String holder, Instant expiresAt){
        List<LocalDate> heldNights = new ArrayList<>();
        for (LocalDate night : nights){
            // Set only if absent, with expiry, in one atomic operation
            Boolean held = redis.opsForValue().setIfAbsent(holdKey(roomId, night), holder, Duration.between(Instant.now(), expiresAt));
            if (!Boolean.TRUE.equals(held)){
                releaseRoom(roomId, heldNights);
                return false;
            }
            heldNights.add(night);
        }
        return true;
    }

    /**
     * Releases a room for the given nights, regardless of who holds it. Only
     * used to undo holds that were just made by the same request.
     *
     * @param roomId the id of the room
     * @param nights the nights to release
     */
    public void releaseRoom(UUID roomId, List<LocalDate> nights){
        if (nights.isEmpty()) return;
        redis.delete(nights.stream().map(night -> holdKey(roomId, night)).toList());
    }

    /**
     * Adds items to the user's cart. The cart keeps its existing expiry.
     *
     * @param userId    the id of the user
     * @param items     the items to add
     * @param expiresAt when the cart expires
     */
    public void addCartItems(UUID userId, List<CartItem> items, Instant expiresAt){
        String key = CART_PREFIX + userId;
        Map<String, String> fields = new HashMap<>();
        for (CartItem item : items){
            fields.put(ITEM_FIELD_PREFIX + item.itemId(), jsonMapper.writeValueAsString(item));
        }
        redis.opsForHash().putAll(key, fields);
        redis.expireAt(key, expiresAt);
    }

    /**
     * Releases everything in the user's cart: the holds on its rooms, and the
     * cart itself. Safe to call several times, and on a cart that has expired,
     * as deleting keys that do not exist does nothing.
     *
     * @param userId the id of the user
     * @return the number of rooms released
     */
    public int releaseCart(UUID userId){
        List<CartItem> items = getCartItems(userId);

        // Holds first, cart last: if this stops halfway, the cart still lists the holds, so a retry can release them
        if (!items.isEmpty()){
            List<String> holdKeys = items.stream()
                    .flatMap(item -> item.from().toLocalDate().datesUntil(item.to().toLocalDate())
                            .map(night -> holdKey(item.roomId(), night)))
                    .toList();
            redis.delete(holdKeys);
        }
        redis.delete(CART_PREFIX + userId);
        return items.size();
    }

    /**
     * @param roomId the id of the room
     * @param night  the night
     * @return the key holding the room for the night
     */
    private String holdKey(UUID roomId, LocalDate night){
        return HOLD_PREFIX + roomId + ":" + night;
    }
}

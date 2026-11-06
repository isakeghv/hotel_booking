package com.example.bookingserviceproject.cache;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.*;

/**
 * Access to the booking cache (Redis) for users' carts. Each cart is one hash,
 * {@code booking:cart:{userId}}, with the fields:
 * <ul>
 *     <li>{@code bookingId}: the id the booking gets when it is confirmed</li>
 *     <li>{@code expiresAt}: when the cart expires, as epoch milliseconds</li>
 *     <li>{@code item:{itemId}}: one room, as JSON</li>
 * </ul>
 * The whole cart expires at one fixed time, set when its first room is added.
 * Adding more rooms does not extend it.
 */
@Component
public class BookingCart {
    private static final String CART_PREFIX = "booking:cart:";
    private static final String BOOKING_ID_FIELD = "bookingId";
    private static final String EXPIRES_AT_FIELD = "expiresAt";
    private static final String ITEM_FIELD_PREFIX = "item:";

    private final StringRedisTemplate redis;
    private final JsonMapper jsonMapper;

    /**
     * Creates the cache access with the Redis client and JSON mapper it uses.
     *
     * @param redis      client for the booking cache
     * @param jsonMapper mapper used to store cart items as JSON
     */
    public BookingCart(StringRedisTemplate redis, JsonMapper jsonMapper){
        this.redis = redis;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Returns when the user's cart expires, without starting a new cart.
     *
     * @param userId the id of the user
     * @return when the cart expires, or empty if the user has no cart
     */
    public Optional<Instant> getExpiry(UUID userId){
        Object stored = redis.opsForHash().get(cartKey(userId), EXPIRES_AT_FIELD);
        return Optional.ofNullable(stored).map(value -> Instant.ofEpochMilli(Long.parseLong((String) value)));
    }

    /**
     * Returns when the user's cart expires. If the user has no cart, a new one
     * is started with the given expiry and a new bookingId. An existing expiry
     * or bookingId is never changed.
     *
     * @param userId    the id of the user
     * @param expiresAt the expiry to use if the cart is new
     * @return when the cart and everything in it expires
     */
    public Instant getOrStartExpiry(UUID userId, Instant expiresAt){
        String key = cartKey(userId);

        // Only the first call sets them, also when several requests arrive at the same time
        redis.opsForHash().putIfAbsent(key, BOOKING_ID_FIELD, UUID.randomUUID().toString());
        Boolean started = redis.opsForHash().putIfAbsent(key, EXPIRES_AT_FIELD, String.valueOf(expiresAt.toEpochMilli()));

        Instant cartExpiry = expiresAt;
        if (!Boolean.TRUE.equals(started)){
            Object stored = redis.opsForHash().get(key, EXPIRES_AT_FIELD);
            if (stored != null) cartExpiry = Instant.ofEpochMilli(Long.parseLong((String) stored));
        }

        // Absolute time, so setting it again does not extend it
        redis.expireAt(key, cartExpiry);
        return cartExpiry;
    }

    /**
     * Adds rooms to the user's cart. The cart must already be started with
     * {@link #getOrStartExpiry(UUID, Instant)}.
     *
     * @param userId    the id of the user
     * @param items     the rooms to add
     * @param expiresAt when the cart expires, set again so the key never ends up without an expiry
     */
    public void addItems(UUID userId, List<CartItem> items, Instant expiresAt){
        String key = cartKey(userId);
        Map<String, String> fields = new HashMap<>();
        for (CartItem item : items){
            fields.put(ITEM_FIELD_PREFIX + item.itemId(), jsonMapper.writeValueAsString(item));
        }

        redis.opsForHash().putAll(key, fields);
        redis.expireAt(key, expiresAt);
    }

    /**
     * Retrieves everything in the user's cart in one request to Redis.
     *
     * @param userId the id of the user
     * @return the cart's contents, or {@link CartContents#empty()} if the user has no cart or it has expired
     */
    public CartContents getCart(UUID userId){
        Map<Object, Object> entries = redis.opsForHash().entries(cartKey(userId));
        Object bookingId = entries.get(BOOKING_ID_FIELD);
        Object expiresAt = entries.get(EXPIRES_AT_FIELD);
        if (bookingId == null || expiresAt == null) return CartContents.empty();

        Instant expiry = Instant.ofEpochMilli(Long.parseLong((String) expiresAt));
        if (!expiry.isAfter(Instant.now())) return CartContents.empty();

        List<CartItem> items = entries.entrySet().stream()
                .filter(entry -> ((String) entry.getKey()).startsWith(ITEM_FIELD_PREFIX))
                .map(entry -> jsonMapper.readValue((String) entry.getValue(), CartItem.class))
                .toList();
        return new CartContents(UUID.fromString((String) bookingId), expiry, items);
    }

    /**
     * @param userId the id of the user
     * @return the key of the user's cart
     */
    private String cartKey(UUID userId){
        return CART_PREFIX + userId;
    }
}

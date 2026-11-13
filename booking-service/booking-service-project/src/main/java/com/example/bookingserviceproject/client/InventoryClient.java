package com.example.bookingserviceproject.client;

import com.example.bookingserviceproject.dto.inventory.InventoryExtraPrice;
import com.example.bookingserviceproject.dto.inventory.InventoryPrimaryImage;
import com.example.bookingserviceproject.dto.inventory.InventoryReservation;
import com.example.bookingserviceproject.exception.PassThroughException;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Internal HTTP calls to the inventory-service. Its refusals (4xx) are passed
 * on with the same status and message, and its failures (5xx, timeouts, not
 * reachable) become a 503.
 */
@Component
public class InventoryClient {
    private static final Logger log = LoggerFactory.getLogger(InventoryClient.class);
    private static final String RESERVE_UNAVAILABLE = "Rooms cannot be reserved right now. Please try again later";
    private static final String EXTRA_UNAVAILABLE = "Extras cannot be added right now. Please try again later";
    private static final String IMAGES_UNAVAILABLE = "Images cannot be fetched right now";
    private static final String DEFAULT_REFUSAL = "The request could not be completed";

    private final RestClient restClient;
    private final JsonMapper jsonMapper;

    /**
     * Creates the client with the HTTP client and JSON mapper it uses.
     *
     * @param inventoryRestClient HTTP client with the inventory-service's base URL and timeouts
     * @param jsonMapper          mapper used to read inventory's error bodies
     */
    public InventoryClient(RestClient inventoryRestClient, JsonMapper jsonMapper){
        this.restClient = inventoryRestClient;
        this.jsonMapper = jsonMapper;
    }

    /**
     * Holds rooms of a variant in the user's cart in the inventory-service.
     * Either all rooms are held, or none.
     *
     * @param userId    the id of the user whose cart the rooms are held in
     * @param variantId the id of the chosen variant
     * @param amount    the number of rooms
     * @param from      the check-in date
     * @param to        the check-out date
     * @return the held rooms, with their itemIds and prices
     * @throws PassThroughException        if inventory refuses, e.g. 404 variant not found, 409 not enough rooms
     * @throws ServiceUnavailableException if inventory fails or cannot be reached
     */
    public InventoryReservation reserveRooms(UUID userId, UUID variantId, int amount, LocalDate from, LocalDate to){
        InventoryReservation.Result result;
        try {
            result = restClient.get()
                    .uri(uri -> uri.path("/inventory/availability/variants/{variantId}")
                            .queryParam("amount", amount)
                            .queryParam("from", from)
                            .queryParam("to", to)
                            .build(variantId))
                    .header("X-User-Id", userId.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw toException(response, RESERVE_UNAVAILABLE);
                    })
                    .body(InventoryReservation.Result.class);
        } catch (ResourceAccessException e){
            log.error("Inventory-service could not be reached to reserve rooms: {}", e.getMessage());
            throw new ServiceUnavailableException(RESERVE_UNAVAILABLE);
        }

        if (result == null || result.reservation() == null || result.reservation().rooms() == null
                || result.reservation().rooms().isEmpty()){
            log.error("Inventory-service returned no reserved rooms for variant {} and user {}", variantId, userId);
            throw new ServiceUnavailableException(RESERVE_UNAVAILABLE);
        }
        return result.reservation();
    }

    /**
     * Prices an extra in the inventory-service. Nothing is held there, as extras are not limited.
     *
     * @param userId  the id of the user whose cart the extra is priced for
     * @param extraId the id of the extra
     * @param amount  how many of the extra
     * @return the extra, with its unit price and total
     * @throws PassThroughException        if inventory refuses, e.g. 404 extra not found, 409 cannot be bought separately
     * @throws ServiceUnavailableException if inventory fails or cannot be reached
     */
    public InventoryExtraPrice priceExtra(UUID userId, UUID extraId, int amount){
        InventoryExtraPrice.Result result;
        try {
            result = restClient.get()
                    .uri(uri -> uri.path("/inventory/availability/extras/{extraId}")
                            .queryParam("amount", amount)
                            .build(extraId))
                    .header("X-User-Id", userId.toString())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw toException(response, EXTRA_UNAVAILABLE);
                    })
                    .body(InventoryExtraPrice.Result.class);
        } catch (ResourceAccessException e){
            log.error("Inventory-service could not be reached to price an extra: {}", e.getMessage());
            throw new ServiceUnavailableException(EXTRA_UNAVAILABLE);
        }

        if (result == null || result.extra() == null || result.extra().extra() == null){
            log.error("Inventory-service returned no price for extra {} and user {}", extraId, userId);
            throw new ServiceUnavailableException(EXTRA_UNAVAILABLE);
        }
        return result.extra();
    }

    /**
     * Fetches the primary image of several room types in one request. A room
     * type without a primary image is returned with its file path and alt text null.
     *
     * @param roomTypeIds the room types, 1-30 (inventory's limit)
     * @return one entry per distinct room type
     * @throws PassThroughException        if inventory refuses, e.g. 400 more than 30 ids
     * @throws ServiceUnavailableException if inventory fails or cannot be reached
     */
    public List<InventoryPrimaryImage> getPrimaryImages(Collection<UUID> roomTypeIds){
        InventoryPrimaryImage.Result result;
        try {
            result = restClient.post()
                    .uri("/inventory/roomtypes/images/primary")
                    .body(Map.of("roomTypeIds", roomTypeIds))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw toException(response, IMAGES_UNAVAILABLE);
                    })
                    .body(InventoryPrimaryImage.Result.class);
        } catch (ResourceAccessException e){
            log.error("Inventory-service could not be reached to fetch primary images: {}", e.getMessage());
            throw new ServiceUnavailableException(IMAGES_UNAVAILABLE);
        }

        if (result == null || result.primaryImages() == null){
            log.error("Inventory-service returned no primary images for {} room types", roomTypeIds.size());
            throw new ServiceUnavailableException(IMAGES_UNAVAILABLE);
        }
        return result.primaryImages();
    }

    /**
     * Converts an error response from the inventory-service to the exception
     * returned to the client.
     *
     * @param response           the error response
     * @param unavailableMessage the message used when inventory fails (5xx)
     * @return a {@link PassThroughException} for refusals (4xx), otherwise a {@link ServiceUnavailableException}
     * @throws IOException if the status cannot be read
     */
    private RuntimeException toException(ClientHttpResponse response, String unavailableMessage) throws IOException {
        HttpStatusCode status = response.getStatusCode();
        if (status.is5xxServerError()){
            log.error("Inventory-service failed with status {}", status.value());
            return new ServiceUnavailableException(unavailableMessage);
        }
        return new PassThroughException(status, readErrorMessage(response));
    }

    /**
     * Reads the message of an error body {@code {"err": "..."}}. Validation
     * errors, where {@code err} is a list, and unreadable bodies get a default message.
     *
     * @param response the error response
     * @return the message to return to the client
     */
    private String readErrorMessage(ClientHttpResponse response){
        try {
            JsonNode err = jsonMapper.readTree(response.getBody()).path("err");
            if (err.isString()) return err.asString();
        } catch (IOException | JacksonException e){
            log.warn("Could not read the error body from the inventory-service: {}", e.getMessage());
        }
        return DEFAULT_REFUSAL;
    }
}

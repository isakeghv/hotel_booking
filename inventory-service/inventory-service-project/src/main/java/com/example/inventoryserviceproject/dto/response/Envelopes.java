package com.example.inventoryserviceproject.dto.response;

import com.example.inventoryserviceproject.dto.FieldError;
import com.example.inventoryserviceproject.model.Bed;
import com.example.inventoryserviceproject.model.Extra;
import com.example.inventoryserviceproject.model.Room;
import com.example.inventoryserviceproject.model.RoomType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The response bodies of the endpoints. Every response is an object with one
 * key, e.g. {@code {"roomTypes": [...]}}. Each record here is one such body,
 * so the key and the content are typed, and Swagger shows the real structure.
 */
public final class Envelopes {

    private Envelopes(){}

    // ----- Availability -----

    /** Body of the room search. */
    public record AvailableRoomTypesResult(
            @Schema(description = "The available room types, cheapest total first. Empty if nothing is available")
            List<AvailableRoomTypeResponse> roomTypes) {}

    /** Body when rooms are held in a cart. */
    public record ReservationResult(AvailabilityResponse reservation) {}

    /** Body when an extra is priced for a cart. */
    public record ExtraPriceResult(ExtraAvailabilityResponse extra) {}

    // ----- Room types, rooms and variants -----

    /** Body when a room type is created. */
    public record CreatedRoomTypeResult(@JsonProperty("RoomType") RoomType roomType) {}

    /** Body of the list of all room types. */
    public record RoomTypesResult(List<RoomType> rooms) {}

    /** Body of a single room type. */
    public record RoomTypeResult(RoomTypeResponse room) {}

    /** Body when a room (room number) is created. */
    public record CreatedRoomResult(Room room) {}

    /** Body of the periods set by staff. */
    public record OccupationsResult(
            @Schema(description = "The periods, earliest first")
            List<OccupiedRoomResponse> occupations) {}

    /** Body of the variants of a room type. */
    public record VariantsResult(RoomTypeVariantsResponse variants) {}

    /** Body of a single variant. */
    public record VariantResult(VariantResponse variant) {}

    /** Body of several variants, keyed by variant id. */
    public record VariantsByIdResult(Map<UUID, VariantIncludesResponse> variants) {}

    // ----- Beds, extras and images -----

    /** Body when a bed type is created. */
    public record CreatedBedResult(Bed created) {}

    /** Body of the list of bed types. */
    public record BedTypesResult(List<Bed> bedTypes) {}

    /** Body when an extra is created. */
    public record CreatedExtraResult(Extra extra) {}

    /** Body of the list of extras. */
    public record ExtrasResult(List<Extra> extras) {}

    /** Body with a message, e.g. after uploading an image. */
    public record MessageResult(String message) {}

    /** Body with the primary image of one room type. */
    public record PrimaryImageResult(
            @Schema(description = "The room type's primary image")
            PrimaryImageResponse primaryImageResponse) {}

    /** Body with the primary image of several room types. */
    public record PrimaryImagesResult(
            @Schema(description = "One entry per requested room type, in the order given. " +
                    "`filePath` and `altText` are null if the room type has no primary image or does not exist")
            List<PrimaryImageResponse> primaryImages) {}

    // ----- Errors -----

    /** Body of an error. The message can be shown to users. */
    public record ErrorResult(
            @Schema(description = "What went wrong", example = "Room not found")
            String err) {}

    /** Body when the input is invalid: one entry per invalid field. */
    public record ValidationErrorResult(List<FieldError> err) {}
}

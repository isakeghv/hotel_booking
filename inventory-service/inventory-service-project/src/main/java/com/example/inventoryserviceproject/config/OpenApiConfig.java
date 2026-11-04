package com.example.inventoryserviceproject.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configures the OpenAPI (Swagger) documentation of the service: the overview
 * shown at the top of Swagger UI, and the groups the endpoints are sorted in.
 */
@Configuration
public class OpenApiConfig {

    private static final String DESCRIPTION = """
            Manages the hotel's rooms and what can be booked: room types, single rooms, variants, \
            extras, beds and images, and which rooms are available when.

            ## Who calls what
            | Access | Meaning |
            |---|---|
            | **Public** | No sign-in needed, reached through the API gateway. |
            | **Signed in** | Any signed-in user: customer, employee, manager or owner (permission level 0–3). |
            | **Staff** | Employee, manager or owner (permission level 1–3). |
            | **Manager** | Manager or owner (permission level 2–3). |
            | **Internal** | Only called by the booking-service inside the Docker network. Not routed by the gateway. |

            The gateway checks the sign-in and adds the headers `X-User-Id` and `X-Permission-Level`. \
            Clients never send these themselves.

            ## Booking flow (for the booking-service)
            1. Customer searches: `GET /inventory/availability` returns available room types with prices.
            2. Customer picks a room type: `GET /inventory/roomtypes/{roomTypeId}/variants` returns its variants with prices.
            3. Customer adds rooms to the cart: `GET /inventory/availability/variants/{variantId}` holds the rooms \
            for **30 minutes** from the first room added, and returns an `itemId` per room. Store these.
            4. Customer adds extras: `GET /inventory/availability/extras/{extraId}` prices an extra for the stay in the cart.
            5. Kafka events finish the flow: `reservation-confirmed` stores the rooms, `reservation-item-remove` removes one \
            room, `reservation-cancelled` releases the whole cart, `booking-cancelled` frees the rooms of a paid booking. \
            Payloads and behaviour: [Kafka documentation](/inventory/springwolf/asyncapi-ui.html).

            ## Conventions
            - **Responses** are wrapped in an object with one key, e.g. `{"roomTypes": [...]}`. The key is named in each endpoint.
            - **Errors** have the key `err`: a message, e.g. `{"err": "Room not found"}`, or for invalid input a list, \
            e.g. `{"err": [{"field": "from", "error": "Check-in date cannot be in the past"}]}`. Messages can be shown to users.
            - **Dates** in queries are `YYYY-MM-DD`. Check-in is 15:00 and check-out 12:00, so a stay from the 5th to the 8th is 3 nights.
            - **Prices** are whole USD. All totals are calculated by the server; clients never need to calculate prices.
            """;

    /**
     * @return the OpenAPI description of the service
     */
    @Bean
    public OpenAPI inventoryOpenApi(){
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory service API")
                        .version("1.0")
                        .description(DESCRIPTION))
                // Relative, so "Try it out" sends requests through the gateway the page was opened from
                .servers(List.of(new Server().url("/").description("Through the API gateway")))
                .tags(List.of(
                        new Tag().name("Availability").description("Searching for available rooms, and holding rooms in carts"),
                        new Tag().name("Room types").description("Types of rooms that can be booked, with their beds and included extras"),
                        new Tag().name("Rooms").description("Single physical rooms (room numbers), and when they are unavailable"),
                        new Tag().name("Variants").description("Options of a room type, e.g. with or without breakfast, which customers choose between"),
                        new Tag().name("Extras").description("Add-ons, e.g. breakfast or parking, either included in rooms or bought separately"),
                        new Tag().name("Beds").description("Bed types that rooms are furnished with"),
                        new Tag().name("Images").description("Images of room types")));
    }
}

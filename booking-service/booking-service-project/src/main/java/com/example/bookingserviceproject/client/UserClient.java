package com.example.bookingserviceproject.client;

import com.example.bookingserviceproject.dto.user.BasicUserInfo;
import com.example.bookingserviceproject.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Internal HTTP calls to the user-service. Every failure (an error status, a
 * timeout, not reachable, an unreadable body) becomes a {@link ServiceUnavailableException}.
 */
@Component
public class UserClient {
    private static final Logger log = LoggerFactory.getLogger(UserClient.class);
    private static final String USERS_UNAVAILABLE = "Users cannot be fetched right now";
    private static final ParameterizedTypeReference<Map<UUID, BasicUserInfo>> USERS_BY_ID = new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    /**
     * Creates the client with the HTTP client it uses.
     *
     * @param userRestClient HTTP client with the user-service's base URL and timeouts
     */
    public UserClient(RestClient userRestClient){
        this.restClient = userRestClient;
    }

    /**
     * Fetches the basic information of several users in one request. A user
     * the user-service does not know is returned with its fields null.
     *
     * @param userIds the users, 1-30 (the user-service's limit). A {@code Set}, as the user-service fails on duplicates
     * @return the users by user id
     * @throws ServiceUnavailableException if the user-service fails, refuses or cannot be reached
     */
    public Map<UUID, BasicUserInfo> getBasicUsers(Set<UUID> userIds){
        Map<UUID, BasicUserInfo> users;
        try {
            users = restClient.post()
                    .uri("/users/basic")
                    .body(Map.of("userIds", userIds))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        log.error("User-service failed to return basic users with status {}", response.getStatusCode().value());
                        throw new ServiceUnavailableException(USERS_UNAVAILABLE);
                    })
                    .body(USERS_BY_ID);
        } catch (RestClientException e){
            log.error("User-service could not be reached to fetch basic users: {}", e.getMessage());
            throw new ServiceUnavailableException(USERS_UNAVAILABLE);
        }

        if (users == null){
            log.error("User-service returned no basic users for {} user ids", userIds.size());
            throw new ServiceUnavailableException(USERS_UNAVAILABLE);
        }
        return users;
    }

    /**
     * Fetches the basic information of one user.
     *
     * @param userId the id of the user
     * @return the user, or empty if the user-service does not know the user (404)
     * @throws ServiceUnavailableException if the user-service fails, refuses otherwise or cannot be reached
     */
    public Optional<BasicUserInfo> getBasicUser(UUID userId){
        try {
            return restClient.get()
                    .uri("/users/{userId}/basic", userId)
                    .exchange((request, response) -> {
                        // Unknown user: an answer, not a failure
                        if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) return Optional.empty();
                        if (response.getStatusCode().isError()){
                            log.error("User-service failed to return a basic user with status {}", response.getStatusCode().value());
                            throw new ServiceUnavailableException(USERS_UNAVAILABLE);
                        }
                        BasicUserInfo.Result result = response.bodyTo(BasicUserInfo.Result.class);
                        if (result == null || result.user() == null){
                            log.error("User-service returned no basic user for user {}", userId);
                            throw new ServiceUnavailableException(USERS_UNAVAILABLE);
                        }
                        return Optional.of(result.user());
                    });
        } catch (RestClientException e){
            log.error("User-service could not be reached to fetch a basic user: {}", e.getMessage());
            throw new ServiceUnavailableException(USERS_UNAVAILABLE);
        }
    }
}

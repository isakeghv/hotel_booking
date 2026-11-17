package com.example.bookingserviceproject.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Configuration providing the HTTP clients used for calling other services.
 */
@Configuration
public class RestClientConfig {
    // Short timeouts, so a slow or unreachable service does not block requests
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Creates the client for internal calls to the inventory-service, inside
     * the Docker network and not through the gateway.
     *
     * @param baseUrl the inventory-service's base URL, e.g. {@code http://inventory-service:3002}
     * @return the client, with the base URL and timeouts set
     */
    @Bean
    public RestClient inventoryRestClient(@Value("${inventory.base-url}") String baseUrl){
        return internalClient(baseUrl);
    }

    /**
     * Creates the client for internal calls to the user-service, inside the
     * Docker network and not through the gateway.
     *
     * @param baseUrl the user-service's base URL, e.g. {@code http://user-service:3001}
     * @return the client, with the base URL and timeouts set
     */
    @Bean
    public RestClient userRestClient(@Value("${user.base-url}") String baseUrl){
        return internalClient(baseUrl);
    }

    /**
     * Builds a client for internal calls, with the base URL and timeouts set.
     *
     * @param baseUrl the service's base URL
     * @return the client
     */
    private RestClient internalClient(String baseUrl){
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }
}

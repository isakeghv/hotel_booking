package com.example.inventoryserviceproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration providing the HTTP client beans used for calling other
 * services.
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Creates the {@link RestTemplate} used for synchronous HTTP calls.
     *
     * @return a default {@code RestTemplate} instance
     */
    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}

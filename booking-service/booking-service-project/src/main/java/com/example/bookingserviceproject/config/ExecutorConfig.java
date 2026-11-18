package com.example.bookingserviceproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Configuration providing the executor used to call other services in parallel.
 */
@Configuration
public class ExecutorConfig {

    /**
     * Creates the executor for calls to other services that run in parallel.
     * Each call gets its own virtual thread: they mostly wait for the answer,
     * so they are cheap, and many requests at once do not wait for a free thread.
     * Closed when the application stops.
     *
     * @return the executor
     */
    @Bean(destroyMethod = "close")
    public ExecutorService externalCallExecutor(){
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}

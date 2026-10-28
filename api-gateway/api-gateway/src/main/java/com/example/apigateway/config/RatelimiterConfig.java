package com.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * Configuration for the rate limiter's Redis connection and the key
 * resolution used to identify clients.
 */
@Configuration
public class RatelimiterConfig {

    @Value("${SPRING_DATA_REDIS_HOST}")
    private String rateLimiterRedisHost;

    /**
     * Creates the reactive connection factory for the rate limiter's Redis
     * instance. The host is read from the {@code SPRING_DATA_REDIS_HOST}
     * environment variable and the default port 6379 is used. Marked as primary
     * so it is chosen by default over the blocklist connection factory.
     *
     * @return the primary connection factory, used by the rate limiter
     */
    @Bean
    @Primary
    public ReactiveRedisConnectionFactory rateLimiterRedisConnectionFactory(){
        return new LettuceConnectionFactory(rateLimiterRedisHost, 6379);
    }

    /**
     * Creates the key resolver that identifies clients by IP address for rate
     * limiting. Note that behind another reverse proxy, the remote address is
     * the proxy's address unless forwarded headers are resolved.
     *
     * @return a resolver mapping each request to the caller's IP address
     */
    @Bean
    public KeyResolver ipKeyResolver(){
        return exchange -> Mono.just(
                Objects.requireNonNull(exchange.getRequest().getRemoteAddress()).getAddress().getHostAddress()
        );
    }
}

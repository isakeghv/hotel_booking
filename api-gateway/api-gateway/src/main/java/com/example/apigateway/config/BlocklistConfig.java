package com.example.apigateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration for the Redis connection used by the session blocklist.
 * <p>
 * Kept separate from the rate limiter's Redis setup so the two can point to
 * different Redis instances.
 */
@Configuration
public class BlocklistConfig {
    @Value("${BLOCKLIST_REDIS_HOST}")
    private String blocklistRedisHost;

    /**
     * Creates the reactive connection factory for the blocklist Redis instance.
     * The host is read from the {@code BLOCKLIST_REDIS_HOST} environment
     * variable and the default port 6379 is used.
     *
     * @return the connection factory for the blocklist Redis instance
     */
    @Bean
    @Qualifier("blocklistRedisConnectionFactory")
    public ReactiveRedisConnectionFactory blocklistRedisConnectionFactory(){
        return new LettuceConnectionFactory(blocklistRedisHost, 6379);
    }

    /**
     * Creates the reactive Redis template used to store revoked session ids.
     * Both keys and values are serialized as plain strings.
     *
     * @param connectionFactory the blocklist connection factory
     * @return the template for reading and writing blocklist entries
     */
    @Bean
    public ReactiveRedisTemplate<String, String> blocklistRedisTemplate(
            @Qualifier("blocklistRedisConnectionFactory") ReactiveRedisConnectionFactory connectionFactory
    ){
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}

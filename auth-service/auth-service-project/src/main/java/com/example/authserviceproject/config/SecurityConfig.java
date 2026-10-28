package com.example.authserviceproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for this service.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain. CSRF protection is disabled and all requests are permitted, so no authentication is enforced by this service itself.
     *
     * @param http the HTTP security builder to configure
     * @return the built security filter chain
     * @throws Exception if the filter chain cannot be built
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    /**
     * Creates the password encoder. The delegating encoder prefixes each hash with the algorithm used, which allows the algorithm to change over time while older hashes still verify.
     *
     * @return a delegating password encoder using Spring Security's default algorithm
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

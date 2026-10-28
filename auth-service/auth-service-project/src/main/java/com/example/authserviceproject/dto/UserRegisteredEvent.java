package com.example.authserviceproject.dto;

import java.util.UUID;

/**
 * Event published to Kafka when a new user has been registered.
 *
 * @param userId the unique id of the newly created user
 * @param firstName the user's first name
 * @param lastName the user's last name
 * @param email the user's email address
 * @param phoneNumber the user's phone number
 * @param countryCode the country code belonging to the phone number
 */
public record UserRegisteredEvent(UUID userId, String firstName, String lastName, String email, String phoneNumber, String countryCode) {}

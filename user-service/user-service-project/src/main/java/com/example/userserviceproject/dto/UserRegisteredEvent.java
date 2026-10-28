package com.example.userserviceproject.dto;

import java.util.UUID;

/**
 * Event published by the auth-service (microservice) when a new user has registered.
 * Carries the data needed to create the user's profile in this service.
 *
 * @param userId      the unique id of the newly registered user
 * @param firstName   the user's first name
 * @param lastName    the user's last name
 * @param email       the user's email address
 * @param phoneNumber the user's phone number, without country code
 * @param countryCode the country calling code for the phone number
 */
public record UserRegisteredEvent(UUID userId, String firstName, String lastName, String email, String phoneNumber, String countryCode) { }

package com.example.authserviceproject.dto;

import jakarta.validation.constraints.*;

/**
 * Request body for signing in. Each component is validated with Bean Validation annotations.
 *
 * @param email the user's email address, 6-80 characters in a valid email format
 * @param password the user's password, 8-150 characters with at least 2 lowercase, 2 uppercase, 2 digits and 2 special characters
 */
public record ReviewSignin(
        @NotBlank (message = "Email is required")
        @Size(max = 80, min = 6, message = "Must be between 6 and 80 characters long")
        @Email(message = "Email must be valid email format")
        String email,

        @NotBlank (message = "Password is required")
        @Size(max =  150, min =  8, message = "Must be between 8 and 150 characters long")
        @Pattern(
                regexp = "^(?=(.*[A-Z]){2,})(?=(.*[a-z]){2,})(?=(.*\\d){2,})(?=(.*[^a-zA-Z0-9]){2,}).+$",
                message =  "The password must contain at least 2 lowercase, 2 uppercase, 2 numbers, and 2 special characters."
        )
        String password
) {
    /**
     * Compact constructor that trims leading and trailing whitespace from the email and password. Null values are left as null so validation can report them.
     */

    // TODO: Add proper sanitation
    public ReviewSignin {
        email = email == null ? null : email.trim();
        password = password == null ? null : password.trim();
    }
}

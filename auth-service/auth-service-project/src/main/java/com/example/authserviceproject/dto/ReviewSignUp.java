package com.example.authserviceproject.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request body for registering a new user. Each component is validated with Bean Validation annotations.
 *
 * @param firstName the user's first name, 3-30 alphabetical characters
 * @param lastName the user's last name, 3-50 alphabetical characters
 * @param countryCode the phone country code, written as {@code 00} followed by 1-3 digits
 * @param phoneNumber the phone number, which must pass {@link ValidPhoneNumber} validation
 * @param email the user's email address, 6-80 characters in a valid email format
 * @param password the user's password, 8-150 characters with at least 2 lowercase, 2 uppercase, 2 digits and 2 special characters
 */
public record ReviewSignUp(
        @NotBlank(message = "First name is required")
        @Size(max = 30, min = 3, message = "Must be between 3 and 30 characters long.")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "Must be alphabetical")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 50, min = 3, message = "Must be between 3 and 50 characters long.")
        @Pattern(regexp = "^[a-zA-Z]+$", message = "Must be alphabetical")
        String lastName,

        @NotBlank(message = "Country code is required")
        @Pattern(regexp = "^00\\d{1,3}$", message = "Must be numerical")
        String countryCode,

        @NotBlank(message = "Phone number code is required")
        @ValidPhoneNumber(message = "Must be a valid phone number")
        String phoneNumber,

        @NotBlank(message = "Email is required")
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
     * Compact constructor that trims leading and trailing whitespace from the first name, last name, email and password. Null values are left as null so validation can report them.
     */
    // TODO: Add proper sanitation
    public ReviewSignUp {
        firstName = firstName == null ? null : firstName.trim();
        lastName = lastName == null ? null : lastName.trim();
        email = email == null ? null : email.trim();
        password = password == null ? null : password.trim();
    }
}

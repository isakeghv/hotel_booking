package com.example.authserviceproject.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service for hashing and verifying passwords with the configured {@link PasswordEncoder}.
 */
@Service
public class PasswordService {
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates the service with the encoder used for hashing.
     *
     * @param passwordEncoder the password encoder
     */
    public PasswordService(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Hashes a raw password.
     *
     * @param rawPassword the plain text password
     * @return the encoded password
     */
    public String hashPassword(String rawPassword){
        return passwordEncoder.encode(rawPassword);
    }

    /**
     * Checks a raw password against an encoded password.
     *
     * @param rawPassword the plain text password
     * @param encodedPassword the stored encoded password
     * @return true if the passwords match
     */
    public boolean checkPassword(String rawPassword, String encodedPassword){
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

}

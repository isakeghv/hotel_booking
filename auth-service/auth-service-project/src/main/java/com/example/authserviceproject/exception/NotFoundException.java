package com.example.authserviceproject.exception;

/**
 * Thrown when a requested resource does not exist. Results in a 404 response.
 */
public class NotFoundException extends RuntimeException{
    /**
     * Creates the exception with a message that is returned to the client.
     *
     * @param message the error message
     */
    public NotFoundException(String message) {super(message); }
}

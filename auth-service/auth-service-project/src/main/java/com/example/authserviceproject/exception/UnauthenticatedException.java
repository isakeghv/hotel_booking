package com.example.authserviceproject.exception;

/**
 * Thrown when the caller could not be authenticated. Results in a 401 response.
 */
public class UnauthenticatedException extends RuntimeException{
    /**
     * Creates the exception with a message that is returned to the client.
     *
     * @param message the error message
     */
    public UnauthenticatedException(String message) {super(message); }
}


package com.example.bookingserviceproject.exception;

/**
 * Thrown when another service the request depends on cannot be reached, or fails. Results in a 503 response.
 */
public class ServiceUnavailableException extends RuntimeException{
    /**
     * Creates the exception with a message that is returned to the client.
     *
     * @param message the error message
     */
    public ServiceUnavailableException(String message) {super(message); }
}

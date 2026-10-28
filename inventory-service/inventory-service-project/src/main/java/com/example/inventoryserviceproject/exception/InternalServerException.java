package com.example.inventoryserviceproject.exception;

/**
 * Thrown when an unexpected server-side failure occurs. Results in a 500 response.
 */
public class InternalServerException extends RuntimeException{
    /**
     * Creates the exception with a message that is returned to the client.
     *
     * @param message the error message
     */
    public InternalServerException(String message) {super(message); }
}

package com.example.inventoryserviceproject.exception;

/**
 * Thrown when a request conflicts with existing state, e.g. a resource that already exists. Results in a 409 response.
 */
public class ConflictException extends RuntimeException{
    /**
     * Creates the exception with a message that is returned to the client.
     *
     * @param message the error message
     */
    public ConflictException(String message) {super(message); }
}

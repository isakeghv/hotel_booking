package com.example.inventoryserviceproject.exception;

/**
 * Thrown when the mediatype is unsupported. Results in a 415 response.
 */
public class UnsupportedMediaException extends RuntimeException {

    /**
     * Creates the exception with a message that is returned to the client.
     *
     * @param message the error message
     */
    public UnsupportedMediaException(String message) {
        super(message);
    }
}

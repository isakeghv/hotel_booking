package com.example.userserviceproject.exception;

/**
 * Thrown when the caller is authenticated but not permitted to perform the action. Results in a 403 response.
 */
public class UnauthorizedException extends RuntimeException{
    /**
     * Creates the exception with a message that is returned to the client.
     *
     * @param message the error message
     */
    public UnauthorizedException(String message) {super(message); }
}

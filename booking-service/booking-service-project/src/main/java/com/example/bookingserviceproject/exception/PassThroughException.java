package com.example.bookingserviceproject.exception;

import org.springframework.http.HttpStatusCode;

/**
 * Thrown when another service refuses a request (4xx), so its status and
 * message are passed on to the client unchanged, e.g. 409 "No rooms of this
 * type are available for the selected dates" from the inventory-service.
 */
public class PassThroughException extends RuntimeException{
    private final HttpStatusCode status;

    /**
     * Creates the exception with the other service's status and message.
     *
     * @param status  the status the other service answered with
     * @param message the error message, returned to the client
     */
    public PassThroughException(HttpStatusCode status, String message) {
        super(message);
        this.status = status;
    }

    /**
     * @return the status the other service answered with
     */
    public HttpStatusCode getStatus() { return status; }
}

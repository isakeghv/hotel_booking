package com.example.bookingserviceproject.exception;

/**
 * Thrown when an event could not be sent to Kafka. Not returned to the client
 * as is: the service that sends the event decides what the user is told.
 */
public class EventPublishException extends RuntimeException{
    /**
     * Creates the exception for the topic the event could not be sent to.
     *
     * @param topic the topic of the event
     * @param cause why sending failed
     */
    public EventPublishException(String topic, Throwable cause) {super("Could not send event to " + topic, cause); }
}

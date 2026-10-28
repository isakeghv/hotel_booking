package com.example.authserviceproject.service;

import com.example.authserviceproject.dto.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for publishing user events to Kafka.
 */
@Service
public class UserEventProducer {
    private final KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate;

    /**
     * Creates the producer with the Kafka template used for sending.
     *
     * @param kafkaTemplate the template used to send {@link UserRegisteredEvent} messages
     */
    public UserEventProducer(KafkaTemplate<String, UserRegisteredEvent> kafkaTemplate){
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes a user registered event to the {@code user-registered} topic, using the user id as message key.
     *
     * @param event the event to publish
     */
    public void publishUserRegistered(UserRegisteredEvent event){
        kafkaTemplate.send("user-registered", event.userId().toString(), event);
    }
}

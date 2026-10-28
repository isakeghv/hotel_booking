package com.example.userserviceproject.service;

import com.example.userserviceproject.dto.UserRegisteredEvent;
import com.example.userserviceproject.model.User;
import com.example.userserviceproject.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


/**
 * Kafka consumer that reacts to events published by the auth-service when a
 * new user registers, and creates the matching user profile in this service.
 */
@Service
public class RegisterUserEventConsumer {
    private final UserRepository userRepository;

    /**
     * Creates the consumer with the repository used to persist users.
     *
     * @param userRepository the repository used to save new users
     */
    public RegisterUserEventConsumer(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Handles a {@code user-registered} event from the auth-service by creating
     * an active {@link User} from the event data and saving it. Listens as part
     * of the {@code user-service-group} consumer group.
     *
     * @param event the registration event published by the auth-service (microservice)
     */
    @KafkaListener(topics = "user-registered", groupId = "user-service-group")
    public void handleUserRegistered(UserRegisteredEvent event){
        User user = new User(event);
        userRepository.save(user);
    }
}

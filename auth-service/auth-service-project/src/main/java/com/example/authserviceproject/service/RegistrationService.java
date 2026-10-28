package com.example.authserviceproject.service;

import com.example.authserviceproject.dto.ReviewSignUp;
import com.example.authserviceproject.dto.UserRegisteredEvent;
import com.example.authserviceproject.exception.ConflictException;
import com.example.authserviceproject.mapper.UserEventMapper;
import com.example.authserviceproject.model.User;
import com.example.authserviceproject.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Service for registering new users.
 */
@Service
public class RegistrationService {
    private final UserRepository userRepo;
    private final PasswordService pwdService;
    private final UserEventProducer userEventProducer;

    /**
     * Creates the service with its dependencies.
     *
     * @param userRepository repository used to check for and store users
     * @param passwordService service used to hash passwords
     * @param userEventProducer producer used to publish the registration event
     */
    public RegistrationService(UserRepository userRepository, PasswordService passwordService, UserEventProducer userEventProducer){
        this.userRepo = userRepository;
        this.pwdService = passwordService;
        this.userEventProducer = userEventProducer;
    }

    /**
     * Registers a new user. The password is hashed before the user is saved, and a {@link UserRegisteredEvent} is published afterward.
     *
     * @param req the validated sign-up request
     * @return the saved user
     * @throws ConflictException if the email is already in use
     */
    public User registerUser(ReviewSignUp req) {
        // Check if user already exists. If true, return exception
        boolean userExists = userRepo.existsByCredentials_Email(req.email());
        if (userExists) throw new ConflictException("Email already in use");

        // Creating userObj, and setting pwd to be hashed pwd.
        User user = new User(req);
        String hashedPassword = pwdService.hashPassword(req.password());
        user.getCredentials().setPassword(hashedPassword);

        userRepo.save(user);

        UserRegisteredEvent event = UserEventMapper.toUserRegisteredEvent(user, req);
        userEventProducer.publishUserRegistered(event);

        return user;
    }
}

package com.example.userserviceproject.model;

import com.example.userserviceproject.dto.UserRegisteredEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * JPA entity representing a user profile, stored in the {@code users} table.
 * Created from a {@link UserRegisteredEvent} when a user registers in the
 * auth-service.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID userId;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String phoneNumber;
    private String email;
    private boolean isActive;

    /**
     * Creates an empty user. Required by JPA, which instantiates entities
     * through the no-argument constructor.
     */
    public User(){}

    /**
     * Creates a new, active user from a registration event.
     *
     * @param event the registration event containing the user's details
     */
    public User(UserRegisteredEvent event){
        this.userId = event.userId();
        this.firstName = event.firstName();
        this.lastName = event.lastName();
        this.email = event.email();
        this.countryCode = event.countryCode();
        this.phoneNumber = event.phoneNumber();
        this.isActive = true;
    }

    /**
     * Returns the unique id of the user.
     *
     * @return the user id
     */
    public UUID getUserId(){ return userId; }

    /**
     * Returns the user's first name.
     *
     * @return the first name
     */
    public String getFirstName(){ return firstName; }

    /**
     * Returns the user's last name.
     *
     * @return the last name
     */
    public String getLastName(){ return lastName; }

    /**
     * Returns the country calling code for the user's phone number.
     *
     * @return the country code
     */
    public String getCountryCode(){ return countryCode; }

    /**
     * Returns the user's phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber(){ return phoneNumber; }

    /**
     * Returns the user's email address.
     *
     * @return the email address
     */
    public String getEmail(){ return email; }

    /**
     * Returns whether the user's account is active. New users are active by
     * default.
     *
     * @return true if the user is active, false otherwise
     */
    public boolean getIsActive(){ return isActive; }

}

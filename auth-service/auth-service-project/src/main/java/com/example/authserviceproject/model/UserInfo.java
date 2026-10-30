package com.example.authserviceproject.model;

import jakarta.persistence.Embeddable;

/**
 * Embeddable holding a user's personal information.
 */
@Embeddable
public class UserInfo {
    private String firstName;
    private String lastName;
    private String phoneNumber;

    /**
     * Creates empty user information. Required by JPA.
     */
    public UserInfo(){}

    /**
     * Creates user information with all fields specified.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param phoneNumber the phone number in international format, including the country calling code
     */
    public  UserInfo(String firstName, String lastName, String phoneNumber){
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns the first name.
     *
     * @return the first name
     */
    public String getFirstName(){return firstName; }

    /**
     * Sets the first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName){ this.firstName = firstName; }

    /**
     * Returns the last name.
     *
     * @return the last name
     */
    public String getLastName(){return lastName; }

    /**
     * Sets the last name.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName){ this.lastName = lastName; }

    /**
     * Returns the phone number, including the country calling code.
     *
     * @return the phone number
     */
    public String getPhoneNumber(){return phoneNumber; }

    /**
     * Sets the phone number.
     *
     * @param phoneNumber the phone number in international format, including the country calling code
     */
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }
}

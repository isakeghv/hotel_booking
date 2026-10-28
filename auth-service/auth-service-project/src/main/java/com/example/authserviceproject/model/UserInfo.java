package com.example.authserviceproject.model;

import jakarta.persistence.Embeddable;

/**
 * Embeddable holding a user's personal information.
 */
@Embeddable
public class UserInfo {
    private String firstName;
    private String lastName;
    private String countryCode;
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
     * @param countryCode the phone country code
     * @param phoneNumber the phone number without country code
     */
    public  UserInfo(String firstName, String lastName, String countryCode, String phoneNumber){
        this.firstName = firstName;
        this.lastName = lastName;
        this.countryCode = countryCode;
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
     * Returns the phone country code.
     *
     * @return the country code
     */
    public String getCountryCode(){ return countryCode; }

    /**
     * Sets the phone country code.
     *
     * @param countryCode the country code
     */
    public void setCountryCode(String countryCode){ this.countryCode = countryCode; }

    /**
     * Returns the phone number without country code.
     *
     * @return the phone number
     */
    public String getPhoneNumber(){return phoneNumber; }

    /**
     * Sets the phone number.
     *
     * @param phoneNumber the phone number without country code
     */
    public void setPhoneNumber(String phoneNumber){ this.phoneNumber = phoneNumber; }

    /**
     * Returns the phone number with the country code placed in front of it.
     *
     * @return the country code and phone number concatenated
     */
    public String getFullPhoneNumber(){ return countryCode + phoneNumber; }
}

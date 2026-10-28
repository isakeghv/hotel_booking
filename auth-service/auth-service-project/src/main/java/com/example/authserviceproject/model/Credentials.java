package com.example.authserviceproject.model;

import jakarta.persistence.Embeddable;

/**
 * Embeddable holding a user's login credentials. Stored as columns of the owning {@link User} entity.
 */
@Embeddable
public class Credentials {
    private String email;
    private String password;

    /**
     * Creates empty credentials. Required by JPA.
     */
    public Credentials(){}

    /**
     * Creates credentials with the given email and password.
     *
     * @param email the user's email address
     * @param password the user's password, hashed or raw depending on the caller
     */
    public Credentials(String email, String password){
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the email address.
     *
     * @return the user's email address
     */
    public String getEmail(){ return email; }

    /**
     * Sets the email address.
     *
     * @param email the user's email address
     */
    public void setEmail(String email){ this.email = email; }

    /**
     * Returns the password.
     *
     * @return the stored password value
     */
    public String getPassword(){ return password; }

    /**
     * Sets the password.
     *
     * @param password the password value to store, which should already be hashed
     */
    public void  setPassword(String password){ this.password = password; }
}

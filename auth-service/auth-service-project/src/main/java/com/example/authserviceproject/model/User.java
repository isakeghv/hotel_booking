package com.example.authserviceproject.model;

import com.example.authserviceproject.dto.ReviewSignUp;
import com.example.authserviceproject.dto.ReviewSignin;
import jakarta.persistence.*;

import java.util.UUID;

/**
 * Entity representing a user and their sign-in details, stored in the {@code user_credentials} table. The personal information in {@link UserInfo} is transient and is not persisted by this entity.
 */
@Entity
@Table (name = "user_credentials")
public class User {
    @Id
    private UUID userId;
    private int permissionLevel;
    private boolean isActive;


    @Embedded
    private Credentials credentials;

    @Transient
    private UserInfo info;

    /**
     * Creates an empty user. Required by JPA.
     */
    public User(){}

    /**
     * Creates a user with all fields specified.
     *
     * @param userId the unique id of the user
     * @param credentials the user's email and password
     * @param permissionLevel the user's permission level
     * @param isActive whether the user is active
     * @param userInfo the user's personal information
     */
    public User(UUID userId, Credentials credentials, int permissionLevel, boolean isActive, UserInfo userInfo){
        this.userId = userId;
        this.credentials = credentials;
        this.permissionLevel = permissionLevel;
        this.isActive = isActive;
        this.info = userInfo;
    }

    /**
     * Creates a new active user with a random id and permission level 0 from a sign-up request. The password is taken as-is, so it must be hashed afterwards.
     *
     * @param req the sign-up request
     */
    public User(ReviewSignUp req){
        this.userId = UUID.randomUUID();
        this.credentials = new Credentials(req.email(), req.password());
        this.permissionLevel = 0;
        this.isActive = true;
        this.info = new UserInfo(req.firstName(), req.lastName(), req.countryCode(), req.phoneNumber());
    }

    /**
     * Creates a user holding only the credentials from a sign-in request. All other fields are left unset.
     *
     * @param req the sign-in request
     */
    public User(ReviewSignin req){
        this.credentials = new Credentials(req.email(), req.password());
    }

    /**
     * Returns whether the user is active.
     *
     * @return true if the user is active, false if deactivated
     */
    public boolean getIsActive(){ return this.isActive; }

    /**
     * Sets whether the user is active.
     *
     * @param active true to activate the user, false to deactivate
     */
    public void setIsActive(boolean active){ this.isActive = active; }

    /**
     * Returns the user's permission level.
     *
     * @return the permission level
     */
    public int getPermissionLevel() { return this.permissionLevel; }

    /**
     * Changes the user's permission level. A user with level 0 (customer) cannot be changed, and the new level must be between 0 and 3.
     *
     * @param level the new permission level
     * @throws Exception if the current level is 0, or if the new level is outside 0-3
     */
    public void setPermissionLevel(int level) throws Exception{
        if (this.permissionLevel == 0) {
            throw new Exception("Cannot upgrade customer to employee");
        }
        if ( level > 3 || level < 0) {
            throw new Exception("Permission level must be between 0-3");
        }

        this.permissionLevel = level;
    }

    /**
     * Returns the unique id of the user.
     *
     * @return the user id
     */
    public UUID getUserId(){ return this.userId; }

    /**
     * Sets the unique id of the user.
     *
     * @param userId the user id
     */
    public void setUserId(UUID userId){ this.userId = userId; }

    /**
     * Returns the user's credentials.
     *
     * @return the credentials
     */
    public Credentials getCredentials() { return credentials; }

    /**
     * Sets the user's credentials.
     *
     * @param credentials the credentials
     */
    public void setCredentials(Credentials credentials){ this.credentials = credentials; }

    /**
     * Returns the user's personal information.
     *
     * @return the personal information, or null if it was not set
     */
    public UserInfo getInfo() { return info; }

    /**
     * Sets the user's personal information.
     *
     * @param info the personal information
     */
    public void setInfo(UserInfo info) { this.info = info; }
}

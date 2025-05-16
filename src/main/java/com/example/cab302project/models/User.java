package com.example.cab302project.models;

/**
 * Represents a user in the system.
 * This class holds user details such as username, password, email, bio,
 * profile image, and a unique db ID.
 */
public class User {
    /**
     * The user's unique username.
     */
    private String username;
    /**
     * The user's password.
     */
    private String password;
    /**
     * The user's unique email address, often used as a primary identifier.
     */
    private String email;
    /**
     * The user's bio text.
     */
    private String bio;
    /**
     * A identifier for the user's profile image.
     */
    private String profileImage;
    /**
     * The unique identifier assigned to the user in the database.
     */
    private int id; // Typically set by the database upon insertion.

    /**
     * Constructs a new User object with essential details from when registering.
     * @param username The username for the new user.
     * @param password The password for the new user.
     * @param email The email address for the new user.
     */
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    /**
     * Gets the username of the user.
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     * @param username The new username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the password of the user.
     * Note: Exercise caution when exposing password data.
     * @return The password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user.
     * @param password The new password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the email address of the user.
     * @return The email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     * @param email The new email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets the bio for the user.
     * @param bio The new bio text to set.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }

    /**
     * Gets the bio of the user.
     * @return The bio text.
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the profile image reference/identifier for the user.
     * Note: See field comment regarding the type and usage of this field.
     * @param profileImage The new profile image reference string to set.
     */
    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    /**
     * Gets the unique database ID of the user.
     * @return The user's database ID.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique database ID of the user.
     * This is typically used when retrieving a user from the database.
     * @param id The database ID to set.
     */
    public void setId(int id) {
        this.id = id;
    }

    // Consider overriding toString() for easier debugging.
    // Consider overriding equals() and hashCode() if User objects will be
    // compared or used in collections based on their content (e.g., email or ID).
}
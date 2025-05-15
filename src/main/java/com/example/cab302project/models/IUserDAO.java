package com.example.cab302project.models;

import java.sql.SQLException;
import java.util.List;

/**
 * Implementation of classes that interact with the persistence layer, Allows for CRUD operations
 */
public interface IUserDAO {

    /**
     * Adds a new user to the persistence layer.
     * Implementations should handle checking for duplicate users (e.g., by email or username)
     * before insertion.
     * @param user The {@link User} object containing the details of the user to add.
     */
    void addUser(User user);

    /**
     * Retrieves a user from the persistence layer based on their username.
     * @param username The username of the user to retrieve.
     * @return The {@link User} object matching the username, or null if no such user exists.
     */
    User getUserByUsername(String username);

    /**
     * Retrieves a user from the persistence layer based on their email address.
     * This method is marked as optional in the original comment, but defined in the interface.
     * @param email The email address of the user to retrieve.
     * @return The {@link User} object matching the email, or null if no such user exists.
     */
    User getUserByEmail(String email);

    /**
     * Validates a user's credentials for login purposes.
     * Checks if a user exists with the provided email and password combination.
     * @param email The email address of the user.
     * @param password The password provided by the user.
     * @return true if a user with the matching email and password exists, false otherwise.
     */
    boolean validateUser(String email, String password);

    /**
     * Deletes a user from the persistence layer based on their username.
     * This method is marked as optional in the original comment, but defined in the interface.
     * Implementations should consider cascading deletes for related data (e.g., friend requests, events).
     * @param username The username of the user to delete.
     */
    void deleteUser(String username);

    /**
     * Checks if a user exists in the persistence layer based on their email address.
     * @param email The email address to check for existence.
     * @return true if a user with the given email exists, false otherwise.
     */
    boolean userExists(String email);

    /**
     * Updates the email address for a user.
     * @param currentEmail The current email address of the user.
     * @param newEmail The new email address for the user.
     * @return true if the email was updated successfully, false otherwise
     */
    boolean updateEmail(String currentEmail, String newEmail);

    /**
     * Updates the password for a user.
     * @param email The email address of the user.
     * @param newPassword The new password for the user.
     * @return true if the password was updated successfully, false otherwise
     */
    boolean updatePassword(String email, String newPassword);

    /**
     * Sends a friend request from one user to another.
     * @param senderUsername The username of the user sending the request.
     * @param receiverUsername The username of the user receiving the request.
     * @return true if the friend request was successfully initiated, false otherwise
     */
    boolean sendFriendRequest(String senderUsername, String receiverUsername);

    /**
     * Accepts a pending friend request.
     * Updates the status of a friend request between two users.
     * @param senderUsername The username of the user who sent the request.
     * @param receiverUsername The username of the user who is accepting the request.
     * @return true if a pending request was found and accepted, false otherwise.
     */
    boolean acceptFriendRequest(String senderUsername, String receiverUsername);

    /**
     * Declines a pending friend request.
     * Updates the status of a friend request between two users.
     * @param senderUsername The username of the user who sent the request.
     * @param receiverUsername The username of the user who is declining the request.
     * @return true if a pending request was found and declined, false otherwise.
     */
    boolean declineFriendRequest(String senderUsername, String receiverUsername);

    /**
     * Updates the bio for a user.
     * @param email The email address of the user.
     * @param newBio The new bio text for the user.
     * @return true if the bio was updated successfully, false otherwise
     */
    boolean updateBio(String email, String newBio);

    /**
     * Updates the profile image for a user.
     * @param email The email address of the user.
     * @param imageData A byte array containing the image data. Can be null to remove the image.
     * @return true if the profile image was updated successfully, false otherwise
     */
    boolean updateProfileImage(String email, byte[] imageData);

    /**
     * Retrieves the profile image data for a user.
     * @param email The email address of the user.
     * @return A byte array containing the profile image data, or null if no image is set or the user is not found.
     */
    byte[] getProfileImage(String email);

    /**
     * Retrieves a list of users who have sent a pending friend request to the specified user.
     * @param username The username of the user checking for pending requests.
     * @return A List of User objects representing the senders of pending requests. Returns an empty list if no pending requests exist or the user is not found.
     */
    List<User> getPendingFriendRequests(String username);

    /**
     * Retrieves a list of friends (users linked by 'accepted' friend requests) for the specified user.
     * @param username The username of the user whose friends are being retrieved.
     * @return A List of User objects representing the user's friends. Returns an empty list if the user is not found or has no friends.
     * @throws SQLException If a database access error occurs during the operation.
     */
    List<User> getFriends(String username) throws SQLException;
}

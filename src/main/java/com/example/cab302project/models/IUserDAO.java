package com.example.cab302project.models;

import java.sql.SQLException;
import java.util.List;

public interface IUserDAO {

    // Adds a new user to the database
    void addUser(User user);

    // Retrieves a user by their username
    User getUserByUsername(String username);

    // Retrieves a user by their email (optional)
    User getUserByEmail(String email);

    // Validates a user’s credentials (for login)
    boolean validateUser(String email, String password);

    // Updates a user’s information (e.g., email, password)
    void updateUser(User user);

    // Deletes a user from the database (optional)
    void deleteUser(String username);

    boolean userExists(String email);

    boolean updateEmail(String currentEmail, String newEmail);

    boolean updatePassword(String email, String newPassword);

    // Send a friend request from one user to another
    boolean sendFriendRequest(String senderUsername, String receiverUsername);

    // Accept a pending friend request
    boolean acceptFriendRequest(String senderUsername, String receiverUsername);

    // Decline a pending friend request
    boolean declineFriendRequest(String senderUsername, String receiverUsername);

    boolean updateBio(String email, String newBio);

    boolean updateProfileImage(String email, byte[] imageData);

    byte[] getProfileImage(String email);

    // Get all pending friend requests for a user
    List<User> getPendingFriendRequests(String username);

    // Get all friends (accepted requests) of a user
    List<User> getFriends(String username) throws SQLException;
}

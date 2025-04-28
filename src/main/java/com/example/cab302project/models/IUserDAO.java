package com.example.cab302project.models;

import com.example.cab302project.models.User;

public interface IUserDAO {

    // Adds a new user to the database
    void addUser(User user);

    // Retrieves a user by their username
    User getUserByUsername(String username);

    // Retrieves a user by their email (optional)
    User getUserByEmail(String email);

    // Validates a user’s credentials (for login)
    boolean validateUser(String username, String password);

    // Updates a user’s information (e.g., email, password)
    void updateUser(User user);

    // Deletes a user from the database (optional)
    void deleteUser(String username);

    boolean userExists(String email);
}
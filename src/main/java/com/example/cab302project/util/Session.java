package com.example.cab302project.util;

import com.example.cab302project.models.User;

/**
 * Manages the currently logged-in user's session for the application.
 * This class uses a static field to hold the user, meaning there's a single
 * logged-in user across the entire application instance.
 */
public class Session {
    private static User loggedInUser;

    /**
     * Sets the current logged-in user
     * @param user represents the successfully logged-in user
     */
    public static void setLoggedInUser(User user) {
        loggedInUser = user;
    }

    /**
     * Retrieves the current logged-in user
     * @return User currently logged-in
     */
    public static User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Clears the current user's session. used for logout
     */
    public static void clear() {
        loggedInUser = null;
    }
}

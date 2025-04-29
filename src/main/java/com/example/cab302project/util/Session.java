// File: Session.java
package com.example.cab302project.util;

public class Session {
    private static String loggedInEmail;

    public static void setLoggedInEmail(String email) {
        loggedInEmail = email;
    }

    public static String getLoggedInEmail() {
        return loggedInEmail;
    }

    public static void clear() {
        loggedInEmail = null;
    }
}

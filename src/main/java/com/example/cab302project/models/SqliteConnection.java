package com.example.cab302project.models;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages a single instance of a SQLite database connection.
 * This ensures that only one database connection is created and used throughout the application,
 * providing a central point of access to the users database
 */
public class SqliteConnection {

    /**
     * Instance of the databases connection
     * Initialized upon the first call to getInstance().
     */
    private static Connection instance = null;

    /**
     * Constructor to prevent instantiation from outside the class.
     * Establishes the connection to the users database in SQLite database.
     * Errors during connection establishment are printed to standard error for debugging.
     */
    private SqliteConnection() {
        String url = "jdbc:sqlite:users.db";
        try {
            instance = DriverManager.getConnection(url);
        } catch (SQLException sqlEx) {
            System.err.println("Failed to connect to database: " + sqlEx.getMessage());
        }
    }

    /**
     * Returns the single instance of the database connection.
     * If the instance does not exist, it creates it by calling the private constructor.
     *
     * @return The singleton instance of the Connection to the database.
     */
    public static Connection getInstance() {
        if (instance == null) {
            new SqliteConnection(); // The constructor sets the instance
        }
        return instance;
    }

    /**
     * Injects a mock or test database connection for testing purposes.
     * This method is only for testing
     * to replace the real database connection with a test double.
     *
     * @param testConnection The test Connection object to inject.
     */
    public static void injectTestConnection(Connection testConnection) {
        instance = testConnection;
    }
}
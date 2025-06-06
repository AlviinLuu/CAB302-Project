package com.example.cab302project.models;

import java.sql.*;

public class SqliteUserDAO implements IUserDAO {

    private Connection connection;

    public SqliteUserDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
        insertSampleData();
    }

    // Create the users table if it doesn't exist
    private void createTable() {
        try {
            Statement statement = connection.createStatement();
            String query = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username VARCHAR NOT NULL UNIQUE,"
                    + "password VARCHAR NOT NULL,"
                    + "email VARCHAR NOT NULL UNIQUE"
                    + ")";
            statement.execute(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Insert some sample data (you can remove this if it's not needed)
    private void insertSampleData() {
        try {
            Statement clearStatement = connection.createStatement();
            String clearQuery = "DELETE FROM users";
            clearStatement.execute(clearQuery);

            Statement insertStatement = connection.createStatement();
            String insertQuery = "INSERT INTO users (username, password, email) VALUES "
                    + "('john', 'password123', 'john@example.com'),"
                    + "('jane', 'abc123', 'jane@example.com'),"
                    + "('jay', 'qwerty', 'jay@example.com')";
            insertStatement.execute(insertQuery);
        } catch (SQLException e) {
            e.printStackTrace(); 
        }
    }

    // Add a new user to the database
    @Override
    public void addUser(User user) {
        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try {
            // Disable auto-commit for transaction control
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getEmail());

                // Log user details to verify the data being passed
                System.out.println("Adding user: " + user.getUsername() + ", " + user.getEmail());

                stmt.executeUpdate();

                // Commit the transaction explicitly
                connection.commit();

                System.out.println("User added successfully!");
            } catch (SQLException e) {
                connection.rollback();  // Rollback in case of error
                System.out.println("Error during user insertion: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Re-enable auto-commit after the transaction
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Retrieve a user by username
    @Override
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Retrieve a user by email
    @Override
    public User getUserByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Validate if a user exists with the provided username and password
    @Override
    public boolean validateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            return rs.next(); // returns true if a match is found
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    // Update a user's information (password, email)
    @Override
    public void updateUser(User user) {
        String query = "UPDATE users SET password = ?, email = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getUsername());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Delete a user by their username
    @Override
    public void deleteUser(String username) {
        String query = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

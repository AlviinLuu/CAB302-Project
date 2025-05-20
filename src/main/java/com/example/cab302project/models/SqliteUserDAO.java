package com.example.cab302project.models;

import net.fortuna.ical4j.model.DateTime;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * SQlite implementation of IUserDAO
 * This class handles all operations and methods for users, friend request and events
 * Using sthe connection from SqliteConnection this maanges table creation and methods for user management
 * friend request handling, event storage and handling
 */
public class SqliteUserDAO implements IUserDAO {
    /**
     * Database connection
     */
    private Connection connection;

    /**
     * Grabs the Sqliteconnection and ensures the necessary tables are created
     */
    public SqliteUserDAO() {
        connection = SqliteConnection.getInstance();
        createTables();
    }

    /**
     * This creats the following tables with the following columns and their respective types
     */
    private void createTables() {
        try {
            Statement statement = connection.createStatement();

            String usersTableQuery = "CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "username TEXT NOT NULL UNIQUE,"
                    + "password TEXT NOT NULL,"
                    + "email TEXT NOT NULL UNIQUE,"
                    + "bio TEXT DEFAULT '',"
                    + "profile_image BLOB"
                    + ")";
            statement.execute(usersTableQuery);

            String friendRequestsTableQuery = "CREATE TABLE IF NOT EXISTS friend_requests ("
                    + "sender_email TEXT NOT NULL,"
                    + "receiver_email TEXT NOT NULL,"
                    + "status TEXT NOT NULL,"
                    + "PRIMARY KEY (sender_email, receiver_email),"
                    + "FOREIGN KEY (sender_email) REFERENCES users (email),"
                    + "FOREIGN KEY (receiver_email) REFERENCES users (email)"
                    + ")";
            statement.execute(friendRequestsTableQuery);

            // Create events table if not exists
            String eventsTableQuery = "CREATE TABLE IF NOT EXISTS events ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "user_id INTEGER NOT NULL,"
                    + "user_email TEXT NOT NULL,"
                    + "name TEXT NOT NULL,"
                    + "start_time TEXT NOT NULL,"
                    + "end_time TEXT NOT NULL,"
                    + "FOREIGN KEY (user_id) REFERENCES users(id)"

                    + ")";
            statement.execute(eventsTableQuery);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds user to the database, checks if the email already exists.
     * If the bio is initally empty sets it to an empty string.
     * @param user The respective user object containing its respective details (username, password, email, bio).
     */
    @Override
    public void addUser(User user) {
        if (userExists(user.getEmail())) {
            System.out.println("Email already exists.");
            return;
        }

        if (getUserByUsername(user.getUsername()) != null) {
            System.out.println("Username already exists.");
            return;
        }

        if (user.getBio() == null) {
            user.setBio(""); // Set bio to an empty string if it's null
        }

        String query = "INSERT INTO users (username, password, email, bio) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getBio());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the user based off the specified column
     * @param column Name of column within db to search
     * @param value value to match the respective column
     * @return The user object if it can be found else return null
     */
    private User getUserByColumn(String column, String value) {
        String query = "SELECT * FROM users WHERE " + column + " = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, value);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("email")
                );
                user.setBio(rs.getString("bio"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a user from the database by their username.
     * @param username The username of the user to retrieve.
     * @return The User object if found, otherwise null.
     */
    @Override
    public User getUserByUsername(String username) {
        return getUserByColumn("username", username);
    }

    /**
     * Retrieves a user from the database by their email.
     * @param email The username of the user to retrieve.
     * @return The User object if found, otherwise null.
     */
    @Override
    public User getUserByEmail(String email) {
        return getUserByColumn("email", email);
    }

    /**
     * Checks if user exists with its given email and password
     * @param email email address of the user
     * @param password password of the user
     * @return if matching user is found return true, false otherwise
     */
    @Override
    public boolean validateUser(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Checks if user exist based off email
     * @param email Enmail address to check
     * @return true if user exist with given email, false otherwise
     */
    @Override
    public boolean userExists(String email) {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Deletes user based off username
     * @param username The username of the target user
     */
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

    /**
     * Updates profile image of the user keyed by their email
     * @param email email address of user
     * @param imageData profile image data in byter array format
     * @return true if updata was successful, else false
     */
    @Override
    public boolean updateProfileImage(String email, byte[] imageData) {
        String query = "UPDATE users SET profile_image = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setBytes(1, imageData);  // Store the byte array in the database
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the profile image of the user keyed by their email
     * @param email Email of the target user
     * @return The image data of the users profile in byte array format
     */
    @Override
    public byte[] getProfileImage(String email) {
        String query = "SELECT profile_image FROM users WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBytes("profile_image");  // Return the byte array
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;  // No image found
    }

    /**
     * Updates the email with a new email
     * @param currentEmail Current users email
     * @param newEmail The new email the User wants to change it to
     * @return True if successful, false otherwise
     */
    @Override
    public boolean updateEmail(String currentEmail, String newEmail) {
        String sql = "UPDATE users SET email = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newEmail);
            stmt.setString(2, currentEmail);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Updates users password to a new password
     * @param email Current users email
     * @param newPassword Users new password
     * @return true if successful, else false
     */
    @Override
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Method to send frined request to other users
     * @param senderUsername Username of user sending request
     * @param receiverUsername Username of user recieving request
     * @return True if sent successfully, else false
     */
    @Override
    public boolean sendFriendRequest(String senderUsername, String receiverUsername) {
        User sender = getUserByUsername(senderUsername);
        User receiver = getUserByUsername(receiverUsername);

        if (sender != null && receiver != null) {
            if (isFriendRequestPending(sender.getEmail(), receiver.getEmail())) {
                System.out.println("Friend request already sent.");
                return false;
            }
            String query = "INSERT INTO friend_requests (sender_email, receiver_email, status) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, sender.getEmail());
                stmt.setString(2, receiver.getEmail());
                stmt.setString(3, "pending");
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Checks if a friend request is pending between 2 users
     * @param senderEmail Email of yser sending request
     * @param receiverEmail Email of user receiving request
     * @return True if pending exist else false
     */
    private boolean isFriendRequestPending(String senderEmail, String receiverEmail) {
        String query = "SELECT COUNT(*) FROM friend_requests WHERE sender_email = ? AND receiver_email = ? AND status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, senderEmail);
            stmt.setString(2, receiverEmail);
            stmt.setString(3, "pending");
            ResultSet rs = stmt.executeQuery();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Accepts friend request between 2 users
     * @param senderUsername Username of sender
     * @param receiverUsername Username receiver
     * @return True if successful else false
     */
    @Override
    public boolean acceptFriendRequest(String senderUsername, String receiverUsername) {
        User sender = getUserByUsername(senderUsername);
        User receiver = getUserByUsername(receiverUsername);

        if (sender != null && receiver != null) {
            String query = "UPDATE friend_requests SET status = ? WHERE sender_email = ? AND receiver_email = ? AND status = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, "accepted");
                stmt.setString(2, sender.getEmail());
                stmt.setString(3, receiver.getEmail());
                stmt.setString(4, "pending");
                int rowsUpdated = stmt.executeUpdate();
                return rowsUpdated > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Declines friend request between 2 users
     * @param senderUsername Username of sender
     * @param receiverUsername Username receiver
     * @return True if successful else false
     */
    @Override
    public boolean declineFriendRequest(String senderUsername, String receiverUsername) {
        User sender = getUserByUsername(senderUsername);
        User receiver = getUserByUsername(receiverUsername);

        if (sender != null && receiver != null) {
            String query = "UPDATE friend_requests SET status = ? WHERE sender_email = ? AND receiver_email = ? AND status = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, "declined");
                stmt.setString(2, sender.getEmail());
                stmt.setString(3, receiver.getEmail());
                stmt.setString(4, "pending");
                int rowsUpdated = stmt.executeUpdate();
                return rowsUpdated > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Updates users bio
     * @param email Email of user whose bio is being changed
     * @param newBio The new users bio
     * @return True if successful else false
     */
    @Override
    public boolean updateBio(String email, String newBio) {
        String query = "UPDATE users SET bio = ? WHERE email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newBio);
            stmt.setString(2, email);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Grabs list of pending friend request for a user
     * @param username Username of the user checking their request
     * @return List of User objects who sent pending request, returns and empty list if no user found or no pending request
     */
    @Override
    public List<User> getPendingFriendRequests(String username) {
        User user = getUserByUsername(username);
        List<User> pendingRequests = new ArrayList<>();
        if (user != null) {
            String query = "SELECT u.username, u.password, u.email, u.bio FROM users u "
                    + "JOIN friend_requests fr ON u.email = fr.sender_email "
                    + "WHERE fr.receiver_email = ? AND fr.status = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, user.getEmail());
                stmt.setString(2, "pending");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    User requester = new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                    requester.setBio(rs.getString("bio"));
                    pendingRequests.add(requester);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return pendingRequests;
    }

    /**
     * Retrieves list of friends who have accepted the pending request for the specified user
     * @param username Username of user whose friends are being retrieved
     * @return List of user objects representing user friends. Returns an empty list if they have no friends
     * @throws SQLException SQLException if a database error occurs
     */
    @Override
    public List<User> getFriends(String username) throws SQLException {
        User user = getUserByUsername(username);
        List<User> friends = new ArrayList<>();
        if (user != null) {
            String query =
                    "SELECT u.username, u.password, u.email, u.bio " +
                            "FROM users u " +
                            "JOIN friend_requests fr " +
                            "  ON (u.email = fr.sender_email OR u.email = fr.receiver_email) " +
                            "WHERE (fr.sender_email = ? OR fr.receiver_email = ?) " +
                            "  AND fr.status = ? " +
                            "  AND u.username <> ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getEmail());
                stmt.setString(3, "accepted");
                stmt.setString(4, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        User friend = new User(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        );
                        friend.setBio(rs.getString("bio"));
                        friends.add(friend);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return friends;
    }

    /**
     * Inserts new event into the database
     * @param userId ID of the user whose event this is
     * @param user_email Email of user whose event this is
     * @param name Name of the event
     * @param start_time start time of the event
     * @param end_time end time of the event
     */
    public void insertEvent(int userId, String user_email, String name, String start_time, String end_time) {
        String query = "INSERT INTO events (user_id, user_email, name, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, user_email);
            stmt.setString(3, name);
            stmt.setString(4, start_time);
            stmt.setString(5, end_time);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes events matching a particular user email
     * @param userEmail the email to delete events from
     */
    public void clearEventsByEmail(String userEmail) {
        String query = "DELETE FROM events WHERE user_email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userEmail);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    /**
     * Executes a "one-way" SQL statement (no return value)
     * @param query query to be executed
     */
    public void executeQuery(String query) {
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves list of events for a specific user on given data
     * @param email email of the specific user whose events are being grabbed
     * @param date The LocalDate to retrieve events.
     * @return List of events fpr specified user and data. Returns an empty list if no events are found or if error occurs
     */
    public List<Event> getUserEventsByEmailAndDate(String email, LocalDate date) {
        List<Event> events = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        String dateString = date.format(formatter);

        String query = "SELECT e.name, e.start_time, e.end_time, u.username " +
                "FROM events e JOIN users u ON e.user_email = u.email " +
                "WHERE e.user_email = ? AND e.start_time LIKE ? || '%'";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, email);
            stmt.setString(2, dateString);
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String startTime = resultSet.getString("start_time");
                String endTime = resultSet.getString("end_time");
                String username = resultSet.getString("username");

                Event event = new Event(name, startTime, endTime, username);
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving events by email and date: " + e.getMessage());
        }

        return events;
    }

}


package com.example.cab302project.models;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class SqliteUserDAO implements IUserDAO {

    private Connection connection;

    public SqliteUserDAO() {
        connection = SqliteConnection.getInstance();
        createTables();
    }

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

    @Override
    public User getUserByUsername(String username) {
        return getUserByColumn("username", username);
    }

    @Override
    public User getUserByEmail(String email) {
        return getUserByColumn("email", email);
    }

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

    @Override
    public void updateUser(User user) {
        String query = "UPDATE users SET password = ?, email = ?, bio = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getBio());
            stmt.setString(4, user.getUsername());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    public void clearEventsByEmail(String userEmail) {
        String query = "DELETE FROM events WHERE user_email = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, userEmail);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void clearAllEvents() {
        String query = "DELETE FROM events";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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

    public List<Event> getAllEvents() {
        List<Event> events = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        String query = "SELECT * " +
                "FROM events";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet resultSet = stmt.executeQuery();

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String startTime = resultSet.getString("start_time");
                String endTime = resultSet.getString("end_time");

                Event event = new Event(name, startTime, endTime, "username");
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all events: " + e.getMessage());
        }

        return events;
    }
}


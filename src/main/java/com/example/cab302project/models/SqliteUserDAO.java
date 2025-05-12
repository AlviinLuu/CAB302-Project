package com.example.cab302project.models;

import java.sql.*;
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
                    + "email TEXT NOT NULL UNIQUE"
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

        String query = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
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

            return rs.next(); // returns true if a match is found
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

            // If count is greater than 0, the user exists
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

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

    @Override
    public void deleteUser(String username) {
        // Deleting a user may fail if they have related friend requests, consider handling that
        String query = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    public List<User> getPendingFriendRequests(String username) {
        User user = getUserByUsername(username);
        List<User> pendingRequests = new ArrayList<>();
        if (user != null) {
            String query = "SELECT u.username, u.password, u.email FROM users u "
                    + "JOIN friend_requests fr ON u.email = fr.sender_email "
                    + "WHERE fr.receiver_email = ? AND fr.status = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, user.getEmail());
                stmt.setString(2, "pending");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    pendingRequests.add(new User(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    ));
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
            // Added "AND u.username <> ?" to exclude the user themself
            String query =
                    "SELECT u.username, u.password, u.email " +
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
                stmt.setString(4, username);            // ← filter out self here

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        friends.add(new User(
                                rs.getString("username"),
                                rs.getString("password"),
                                rs.getString("email")
                        ));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return friends;
    }
}

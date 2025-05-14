package com.example.cab302project.controllers;

import com.example.cab302project.models.SqliteConnection;
import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.util.Session;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FriendsController {

    @FXML private VBox mainContent;
    @FXML private TextArea aiPromptField;
    @FXML private Label aiResponseLabel;
    @FXML private Label profileHeaderLabel;
    @FXML private ImageView profileImage;
    @FXML private Label usernameLabel;
    @FXML private Label nameLabel;
    @FXML private Label bioLabel;
    @FXML private GridPane miniDayView;
    @FXML private ImageView logoImage;

    @FXML private ListView<String> searchResultsList;
    @FXML private TextField searchUserField;

    @FXML private ListView<String> pendingRequestsList;   // outgoing
    @FXML private ListView<String> incomingRequestsList;  // incoming

    @FXML private ComboBox<String> friendSelector;

    private ObservableList<String> allUsers        = FXCollections.observableArrayList();
    private ObservableList<String> pendingOutgoing = FXCollections.observableArrayList();
    private ObservableList<String> incomingReqs    = FXCollections.observableArrayList();
    private ObservableList<String> friendList      = FXCollections.observableArrayList();

    private LocalDate currentDate = LocalDate.now();
    private Connection connection;
    private SqliteUserDAO userDAO;

    @FXML
    public void initialize() {
        // 1) Ensure session has a user
        User sessionUser = Session.getLoggedInUser();
        if (sessionUser == null) {
            System.err.println("ERROR: No user in session! Call Session.setLoggedInUser(...) after login.");
            return;
        }

        // 2) DB & DAO
        connection = SqliteConnection.getInstance();
        userDAO    = new SqliteUserDAO();

        // 3) Load logo
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);

        // 4) Load data from DB
        loadAllUsers();
        refreshFriendList();
        refreshIncomingRequests();
        refreshOutgoingRequests();

        // 5) Wire UI list views
        friendSelector.setItems(friendList);
        // if nothing in list show message
        Label placeholderLabel2 = new Label("Select a user");
        placeholderLabel2.setStyle("-fx-text-fill: #6A4B8B; -fx-font-style: italic;");
        searchResultsList.setPlaceholder(placeholderLabel2);

        pendingRequestsList.setItems(pendingOutgoing);
        // if nothing in list show message
        Label placeholderLabel = new Label("No pending requests");
        placeholderLabel.setStyle("-fx-text-fill: #6A4B8B; -fx-font-style: italic;");
        pendingRequestsList.setPlaceholder(placeholderLabel);

        incomingRequestsList.setItems(incomingReqs);
        // if nothing in list show message
        Label placeholderLabel1 = new Label("No incoming requests");
        placeholderLabel1.setStyle("-fx-text-fill: #6A4B8B; -fx-font-style: italic;");
        incomingRequestsList.setPlaceholder(placeholderLabel1);

        // 6) Show profile on selection
        friendSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldU, newU) -> {
            if (newU != null) loadUserProfile(newU);
        });

        // 7) Live search logic (always set a fresh list)
        searchUserField.textProperty().addListener((obs, oldText, text) -> {
            if (text == null || text.isBlank()) {
                searchResultsList.setItems(FXCollections.observableArrayList());
            } else {
                List<String> matches = allUsers.stream()
                        .filter(u -> u.toLowerCase().contains(text.toLowerCase()))
                        .collect(Collectors.toList());
                searchResultsList.setItems(FXCollections.observableArrayList(matches));
            }
        });

        // 8) Render the mini-day view
        renderMiniDayView();
    }

    // ─── Data Loaders ──────────────────────────────────────────────────

    private void loadAllUsers() {
        allUsers.clear();
        String me = Session.getLoggedInUser().getUsername();

        String sql = "SELECT username FROM users";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                String u = rs.getString("username");
                if (!u.equals(me)) {
                    allUsers.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshFriendList() {
        try {
            friendList.setAll(
                    userDAO.getFriends(Session.getLoggedInUser().getUsername())
                            .stream()
                            .map(User::getUsername)
                            .collect(Collectors.toList())
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void refreshIncomingRequests() {
        incomingReqs.setAll(
                userDAO.getPendingFriendRequests(Session.getLoggedInUser().getUsername())
                        .stream()
                        .map(User::getUsername)
                        .collect(Collectors.toList())
        );
    }

    private void refreshOutgoingRequests() {
        pendingOutgoing.clear();
        String sql = """
            SELECT receiver_email
              FROM friend_requests
             WHERE sender_email = ?
               AND status = 'pending'
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, Session.getLoggedInUser().getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String username = rs.getString("receiver_email");
                    pendingOutgoing.add(username);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUserProfile(String username) {
        User u = userDAO.getUserByUsername(username);

        if (u != null) {
            profileHeaderLabel.setText(username + "'s Profile");
            usernameLabel.setText("@" + u.getUsername());
            nameLabel.setText(u.getEmail());
            bioLabel.setText(u.getBio());

            // Check if user has a profile image
            byte[] profileImageData = userDAO.getProfileImage(u.getEmail()); // Get the image data from the database

            if (profileImageData != null && profileImageData.length > 0) {
                // If user has a profile image, load it from the byte array
                Image image = new Image(new ByteArrayInputStream(profileImageData));
                profileImage.setImage(image);
            } else {
                // If no profile image, load the default
                InputStream is = getClass().getResourceAsStream("/images/default_profile.png");
                assert is != null;
                profileImage.setImage(new Image(is));
            }
        }
    }


    // ─── Button Handlers ────────────────────────────────────────────────

    @FXML
    private void handleRemoveFriend() {
        String selectedFriend = friendSelector.getSelectionModel().getSelectedItem();
        if (selectedFriend != null && friendList.contains(selectedFriend)) {
            friendList.remove(selectedFriend);
            friendSelector.getSelectionModel().clearSelection();

            // Clear profile display
            profileHeaderLabel.setText("Friend profile");
            usernameLabel.setText("");
            nameLabel.setText("");
            bioLabel.setText("");
            profileImage.setImage(null);  // Optional: set to a default image instead

            showAlert("Friend Removed", selectedFriend + " has been removed from your friend list.");
        } else {
            showAlert("No Selection", "Please select a friend to remove.");
        }
    }

    @FXML private void handleSendRequest() {
        String selected = searchResultsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection","Please select a user to send request.");
            return;
        }
        String me = Session.getLoggedInUser().getUsername();
        if (friendList.contains(selected)) {
            showAlert("Already Friends", selected + " is already your friend.");
        } else if (pendingOutgoing.contains(selected)) {
            showAlert("Already Requested","You already sent a request to " + selected + ".");
        } else {
            boolean ok = userDAO.sendFriendRequest(me, selected);
            if (ok) {
                showAlert("Request Sent","Friend request sent to " + selected + ".");
                refreshOutgoingRequests();
            } else {
                showAlert("Error","Could not send request. Ensure user exists and no pending request.");
            }
        }
        searchUserField.clear();
        searchResultsList.setItems(FXCollections.observableArrayList());
    }

    @FXML private void handleAcceptRequest() {
        String sel = incomingRequestsList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("No Selection","Please select a request to accept.");
            return;
        }
        String me = Session.getLoggedInUser().getUsername();
        if (userDAO.acceptFriendRequest(sel, me)) {
            showAlert("Accepted", sel + " is now your friend!");
            refreshIncomingRequests();
            refreshFriendList();
        } else {
            showAlert("Error","Could not accept. Try again.");
        }
    }

    @FXML private void handleDeclineRequest() {
        String sel = incomingRequestsList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("No Selection","Please select a request to decline.");
            return;
        }
        String me = Session.getLoggedInUser().getUsername();
        if (userDAO.declineFriendRequest(sel, me)) {
            showAlert("Declined", sel + " has been declined.");
            refreshIncomingRequests();
        } else {
            showAlert("Error","Could not decline. Try again.");
        }
    }

    @FXML private void handleDeleteRequest() {
        String sel = pendingRequestsList.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert("No Selection","Please select a request to delete.");
            return;
        }
        String sql = """
            DELETE FROM friend_requests
             WHERE sender_email   = ?
               AND receiver_email = ?
        """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, Session.getLoggedInUser().getUsername());
            ps.setString(2, sel);
            ps.executeUpdate();
            showAlert("Deleted","Request to " + sel + " removed.");
            refreshOutgoingRequests();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─── Navigation & AI ───────────────────────────────────────────────

    @FXML private void goToHome() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/example/cab302project/calendar-view.fxml"));
            Stage stage = (Stage)mainContent.getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void openSettingsPage() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/cab302project/settings-view.fxml")));
            Stage stage = (Stage)mainContent.getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML private void openProfilePage() {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/cab302project/profile-view.fxml")));
            Stage stage = (Stage)mainContent.getScene().getWindow();
            stage.close();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.setMaximized(true);
            newStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAIPrompt() {
        String prompt = aiPromptField.getText().trim();
        if (prompt.isEmpty()) {
            return;
        }

        // Clear input field right away
        aiPromptField.clear();

        // Show user's prompt above the response
        aiResponseLabel.setText("You: " + prompt + "\n\nAI: ...");

        // Call Ollama and get the response
        String response = callOllama(prompt);

        if (response != null && !response.isEmpty()) {
            // Unescape and clean AI response
            response = response.replace("\\u003c", "<").replace("\\u003e", ">");
            response = response.replaceAll("(?i)<[^>]*>", "").strip();

            // Start typing animation to gradually replace "AI: ..." with actual response
            playTypingAnimation(aiResponseLabel, "You: " + prompt + "\n\nAI: " + response);
        } else {
            aiResponseLabel.setText("You: " + prompt + "\n\nAI: (No response)");
        }
    }


    private String callOllama(String prompt) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String jsonPayload = String.format(
                    "{\"model\":\"deepseek-r1\", \"stream\": false, \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}]}",
                    prompt.replace("\"", "\\\"")
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:11434/api/chat"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            String body = response.body();

            // Match content using regex instead of brittle substring parsing
            String content = extractContentFromJson(body);
            if (content != null) {
                content = content.replace("\\n", "\n").replace("\\\"", "\"");

                // Remove leading "Think" phrases if present
                if (content.toLowerCase().startsWith("think")) {
                    int dot = content.indexOf('.');
                    if (dot != -1 && dot < 15) {
                        content = content.substring(dot + 1).trim();
                    }
                }

                return content;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractContentFromJson(String json) {
        String marker = "\"content\":\"";
        int index = json.indexOf(marker);
        if (index != -1) {
            int start = index + marker.length();
            int end = json.indexOf("\"", start);
            // keep extending end index while it ends in a backslash (escaped quote)
            while (end != -1 && json.charAt(end - 1) == '\\') {
                end = json.indexOf("\"", end + 1);
            }
            if (end != -1) {
                return json.substring(start, end);
            }
        }
        return null;
    }


    private void playTypingAnimation(Label label, String text) {
        label.setText("");

        Thread typingThread = new Thread(() -> {
            try {
                for (int i = 0; i < text.length(); i++) {
                    final int index = i;
                    Platform.runLater(() -> label.setText(text.substring(0, index + 1)));
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        typingThread.start();
    }

    // ─── Mini Day View ────────────────────────────────────────────────
    private void renderMiniDayView() {
        miniDayView.getChildren().clear();
        miniDayView.getRowConstraints().clear();

        // Top date header
        Label header = new Label(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 32px; -fx-text-fill: #2e014f;");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.TOP_LEFT);
        miniDayView.add(header, 0, 0, 2, 1);

        // Spacer
        RowConstraints spacer = new RowConstraints(30);
        miniDayView.getRowConstraints().add(spacer);

        // Hour slots
        for (int hour = 0; hour < 24; hour++) {
            miniDayView.getRowConstraints().add(new RowConstraints(30));

            Label time = new Label(String.format("%02d:00", hour));
            time.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
            time.setMaxWidth(Double.MAX_VALUE);
            time.setAlignment(Pos.CENTER_RIGHT);

            Label event = new Label();
            event.setStyle("-fx-border-color: #bbb; -fx-border-width: 0 0 1px 0;");
            event.setMaxWidth(Double.MAX_VALUE);
            event.setAlignment(Pos.CENTER_LEFT);

            miniDayView.add(time, 0, hour + 1);
            miniDayView.add(event, 1, hour + 1);
            GridPane.setHgrow(event, Priority.ALWAYS);
        }
    }

//    private void renderMiniDayView() {
//        miniDayView.getChildren().clear();
//        miniDayView.getRowConstraints().clear();
//
//        Label dateLbl = new Label(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
//        dateLbl.setStyle("-fx-font-weight:bold; -fx-font-size:50px; -fx-text-fill:#333;");
//        dateLbl.setMaxWidth(Double.MAX_VALUE);
//        dateLbl.setAlignment(Pos.TOP_LEFT);
//        miniDayView.add(dateLbl, 0, 0, 2, 1);
//
//        RowConstraints spacer = new RowConstraints();
//        spacer.setVgrow(Priority.ALWAYS);
//        miniDayView.getRowConstraints().add(spacer);
//    }

    // ─── Helper ────────────────────────────────────────────────────────

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

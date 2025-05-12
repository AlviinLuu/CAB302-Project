package com.example.cab302project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class FriendsController {
    private final ToggleGroup friendsToggleGroup = new ToggleGroup();

    @FXML
    private javafx.scene.layout.VBox mainContent;
    @FXML
    private TextArea aiPromptField;
    @FXML
    private Label aiResponseLabel;
    @FXML
    private Label profileHeaderLabel;
    @FXML
    private Label profileContentLabel;
    @FXML
    private GridPane miniDayView;

    private LocalDate currentDate = LocalDate.now();

    @FXML private ListView<String> searchResultsList;
    @FXML private TextField searchUserField;


    @FXML private ListView<String> pendingRequestsList;
    private ObservableList<String> allUsers = FXCollections.observableArrayList(
            "Username1", "Username2", "Username3", "Harpi", "Simran", "Alex", "Jas" // mock data update using sql once set up
    );

    private ObservableList<String> pendingRequests = FXCollections.observableArrayList();

    @FXML private ListView<String> incomingRequestsList;
    private ObservableList<String> incomingRequests = FXCollections.observableArrayList(
            "Papi", "Jordan", "Liam" // mock data update using sql once set up
    );

    @FXML private ComboBox<String> friendSelector;
    private ObservableList<String> friendList = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        // Populate the ComboBox with some mock usernames (replace with actual logic later)
        friendSelector.getItems().addAll("Username1", "Username2", "Username3");
        friendSelector.setItems(friendList);
        friendList.addAll("Username1", "Username2"); // Pre-existing friends


        // Listen for selection changes
        friendSelector.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                profileHeaderLabel.setText(newVal + "'s Profile");
                profileContentLabel.setText("Details about " + newVal + " will be shown here.");
            }
        });

        for (int i = 0; i < 24; i++) {
            Label hourLabel = new Label(String.format("%02d:00", i));
            hourLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #1A1A1A;");
            miniDayView.add(hourLabel, 0, i);
        }
        renderMiniDayView();

        searchResultsList.setItems(FXCollections.observableArrayList()); // initially empty

        // Live search
        searchUserField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isBlank()) {
                searchResultsList.getItems().clear();
            } else {
                searchResultsList.setItems(
                        allUsers.filtered(user -> user.toLowerCase().contains(newVal.toLowerCase()))
                );
            }
        });

        // requests sent to a user
        incomingRequestsList.setItems(incomingRequests);

        // Add selected user to pending requests
        pendingRequestsList.setItems(pendingRequests);
    }

    @FXML
    private void handleAcceptRequest() {
        String selected = incomingRequestsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            incomingRequests.remove(selected);
            friendList.add(selected); // Add to friend list
            showAlert("Friend Request Accepted", selected + " is now your friend!");
        } else {
            showAlert("No Selection", "Please select a request to accept.");
        }
    }


    @FXML
    private void handleDeclineRequest() {
        String selected = incomingRequestsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            incomingRequests.remove(selected);
            // TODO: Add logic to remove/ignore this request in DB
            showAlert("Friend Request Declined", selected + " has been declined.");
        } else {
            showAlert("No Selection", "Please select a request to decline.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleSendRequest() {
        String selectedUser = searchResultsList.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            // Your logic to send the request
            System.out.println("Request sent to: " + selectedUser);
            // Optionally add to pending list
            pendingRequestsList.getItems().add(selectedUser);
        } else {
            System.out.println("No user selected.");
        }
    }

    @FXML
    private void handleDeleteRequest() {
        String selected = pendingRequestsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            pendingRequests.remove(selected);
            showAlert("Deleted", "Request to " + selected + " removed.");
        } else {
            showAlert("No Selection", "Please select a request to delete.");
        }
    }


//    Navigation buttons between pages
    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/calendar-view.fxml"));
            Parent homeRoot = loader.load();

            Stage homeStage = new Stage();
            homeStage.setTitle("Smart Schedule Assistant");

            Scene scene = new Scene(homeRoot);
            homeStage.setScene(scene);

            //  Set it to full screen
            homeStage.setMaximized(true); // Maximize the window

            homeStage.show();

            //  Close the current Settings page
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openSettingsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/settings-view.fxml"));
            Parent settingsRoot = loader.load();

            Stage settingsStage = new Stage();
            settingsStage.setTitle("Settings");

            Scene scene = new Scene(settingsRoot);
            settingsStage.setScene(scene);

            // Set it to full screen
            settingsStage.setMaximized(true);

            // Close the current Calendar window
            Stage currentStage = (Stage) mainContent.getScene().getWindow();  // mainContent is your root VBox
            currentStage.close();

            settingsStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAIPrompt() {
        String prompt = aiPromptField.getText().trim();
        if (!prompt.isEmpty()) {
            String response = generateResponse(prompt);
            playTypingAnimation(aiResponseLabel, response);
        }
    }

    private String generateResponse(String userInput) {
        return "You said: \"" + userInput + "\" — AI will respond to this once implemented!!!";
    }

//     Just a fun animation effect which shows AI typing while it generates a response
    private void playTypingAnimation(Label label, String fullText) {
        final int[] charIndex = {0};

      javafx.animation.Timeline timeline = new javafx.animation.Timeline(
            new javafx.animation.KeyFrame(
                  javafx.util.Duration.millis(40), // Speed of typing
                event -> {
                  if (charIndex[0] < fullText.length()) {
                    label.setText(fullText.substring(0, charIndex[0] + 1));
                  charIndex[0]++;
            }
      }
    )
    );
    timeline.setCycleCount(fullText.length());
    timeline.play();
    }

    private void renderMiniDayView() {
        miniDayView.getChildren().clear();
        miniDayView.getRowConstraints().clear();

        // Add a label for the current day and month (no year)
        Label dateLabel = new Label(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
        dateLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 50px; -fx-text-fill: #333; ");
        dateLabel.setMaxWidth(Double.MAX_VALUE);
        dateLabel.setAlignment(Pos.TOP_LEFT);
        miniDayView.add(dateLabel, 0, 0, 2, 1); // Span across 2 columns

        // Add a small spacer after the date label
        RowConstraints spacer = new RowConstraints();
        spacer.setMinHeight(40);
        miniDayView.getRowConstraints().add(spacer);

        // Add RowConstraints for each hour
        for (int hour = 0; hour < 24; hour++) {
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setMinHeight(30); // Smaller because it's a mini version
            miniDayView.getRowConstraints().add(rowConstraints);

            // Create time label (hour on the left)
            Label timeLabel = new Label(String.format("%02d:00", hour));
            timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #666;");
            timeLabel.setMaxWidth(Double.MAX_VALUE);
            timeLabel.setAlignment(Pos.CENTER_RIGHT);

            // Create event slot (on the right side)
            Label eventSlot = new Label();
            eventSlot.setStyle("-fx-border-color: #ccc; -fx-border-width: 0 0 1px 0;");
            eventSlot.setMaxWidth(Double.MAX_VALUE);
            eventSlot.setAlignment(Pos.CENTER_LEFT);

            miniDayView.add(timeLabel, 0, hour + 1);
            miniDayView.add(eventSlot, 1, hour + 1);
            GridPane.setHgrow(eventSlot, Priority.ALWAYS);
        }
    }

}

package com.example.cab302project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

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
    private ComboBox<String> friendSelector;
    @FXML
    private GridPane miniDayView;

    private LocalDate currentDate = LocalDate.now();



    @FXML
    public void initialize() {
        // Populate the ComboBox with some mock usernames (replace with actual logic later)
        friendSelector.getItems().addAll("Username1", "Username2", "Username3");

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

    }

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



    // Example simple AI response ****NEED TO BE REPLACED WHEN AI SET UP*******
//    private String generateResponse(String userInput) {
//       return "You said \"" + userInput;
//    }

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

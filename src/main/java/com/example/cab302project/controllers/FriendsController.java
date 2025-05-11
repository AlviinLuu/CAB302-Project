package com.example.cab302project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;



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
    private RadioButton user1Radio, user2Radio, user3Radio;


    @FXML
    public void initialize() {
        // Assign the toggle group in code
        user1Radio.setToggleGroup(friendsToggleGroup);
        user2Radio.setToggleGroup(friendsToggleGroup);
        user3Radio.setToggleGroup(friendsToggleGroup);

        friendsToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                RadioButton selectedRadio = (RadioButton) newToggle;
                String username = selectedRadio.getText();
                profileHeaderLabel.setText(username + "'s Profile");
                profileContentLabel.setText("Details about " + username + " will be shown here.");
            }
        });
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
}

package com.example.cab302project.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FriendsController {
    @FXML
    private javafx.scene.layout.VBox mainContent;

    @FXML
    public void initialize() {
        // Any initialization logic here
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
}

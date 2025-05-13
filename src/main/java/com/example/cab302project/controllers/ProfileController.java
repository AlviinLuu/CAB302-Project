package com.example.cab302project.controllers;

import com.example.cab302project.models.User;
import com.example.cab302project.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ProfileController {

    @FXML private Label profileNameLabel;
    @FXML private TextArea bioTextArea;
    @FXML private ImageView profileImageView;
    @FXML private GridPane miniDayView;
    @FXML private VBox mainContent;
    @FXML private ImageView logoImage;

    private final LocalDate currentDate = LocalDate.now();

    @FXML
    private void initialize() {
        User user = Session.getLoggedInUser();

        if (user != null) {
            profileNameLabel.setText(user.getUsername() != null ? user.getUsername() : "User");
            bioTextArea.setText(user.getBio() != null ? user.getBio() : "No bio provided.");
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                profileImageView.setImage(new Image(user.getProfileImage()));
            } else {
                setDefaultProfileImage();
            }
        } else {
            profileNameLabel.setText("User");
            bioTextArea.setText("No user logged in.");
            setDefaultProfileImage();
        }

        bioTextArea.setEditable(false);
        bioTextArea.setFocusTraversable(false);
        bioTextArea.setStyle("-fx-opacity: 1; -fx-background-color: #ECECFF; -fx-text-fill: black;");

        renderMiniDayView();
    }

    private void setDefaultProfileImage() {
        Image placeholder = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
        profileImageView.setImage(placeholder);
    }

    private void renderMiniDayView() {
        miniDayView.getChildren().clear();
        miniDayView.getRowConstraints().clear();

        Label header = new Label(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
        header.setStyle("-fx-font-weight: bold; -fx-font-size: 32px; -fx-text-fill: #2e014f;");
        header.setMaxWidth(Double.MAX_VALUE);
        header.setAlignment(Pos.TOP_LEFT);
        miniDayView.add(header, 0, 0, 2, 1);

        for (int hour = 0; hour < 24; hour++) {
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

    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(profileImageView.getScene().getWindow());

        if (selectedFile != null) {
            String imagePath = selectedFile.toURI().toString();
            profileImageView.setImage(new Image(imagePath));
            User user = Session.getLoggedInUser();
            if (user != null) {
                user.setProfileImage(imagePath);
            }
        }
    }

    @FXML
    private void openSettingsPage() {
        openPage("/com/example/cab302project/settings-view.fxml", "Settings");
    }

    @FXML
    private void goToHome() {
        openPage("/com/example/cab302project/calendar-view.fxml", "Calendar");
    }

    @FXML
    private void openFriendsPage() {
        openPage("/com/example/cab302project/friends-view.fxml", "Friends");
    }

    @FXML
    private void handleLogOut() {
        Session.clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/login-view.fxml"));
            Parent loginRoot = loader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(loginRoot, 380, 500));
            loginStage.setTitle("Smart Schedule Assistant");
            loginStage.centerOnScreen();
            loginStage.show();

            Stage current = (Stage) mainContent.getScene().getWindow();
            current.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openPage(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();

            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

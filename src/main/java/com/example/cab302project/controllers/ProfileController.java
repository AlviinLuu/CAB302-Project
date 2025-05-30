package com.example.cab302project.controllers;

import com.example.cab302project.models.CalendarDAO;
import com.example.cab302project.models.SqliteUserDAO;
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
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Controller for the Profile view of the application.
 * Displays the logged-in user's profile info, profile image, and a mini day calendar.
 * Handles navigation (settings, home, friends, logout) and profile display logic.
 */
public class ProfileController {

    /**
     * Displays the user's username.
     */
    @FXML private Label profileNameLabel;

    /**
     * Displays the user's bio.
     */
    @FXML private Label bioLabel;

    /**
     * Displays the user's profile image.
     */
    @FXML private ImageView profileImageView;

    /**
     * Mini calendar GridPane for today's 24-hour day view.
     */
    @FXML private GridPane miniDayView;

    /**
     * The main VBox container for profile view content.
     */
    @FXML private VBox mainContent;

    /**
     * ImageView for the application logo.
     */
    @FXML private ImageView logoImage;

    /**
     * Sidebar button showing the current user's username.
     */
    @FXML private Button profileButton;

    /**
     * The current date shown in the mini day view.
     */
    private final LocalDate currentDate = LocalDate.now();

    /**
     * DAO for user-related database actions.
     */
    private final SqliteUserDAO userDAO = new SqliteUserDAO();

    /**
     * Fill colour for highlighting event-in-progress slots.
     */
    private final String fillColour = "-fx-background-color: black ;";

    /**
     * Initializes the profile view after FXML loading.
     * Loads user details, profile image, logo, mini day calendar, and sets the sidebar button.
     */
    @FXML
    private void initialize() {
        User user = Session.getLoggedInUser();

        if (user != null) {
            profileNameLabel.setText(user.getUsername() != null ? user.getUsername() : "User");
            bioLabel.setText(user.getBio() != null ? user.getBio() : "No bio provided.");
            byte[] imgData = userDAO.getProfileImage(user.getEmail());
            if (imgData != null && imgData.length > 0) {
                profileImageView.setImage(new Image(new ByteArrayInputStream(imgData)));
            } else {
                setDefaultProfileImage();
            }
        } else {
            profileNameLabel.setText("User");
            bioLabel.setText("No user logged in.");
            setDefaultProfileImage();
        }

        // Load logo image from resources
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);

        renderMiniDayView();

        User sessionUser = Session.getLoggedInUser();
        try {
            if (sessionUser != null && sessionUser.getUsername() != null) {
                profileButton.setText(sessionUser.getUsername());
            } else {
                profileButton.setText("Profile");
                System.err.println("Warning: sessionUser or username is null.");
            }
        } catch (Exception e) {
            profileButton.setText("Profile");
            e.printStackTrace();
        }
    }

    /**
     * Sets the profile image to a default placeholder.
     */
    private void setDefaultProfileImage() {
        Image placeholder = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
        profileImageView.setImage(placeholder);
    }

    /**
     * Renders a mini day view (24 hour slots) for the sidebar calendar.
     * Highlights any hour blocks with events and displays event names if present.
     */
    private void renderMiniDayView() {
        miniDayView.getChildren().clear();
        miniDayView.getRowConstraints().clear();

        var dayCalendar = new CalendarDAO(currentDate, Period.of(0,0,1), TimeUnit.HOURS);

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

            var fEvent = dayCalendar.getFirstEventForInterval(currentDate, hour, TimeUnit.HOURS);
            if (fEvent != null) {
                event.setText(fEvent.getName());
            }
            if (dayCalendar.IsAnyEventInProgress(currentDate.atTime(LocalTime.of(hour,0,0)))) {
                event.setStyle(fillColour);
            }

            miniDayView.add(time, 0, hour + 1);
            miniDayView.add(event, 1, hour + 1);
            GridPane.setHgrow(event, Priority.ALWAYS);
        }
    }

    /**
     * Navigates to the settings page, replacing the current scene with the settings-view.
     */
    @FXML
    private void openSettingsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/settings-view.fxml"));
            Parent settingsRoot = loader.load();
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            Scene scene = currentStage.getScene();
            scene.setRoot(settingsRoot);
            currentStage.setTitle("Settings"); // Optional: Update window title
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles mouse hover on the logo image, enlarging it for UI feedback.
     */
    @FXML
    private void onLogoHover() {
        logoImage.setScaleX(1.2);
        logoImage.setScaleY(1.2);
    }

    /**
     * Handles mouse exit from the logo image, resetting it to original size.
     */
    @FXML
    private void onLogoExit() {
        logoImage.setScaleX(1.0);
        logoImage.setScaleY(1.0);
    }

    /**
     * Navigates back to the main calendar (home) page, replacing the current scene.
     */
    @FXML
    private void goToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/calendar-view.fxml"));
            Parent homeRoot = loader.load();
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            Scene scene = currentStage.getScene();
            scene.setRoot(homeRoot);
            currentStage.setTitle("Smart Schedule Assistant");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the friends page, replacing the current scene.
     */
    @FXML
    private void openFriendsPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/friends-view.fxml"));
            Parent friendsRoot = loader.load();
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            Scene scene = currentStage.getScene();
            scene.setRoot(friendsRoot);
            currentStage.setTitle("Friends");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles logout action.
     * Clears the session and returns to the login screen, matching the initial launch window size and behavior.
     */
    @FXML
    private void handleLogOut() {
        Session.clear();
        System.out.println("Logging out...");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/login-view.fxml"));
            Parent loginRoot = loader.load();

            Stage loginStage = new Stage();
            loginStage.setTitle("Smart Schedule Assistant");

            // Match initial launch size exactly
            Scene scene = new Scene(loginRoot, 450, 600);
            loginStage.setScene(scene);
            loginStage.setResizable(true);
            loginStage.centerOnScreen();

            loginStage.show();

            // Close the profile window
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a given FXML page in a new maximized window, closing the current profile view.
     * @param fxmlPath Path to the FXML file.
     * @param title    Window title to set.
     */
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

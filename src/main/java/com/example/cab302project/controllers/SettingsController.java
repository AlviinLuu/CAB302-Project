package com.example.cab302project.controllers;
import com.example.cab302project.models.IUserDAO;
import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.services.CalendarImportView;
import com.example.cab302project.util.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Controller class for the Settings view of the application.
 * Handles user interactions on the settings page, including updating user information,
 * managing the profile image, importing calendar data, and navigating to other views.
 */
public class SettingsController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button profileButton;
    @FXML private ImageView profileImage;
    @FXML private ImageView logoImage;
    @FXML private Button uploadImageButton, changeEmailButton, changePasswordButton, UploadingGoogleCalendar;
    //@FXML private Button homeButton, settingsButton, friendsButton, signOutButton;
    @FXML private Label dateLabel;
    @FXML private VBox aiSidebar;
    @FXML private GridPane miniDayView;
    @FXML private SplitPane splitPane;
    @FXML
    private VBox mainContent;
    @FXML private Button editBioButton;
    @FXML private TextArea bioTextArea;
    @FXML private Label calendarSyncStatusLabel;
    @FXML private Label quoteLabel;


    // === Local State ===
    private final LocalDate currentDate = LocalDate.now();
    private final IUserDAO userDAO = new SqliteUserDAO();

    /**
     * Initializes the controller after the FXML file has been loaded.
     * Sets up UI elements by loading user data (bio, profile image) from the session/database,
     * and configures the sidebar and mini-calendar view.
     */
    @FXML
    private void initialize() {
        //Loads logo
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);

        //Sidebar fully expanded on load
        splitPane.setDividerPositions(0.75);

        // Display current user password and email
        User currentUser = Session.getLoggedInUser();
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            passwordField.setText(currentUser.getPassword()); // ⚠️ Only if passwords are stored in plain text (not recommended)
        } else {
            emailField.setText("");
            passwordField.setText("");
        }


        //Apply sidebar styling and calendar info
        //applySidebarButtonStyle();
        updateDateLabel();
        renderMiniDayView();

        bioTextArea.setDisable(false); // Always allow editing
//        User currentUser = Session.getLoggedInUser();
        if (currentUser != null) {
            bioTextArea.setText(currentUser.getBio() != null ? currentUser.getBio() : "");

            // Load profile image from DB
            byte[] imageData = userDAO.getProfileImage(currentUser.getEmail());
            if (imageData != null) {
                InputStream is = new ByteArrayInputStream(imageData);
                profileImage.setImage(new Image(is));
            }

        } else {
            bioTextArea.setText("Unable to load bio.");
        }

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
            e.printStackTrace(); //
        }

        // show random quote at the end of the settings page
        setRandomQuote();

    }

    //private void applySidebarButtonStyle() {
    //   homeButton.setStyle("-fx-text-fill: #1A1A1A; -fx-background-color: #CCCCFF; " +
    //          "-fx-font-size: 20px; -fx-background-radius: 12px; -fx-font-weight: bold;");
    // settingsButton.setStyle("-fx-text-fill: #1A1A1A; -fx-background-color: #D8B9FF; " +
    //         "-fx-font-size: 20px; -fx-background-radius: 12px; -fx-font-weight: bold;");
    // friendsButton.setStyle("-fx-text-fill: #1A1A1A; -fx-background-color: #CCCCFF; " +
    //        "-fx-font-size: 20px; -fx-background-radius: 12px; -fx-font-weight: bold;");
    // }

    private void updateDateLabel() {
        if (dateLabel != null) {
            dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
        }
    }

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

    @FXML
    private void onLogoHover() {
        logoImage.setScaleX(1.2);
        logoImage.setScaleY(1.2);
    }

    @FXML
    private void onLogoExit() {
        logoImage.setScaleX(1.0);
        logoImage.setScaleY(1.0);
    }

    @FXML
    private void handleUploadGoogleCalendar() {
        System.out.println("📂 Upload button clicked.");

        // Get the logged-in user
        User user = Session.getLoggedInUser();

        if (user != null) {
            String userEmail = user.getEmail(); // or however you store it

            // Clear only that user's events
            SqliteUserDAO sqliteUserDAO = new SqliteUserDAO();
            sqliteUserDAO.clearEventsByEmail(userEmail);
            System.out.println("🧹 Events for user " + userEmail + " cleared.");

            // Proceed with file selection
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Upload Google Calendar (.ics)");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("iCalendar Files", "*.ics")
            );
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                try {
                    System.out.println("📄 Selected file: " + selectedFile.getAbsolutePath());

                    // Parse the file for this user
                    CalendarImportView.importCalendarFile(selectedFile, user.getId());

                    // Show status and alert on the settings page
                    calendarSyncStatusLabel.setText("Google Calendar synced ✔");
                    calendarSyncStatusLabel.setStyle("-fx-text-fill: #6A4B8B; -fx-font-size: 14px; -fx-font-style: italic;");
                    showAlert(Alert.AlertType.INFORMATION, "Calendar Synced", "Google Calendar has been successfully synced!");

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to load calendar file.");
                }
            } else {
                System.out.println("⚠️ No file selected.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Error", "No user is currently logged in.");
        }
    }

    /**
     * Handles the action of the Upload Image button.
     * Opens a file chooser for image files, loads the selected image,
     * converts it to a byte array, and updates the user's profile image in the database.
     * Then Displays alerts for success or failure.
     */
    @FXML
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                // Load image into JavaFX ImageView
                Image image = new Image(selectedFile.toURI().toString());
                profileImage.setImage(image);

                // Convert to byte array
                byte[] imageData = java.nio.file.Files.readAllBytes(selectedFile.toPath());

                User currentUser = Session.getLoggedInUser();
                if (currentUser != null) {
                    boolean success = userDAO.updateProfileImage(currentUser.getEmail(), imageData);
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Profile image updated!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to save image.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to read image file.");
            }
        }
    }

    /**
     * Handles the action of the Change Email button.
     * Retrieves the new email from the email field and validates it,
     * It then updates the user's email address in the database and session.
     * Displays alerts for success or failure.
     */
    @FXML
    private void handleChangeEmail() {
        String newEmail = emailField.getText();
        User currentUser = Session.getLoggedInUser();

        if (newEmail.contains("@") && currentUser != null) {
            boolean success = userDAO.updateEmail(currentUser.getEmail(), newEmail);
            if (success) {
                currentUser.setEmail(newEmail);
                Session.setLoggedInUser(currentUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Email updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update email. Try again.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please enter a valid email address.");
        }
    }

    /**
     * Handles the action for the Change Password button.
     * Retrieves the new password from the password field, validates its length,
     * and updates the user's password in the database and session.
     * Displays alerts for success or failure.
     */
    @FXML
    private void handleChangePassword() {
        String newPassword = passwordField.getText();
        User currentUser = Session.getLoggedInUser();

        if (newPassword.length() >= 6 && currentUser != null) {
            boolean success = userDAO.updatePassword(currentUser.getEmail(), newPassword);
            if (success) {
                currentUser.setPassword(newPassword);
                Session.setLoggedInUser(currentUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Password updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update password. Try again.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Warning", "Password must be at least 6 characters.");
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private final String[] appQuotes = {
            "Great plans start with great friends.",
            "Find time for what matters — together.",
            "Sync lives, not just calendars.",
            "When you’re free, and so are they.",
            "Friendships thrive when time aligns.",
            "Life’s better when you're on the same schedule.",
            "Making memories, one synced slot at a time.",
            "Time isn’t the problem. Syncing is.",
            "Planning made social.",
            "We help you find time for friends — literally."
    };

    private void setRandomQuote() {
        int index = (int) (Math.random() * appQuotes.length);
        quoteLabel.setText('"' + appQuotes[index] + '"');
    }

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
            loginStage.setResizable(true); // Match startup behavior
            loginStage.centerOnScreen();   // for polish

            loginStage.show();

            // Close the settings window
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    //    @FXML
//    private void goToFriends() {
//        System.out.println("Navigating to Friends Page (to be implemented).");
//    }
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

    @FXML
    private void openProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/profile-view.fxml"));
            Parent profileRoot = loader.load();
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            Scene scene = currentStage.getScene();
            scene.setRoot(profileRoot);
            currentStage.setTitle("Profile");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the action for the Edit Bio and Save Bio buttons.
     * Retrieves the text from the bio text area, updates the user object in the session,
     * then saves the updated bio to the database.
     * Displays alerts for success or failure.
     */
    @FXML
    private void handleEditBio() {
        // Always editable, just save the current text to DB
        String newBio = bioTextArea.getText();
        User currentUser = Session.getLoggedInUser();

        if (currentUser != null && newBio != null) {
            int wordCount = newBio.trim().isEmpty() ? 0 : newBio.trim().split("\\s+").length;

            if (wordCount > 100) {
                showAlert(Alert.AlertType.WARNING, "Word Limit Exceeded", "Your bio must be 100 words or less.");
                return;
            }

            currentUser.setBio(newBio);
            boolean success = userDAO.updateBio(currentUser.getEmail(), newBio);

            if (success) {
                Session.setLoggedInUser(currentUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Bio updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update bio. Try again.");
            }
        }
    }

    // @FXML
    // private void handleSignOut() {
    //    System.out.println("Signing out...");
    //    try {
    //       FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/calendarpage/login-view.fxml"));
    //       Parent loginRoot = loader.load();
    //        Stage stage = (Stage) signOutButton.getScene().getWindow();
    //        stage.setScene(new Scene(loginRoot));
    //         stage.setTitle("Login");
    //         stage.setMaximized(true);
    //    } catch (IOException e) {
    //        e.printStackTrace();
    //     }
    // }
}
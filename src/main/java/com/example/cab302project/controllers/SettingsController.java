package com.example.cab302project.controllers;

import com.example.cab302project.models.IUserDAO;
import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.services.CalendarImportView;
import com.example.cab302project.util.Session;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the Settings view of the application.
 * Allows users to edit account information, upload a profile image,
 * sync Google Calendar, update bio, and navigate to other pages.
 * Renders the sidebar, mini day view, and shows motivational quotes.
 */
public class SettingsController {

    /** Field for editing the user's email. */
    @FXML private TextField emailField;

    /** Field for editing the user's password. */
    @FXML private PasswordField passwordField;

    /** Sidebar button showing the user's profile name. */
    @FXML private Button profileButton;

    /** Displays the user's profile image. */
    @FXML private ImageView profileImage;

    /** Displays the application logo. */
    @FXML private ImageView logoImage;

    /** Button to upload a new profile image. */
    @FXML private Button uploadImageButton;

    /** Button to change the email address. */
    @FXML private Button changeEmailButton;

    /** Button to change the password. */
    @FXML private Button changePasswordButton;

    /** Button to upload/sync Google Calendar (.ics) file. */
    @FXML private Button UploadingGoogleCalendar;

    /** Displays the current date at the top of the sidebar. */
    @FXML private Label dateLabel;

    /** VBox container for the sidebar. */
    @FXML private VBox Sidebar;

    /** GridPane showing a mini day view (24-hour calendar) in the sidebar. */
    @FXML private GridPane miniDayView;

    /** Main split pane separating sidebar and main settings content. */
    @FXML private SplitPane splitPane;

    /** Main VBox container for settings content. */
    @FXML private VBox mainContent;

    /** Button to save or edit the user bio. */
    @FXML private Button editBioButton;

    /** Editable text area for the user's bio. */
    @FXML private TextArea bioTextArea;

    /** Label showing the status of calendar sync. */
    @FXML private Label calendarSyncStatusLabel;

    /** Label to display a motivational or fun quote. */
    @FXML private Label quoteLabel;

    // === Local State ===

    /** Current date, used for the sidebar mini day view and date label. */
    private final LocalDate currentDate = LocalDate.now();

    /** DAO for user-related actions (DB access). */
    private final IUserDAO userDAO = new SqliteUserDAO();

    // ================== IMAGE HANDLING =====================

    /**
     * Crops and resizes a JavaFX Image to a square of the specified width and height.
     * Center-crops the image, then scales to the target size.
     * @param input The original JavaFX Image.
     * @param width The target width (pixels).
     * @param height The target height (pixels).
     * @return Cropped and resized JavaFX Image.
     */
    private Image cropAndResizeImage(Image input, int width, int height) {
        double origWidth = input.getWidth();
        double origHeight = input.getHeight();
        double size = Math.min(origWidth, origHeight);

        // Crop: calculate coordinates for a square
        double x = (origWidth - size) / 2.0;
        double y = (origHeight - size) / 2.0;

        PixelReader reader = input.getPixelReader();
        WritableImage cropped = new WritableImage(reader, (int)x, (int)y, (int)size, (int)size);

        // Convert cropped WritableImage to BufferedImage
        BufferedImage bImage = SwingFXUtils.fromFXImage(cropped, null);
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g = resized.createGraphics();
        g.drawImage(bImage, 0, 0, width, height, null);
        g.dispose();

        return SwingFXUtils.toFXImage(resized, null);
    }

    /**
     * Converts a JavaFX Image to a PNG byte array for database storage.
     * @param image JavaFX Image to convert.
     * @return Byte array representing the PNG-encoded image.
     * @throws IOException if encoding fails.
     */
    private byte[] imageToByteArray(Image image) throws IOException {
        BufferedImage bImage = SwingFXUtils.fromFXImage(image, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bImage, "png", out);
        return out.toByteArray();
    }

    // ================== INITIALIZATION =====================

    /**
     * Initializes the settings view after FXML loading.
     * Loads logo, sidebar, user info, profile image, bio, date, quote, and sets up sidebar.
     */
    @FXML
    private void initialize() {
        // Loads logo
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);

        // Sidebar fully expanded on load
        splitPane.setDividerPositions(0.75);

        // Display current user password and email
        User currentUser = Session.getLoggedInUser();
        if (currentUser != null) {
            emailField.setText(currentUser.getEmail());
            passwordField.setText(currentUser.getPassword()); // Only if passwords are stored as plain text
        } else {
            emailField.setText("");
            passwordField.setText("");
        }

        // Apply sidebar styling and calendar info
        updateDateLabel();
        renderMiniDayView();

        bioTextArea.setDisable(false); // Always allow editing

        if (currentUser != null) {
            bioTextArea.setText(currentUser.getBio() != null ? currentUser.getBio() : "");

            // Load profile image from DB and ensure it displays as 400x400
            byte[] imageData = userDAO.getProfileImage(currentUser.getEmail());
            if (imageData != null) {
                InputStream is = new ByteArrayInputStream(imageData);
                Image loaded = new Image(is);
                Image processed = cropAndResizeImage(loaded, 400, 400);
                profileImage.setImage(processed);
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
            e.printStackTrace();
        }

        setRandomQuote();
    }

    /**
     * Updates the sidebar's date label to the current date.
     */
    private void updateDateLabel() {
        if (dateLabel != null) {
            dateLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("d MMM")));
        }
    }

    /**
     * Renders a mini day calendar view (24 hourly slots) in the sidebar.
     */
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

    // ================== UI INTERACTIONS & NAVIGATION =====================

    /**
     * Enlarges the logo when hovered for UI feedback.
     */
    @FXML
    private void onLogoHover() {
        logoImage.setScaleX(1.2);
        logoImage.setScaleY(1.2);
    }

    /**
     * Resets the logo to its original size when mouse exits.
     */
    @FXML
    private void onLogoExit() {
        logoImage.setScaleX(1.0);
        logoImage.setScaleY(1.0);
    }

    /**
     * Handles uploading a Google Calendar (.ics) file.
     * Clears existing user events, prompts user to select file, imports events, and updates status label.
     */
    @FXML
    private void handleUploadGoogleCalendar() {
        System.out.println("📂 Upload button clicked.");

        // Get the logged-in user
        User user = Session.getLoggedInUser();

        if (user != null) {
            String userEmail = user.getEmail();

            // Clear only that user's events
            SqliteUserDAO sqliteUserDAO = new SqliteUserDAO();
            sqliteUserDAO.clearEventsByEmail(userEmail);
            System.out.println("🧹 Events for user " + userEmail + " cleared.");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Upload Google Calendar (.ics)");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("iCalendar Files", "*.ics")
            );
            File selectedFile = fileChooser.showOpenDialog(null);

            if (selectedFile != null) {
                try {
                    System.out.println("📄 Selected file: " + selectedFile.getAbsolutePath());

                    CalendarImportView.importCalendarFile(selectedFile, user.getId());

                    calendarSyncStatusLabel.setText("Google Calendar Synced ✔");
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
     * Handles the upload of a new profile image.
     * Allows user to choose image, crops and resizes, updates image in UI and DB.
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
                // Load original image from file
                Image originalImage = new Image(selectedFile.toURI().toString());

                // Crop and resize to 300x300
                Image processedImage = cropAndResizeImage(originalImage, 300, 300);

                // Display 300x300 image in UI
                profileImage.setImage(processedImage);

                // Convert the cropped/resized image to byte array for database storage
                byte[] imageData = imageToByteArray(processedImage);

                // Save to DB
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
     * Handles changing the user's email address.
     * Validates input, updates in database, updates session, and shows a status alert.
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
     * Handles changing the user's password.
     * Validates minimum length, updates in database, updates session, and shows status alert.
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

    /**
     * Displays a standard JavaFX alert dialog.
     * @param alertType Alert type (information, warning, error, etc).
     * @param title Title text for the dialog.
     * @param message Main content/message.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================== MOTIVATIONAL QUOTES =====================

    /** Array of motivational or app-themed quotes shown in the sidebar. */
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

    /**
     * Sets a random motivational or themed quote in the sidebar.
     */
    private void setRandomQuote() {
        int index = (int) (Math.random() * appQuotes.length);
        quoteLabel.setText('"' + appQuotes[index] + '"');
    }

    // ================== NAVIGATION =====================

    /**
     * Navigates to the calendar (home) page, replacing the current scene.
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
     * Handles logout: clears session and returns to login screen.
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
     * Navigates to the profile page, replacing the current scene.
     */
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

    // ================== BIO =====================

    /**
     * Handles editing the user's bio. Saves the current text to DB (up to 100 words).
     */
    @FXML
    private void handleEditBio() {
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
}

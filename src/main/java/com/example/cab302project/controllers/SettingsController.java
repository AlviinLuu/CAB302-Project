package com.example.cab302project.controllers;
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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class SettingsController {

    // === FXML UI Elements ===
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ImageView profileImage;
    @FXML private ImageView logoImage;
    @FXML private Button uploadImageButton, changeEmailButton, changePasswordButton;
    //@FXML private Button homeButton, settingsButton, friendsButton, signOutButton;
    @FXML private Label dateLabel;
    @FXML private VBox aiSidebar;
    @FXML private GridPane miniDayView;
    @FXML private SplitPane splitPane;
    @FXML
    private VBox mainContent;
    // === Local State ===
    private final LocalDate currentDate = LocalDate.now();

    @FXML
    private void initialize() {
        // Load logo
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);

        // Sidebar fully expanded on load
        splitPane.setDividerPositions(0.75);

        // Dummy data for visual feedback
        emailField.setText("example@domain.com");
        passwordField.setText("********");

        // Apply sidebar styling and calendar info
        //applySidebarButtonStyle();
        updateDateLabel();
        renderMiniDayView();
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
    private void handleUploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Profile Picture");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            profileImage.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    @FXML
    private void handleChangeEmail() {
        String newEmail = emailField.getText();
        if (newEmail.contains("@")) {
            System.out.println("Email changed to: " + newEmail);
        } else {
            System.out.println("Invalid email entered.");
        }
    }

    @FXML
    private void handleChangePassword() {
        String newPassword = passwordField.getText();
        if (newPassword.length() >= 6) {
            System.out.println("Password changed.");
        } else {
            System.out.println("Password too short.");
        }
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

            // 🌟 Set it to full screen
            homeStage.setMaximized(true); // Maximize the window

            homeStage.show();

            // 🛑 Close the current Settings page
            Stage currentStage = (Stage) mainContent.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    @FXML
    private void goToFriends() {
        System.out.println("Navigating to Friends Page (to be implemented).");
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

package com.example.cab302project.controllers;

import com.example.cab302project.models.IUserDAO;
import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.util.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Controller class for the Login and Registration views.
 * Manages the UI elements and logic for users to log in or register for an account.
 * Handles switching between login and registration states, validating input,
 * interacting with the user DAO, and managing the user session upon successful authentication.
 */
public class LoginController {
    @FXML private Label introText;
    @FXML private Label errorLabel;
    @FXML private Boolean isLogin = true;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField repeatPasswordField;
    @FXML private Button switchState;
    @FXML private Button submitBtn = new Button();
    @FXML private ImageView logoImage;

    private final IUserDAO userDAO = new SqliteUserDAO();
    public final String loginText = "Welcome Back!";
    public final String registerText = "Welcome Aboard!";

    // Email pattern: user@domain.com
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$");

    @FXML
    private void initialize() {
        // load and set logo
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);
    }

    @FXML
    protected void onSwitchStateClick() {
        if (this.isLogin) {
            // switch to register mode
            introText.setText(registerText);
            switchState.setText("Login Instead");
            repeatPasswordField.setVisible(true);
            usernameField.setVisible(true);
            submitBtn.setText("Register");
        } else {
            // switch to login mode
            introText.setText(loginText);
            switchState.setText("Register Instead");
            repeatPasswordField.setVisible(false);
            usernameField.setVisible(false);
            submitBtn.setText("Login");
        }
        this.isLogin = !this.isLogin;
    }

    @FXML
    protected void onSubmitButtonClick() {
        errorLabel.setVisible(false);
        String username     = usernameField.getText();
        String email        = emailField.getText();
        String password     = passwordField.getText();
        String rptPassword  = repeatPasswordField.getText();

        // Basic non-empty check
        if (email == null || email.isEmpty()
                || password == null || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        // Format checks
        if (!isValidEmail(email)) {
            showError("Invalid email format. Please use user@domain.com");
            return;
        }


        if (this.isLogin) {
            // ---- LOGIN FLOW ----
            System.out.println("Attempting login...");
            boolean isValidUser = userDAO.validateUser(email, password);
            if (isValidUser) {
                User user = userDAO.getUserByEmail(email);
                Session.setLoggedInUser(user);
                System.out.println("Login successful!");
                openCalendarPage();
            } else {
                System.out.println("Login failed.");
                showError("Invalid email or password.");
            }

        } else {
            // ---- REGISTRATION FLOW ----
            System.out.println("Attempting registration...");
            if (!isValidPassword(password)) {
                showError("Password must be at least 8 characters long.");
                return;
            }
            if (!password.equals(rptPassword)) {
                System.out.println("Passwords do not match.");
                showError("Passwords do not match.");
                return;
            }
            if (userDAO.userExists(email)) {
                System.out.println("Email already in use.");
                showError("Account with this email already exists.");
                return;
            }
            if (userDAO.getUserByUsername(username) != null) {
                System.out.println("Username already in use.");
                showError("Account with this username already exists.");
                return;
            }

            // all good → create user
            User newUser = new User(username, password, email);
            userDAO.addUser(newUser);
            System.out.println("User registered successfully!");

            // show success dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Registration Successful");
            alert.setHeaderText("Welcome, " + username + "!");
            alert.setContentText(
                    "To get started with our application, go to the Settings page on the right side and upload your calendar ICS file.\n\n" +
                            "This will allow you to view your calendar events. Don't forget to add friends to make use of our AI feature!!\n\n" +
                            "Press OK and Login to get started."
            );
            alert.showAndWait();

            // clear & switch back to login
            emailField.clear();
            passwordField.clear();
            repeatPasswordField.clear();
            usernameField.clear();
            onSwitchStateClick();
        }
    }

    private void openCalendarPage() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/cab302project/calendar-view.fxml")
            );
            Parent calendarRoot = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Calendar Page");
            stage.setScene(new Scene(calendarRoot));
            stage.setMaximized(true);
            stage.show();

            // close login window
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Validates that the email matches something@something.com */
    private boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /** Validates that the password is at least 8 characters long */
    private boolean isValidPassword(String pw) {
        return pw != null && pw.length() >= 8;
    }

    private void showError(String msg) {
        errorLabel.setVisible(true);
        errorLabel.setText(msg);
    }
}

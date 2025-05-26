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
/**
 * Controller class for the Login and Registration views.
 * Manages the UI elements and logic for users to log in or register for an account.
 * Handles switching between login and registration states, validating input,
 * interacting with the user DAO, and managing the user session upon successful authentication.
 */
public class LoginController {
    @FXML
    private Label introText;
    @FXML
    private Label errorLabel;
    @FXML
    private Boolean isLogin = true;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField repeatPasswordField;
    @FXML
    private Button switchState;
    @FXML
    private Button submitBtn = new Button();

    @FXML
    private ImageView logoImage;
    private final IUserDAO userDAO = new SqliteUserDAO();
    public final String loginText = "Welcome Back!";
    public final String registerText = "Welcome Aboard!";

    public LoginController() {
    }

    @FXML
    private void initialize() {
        // logo
        Image logo = new Image(getClass().getResourceAsStream("/images/logo.png"));
        logoImage.setImage(logo);
    }

    /**
     * Handles the action for the button that switches between Login and Registration states.
     * Toggles the visibility of fields and updates button/label text accordingly.
     */
    @FXML
    protected void onSwitchStateClick() {
        if (this.isLogin) {
            //change to "register" state
            this.introText.setText(registerText);
            this.switchState.setText("Login Instead");
            this.repeatPasswordField.setVisible(true);
            this.usernameField.setVisible(true);
            submitBtn.setText("Register");

        } else {
            this.introText.setText(loginText);
            this.switchState.setText("Register Instead");
            this.repeatPasswordField.setVisible(false);
            this.usernameField.setVisible(false);
            submitBtn.setText("Login");

        }
        this.isLogin = !this.isLogin;
    }

    /**
     * Handles the action for the main submit button (Login or Register).
     * Reads input fields then performs validation based on the current state (Login or Registration),
     * interacts with the IUserDAO to authenticate or register the user,
     * manages the user Session, and navigates to the calendar page on successful login.
     * Displays error messages for various failure cases.
     */
    @FXML
    protected void onSubmitButtonClick() {
        this.errorLabel.setVisible(false);
        String username = this.usernameField.getText();
        String email = this.emailField.getText();
        String password = this.passwordField.getText();
        String rptPassword = this.repeatPasswordField.getText();

        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            if (this.isLogin) {
                System.out.println("Attempting login...");
                boolean isValidUser = this.userDAO.validateUser(email, password);
                if (isValidUser) {
                    User user = userDAO.getUserByEmail(email);
                    Session.setLoggedInUser(user);
                    System.out.println("Login successful!");
                    openCalendarPage();
                } else {
                    System.out.println("Login failed.");
                    this.showError("Invalid email or password.");
                }
            } else {
                System.out.println("Attempting registration...");
                if (!password.equals(rptPassword)) {
                    System.out.println("Passwords do not match.");
                    this.showError("Passwords do not match.");
                    return;
                }
                if (this.userDAO.userExists(email)) {
                    System.out.println("Email already in use.");
                    this.showError("Account with this email already exists.");
                } else if (this.userDAO.getUserByUsername(email) != null) {
                    System.out.println("Username already in use.");
                    this.showError("Account with this username already exists.");
                } else if(!isValidPassword(password)){
                    this.showError("Password Does Not Meet Requirements");
                }else {
                    User newUser = new User(username, password, email);
                    this.userDAO.addUser(newUser); // No return value, so we assume success only after checks
                    System.out.println("User registered successfully!");
//                    this.showError("Registration successful!");

                    // Show instructional message
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registration Successful");
                    alert.setHeaderText("Welcome, " + username + "!");
                    alert.setContentText(
                            "To get started with our application, go to the Settings page on the right side and upload your calendar ICS file.\n\n" +
                                    "This will allow you to view your calendar events. Don't forget to add friends to make use of our AI feature!!\n\n" +
                            "Press OK and Login to get started."
                    );
                    alert.showAndWait();

                    // Clear fields and reset UI
                    this.emailField.clear();
                    this.passwordField.clear();
                    this.repeatPasswordField.clear();
                    this.usernameField.clear();
                    this.onSwitchStateClick(); // Switch back to login mode
                }
            }
        } else {
            this.showError("Please fill in all fields.");
        }
    }


    private void openCalendarPage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/cab302project/calendar-view.fxml"));
            Parent calendarRoot = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Calendar Page");
            stage.setScene(new Scene(calendarRoot));
            stage.setMaximized(true);
            stage.show();

            // Close the login window
            Stage currentStage = (Stage) emailField.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean isValidEmail(String email){
        return true;
    }
    private boolean isValidPassword(String pw){
        return true;
    }
    private void showError(String msg) {
        this.errorLabel.setVisible(true);
        this.errorLabel.setText(msg);
    }
}

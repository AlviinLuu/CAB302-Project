package com.example.cab302project.controllers;

import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import com.example.cab302project.models.IUserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import jdk.jshell.spi.ExecutionControl;

public class LoginController {
    @FXML
    private Label introText;
    @FXML
    private Boolean isLogin = true; // Start in "login" mode (false)
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
    private Label errorLabel; // Label to display error messages

    // Database access object (DAO)
    private final IUserDAO userDAO;

    public final String loginText = "Welcome Back!";
    public final String registerText = "Welcome Aboard!";

    public LoginController() {
        // Initialize userDAO to interact with the database
        this.userDAO = new SqliteUserDAO();
    }

    @FXML
    protected void onSwitchStateClick() {
        if (isLogin) {
            // Switch to Register mode
            introText.setText(registerText);
            switchState.setText("Login Instead");
            repeatPasswordField.setVisible(true); // Show repeat password for registration
            usernameField.setVisible(true);
        } else {
            // Switch to Login mode
            introText.setText(loginText);
            switchState.setText("Register Instead");
            repeatPasswordField.setVisible(false); // Hide repeat password for login
            usernameField.setVisible(false);
        }
        isLogin = !isLogin; // Toggle the login state
    }

    @FXML
    protected void onSubmitButtonClick() {
        //hide error label (will get immediatly shown again if there is a problem)
        errorLabel.setVisible(false);
        // Get input values from the fields
        String email = emailField.getText();
        String password = passwordField.getText();
        String rptPassword = repeatPasswordField.getText(); // For registration only

        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            if (isLogin) {
                // Login logic
                System.out.println("Attempting login...");

                // Check if user exists in the database
                boolean isValidUser = userDAO.validateUser(email, password);
                if (isValidUser) {
                    System.out.println("Login successful!");
                    // Proceed to the next screen (e.g., main app screen)
                    // You can use a method like `goToHomePage()` here
                } else {
                    System.out.println("Login failed.");
                    // Show error message
                    showError("Invalid email or password.");
                }
            } else {
                // Registration logic
                System.out.println("Attempting registration...");

                // Check if the email already exists in the database
                if (userDAO.userExists(email)) {
                    System.out.println("Account with this email already exists.");
                    showError("Account with this email already exists.");
                } else if (rptPassword != null && password.equals(rptPassword)) {
                    // Ensure password matches the repeat password
                    User newUser = new User(email, password, email); // Create a new user (email as username or adjust)
                    userDAO.addUser(newUser); // Add the user to the database
                    System.out.println("User registered successfully!");

                    // Show success message
                    showError("Registration successful!");


                    // Optionally clear fields after registration
                    emailField.clear();
                    passwordField.clear();
                    repeatPasswordField.clear();
                } else {
                    // Passwords do not match during registration
                    System.out.println("Passwords do not match.");
                    showError("Passwords do not match.");
                }
            }
        } else {
            // Handle empty fields (email or password)
            showError("Please fill in all fields.");
        }
    }

    private void showError(String msg){
        errorLabel.setVisible(true);
        errorLabel.setText(msg);
    }

}

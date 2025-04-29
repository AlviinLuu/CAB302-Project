package com.example.cab302project.controllers;

import com.example.cab302project.models.IUserDAO;
import com.example.cab302project.models.SqliteUserDAO;
import com.example.cab302project.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private Label introText;
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
    private Label errorLabel;

    private final IUserDAO userDAO = new SqliteUserDAO();

    public final String loginText = "Welcome Back!";
    public final String registerText = "Welcome Aboard!";

    public LoginController() {}

    @FXML
    protected void onSwitchStateClick() {
        if (this.isLogin) {
            this.introText.setText(registerText);
            this.switchState.setText("Login Instead");
            this.repeatPasswordField.setVisible(true);
            this.usernameField.setVisible(true);
        } else {
            this.introText.setText(loginText);
            this.switchState.setText("Register Instead");
            this.repeatPasswordField.setVisible(false);
            this.usernameField.setVisible(false);
        }
        this.isLogin = !this.isLogin;
    }

    @FXML
    protected void onSubmitButtonClick() {
        this.errorLabel.setVisible(false);
        String email = this.emailField.getText();
        String password = this.passwordField.getText();
        String rptPassword = this.repeatPasswordField.getText();

        if (email != null && !email.isEmpty() && password != null && !password.isEmpty()) {
            if (this.isLogin) {
                System.out.println("Attempting login...");
                boolean isValidUser = this.userDAO.validateUser(email, password);
                if (isValidUser) {
                    System.out.println("Login successful!");
                    openCalendarPage();
                } else {
                    System.out.println("Login failed.");
                    this.showError("Invalid email or password.");
                }
            } else {
                System.out.println("Attempting registration...");
                if (this.userDAO.userExists(email)) {
                    System.out.println("Account with this email already exists.");
                    this.showError("Account with this email already exists.");
                } else if (rptPassword != null && password.equals(rptPassword)) {
                    User newUser = new User(email, password, email);
                    this.userDAO.addUser(newUser);
                    System.out.println("User registered successfully!");
                    this.showError("Registration successful!");
                    this.emailField.clear();
                    this.passwordField.clear();
                    this.repeatPasswordField.clear();
                } else {
                    System.out.println("Passwords do not match.");
                    this.showError("Passwords do not match.");
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

    private void showError(String msg) {
        this.errorLabel.setVisible(true);
        this.errorLabel.setText(msg);
    }
}

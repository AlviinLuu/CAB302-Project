package com.example.cab302project;

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
    private Boolean isLogin = true;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button switchState;

    public final String loginText = "Welcome Back!";
    public final String registerText = "Welcome Aboard!";


    @FXML
    protected void onSwitchStateClick() {
        if (isLogin){
            introText.setText(loginText);
            switchState.setText("Register Instead");
        }else{
            introText.setText(registerText);
            switchState.setText("Login Instead");
        }
        isLogin = !isLogin;

    }

    @FXML
    protected void onSubmitButtonClick() {
        System.out.println("Hello");
        if (emailField.getText() != null){
            String email = emailField.getText();
        }
        if (passwordField.getText() != null){
            String password = passwordField.getText();
        }

    }
}
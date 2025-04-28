package com.example.cab302project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import jdk.jshell.spi.ExecutionControl;

import java.awt.color.ICC_ColorSpace;

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
    private PasswordField repeatPasswordField;
    @FXML
    private Button switchState;
    @FXML
    private TextField usernameField;
    @FXML
    private Label errorLabel;

    private String username;
    private String password;
    private String email;
    private String rptPassword;

    public final String loginText = "Welcome Back!";
    public final String registerText = "Welcome Aboard!";


    @FXML
    protected void onSwitchStateClick() {
        if (isLogin){
            introText.setText(loginText);
            switchState.setText("Register Instead");
            repeatPasswordField.setVisible(false);
            usernameField.setVisible(false);
        }else{
            introText.setText(registerText);
            switchState.setText("Login Instead");
            repeatPasswordField.setVisible(true);
            usernameField.setVisible(true);
        }
        isLogin = !isLogin;
    }

    @FXML
    protected void onSubmitButtonClick() {
        errorLabel.setVisible(false);
        //System.out.println("Hello");
        if (emailField.getText() != null || emailField.getText() != ""){
            email = emailField.getText();
        }else{
            //this is not working currently
            displayError("Please Enter Username");
        }

        if (passwordField.getText() != null) {
            password = passwordField.getText();
            System.out.println(password);
        }
        if (usernameField != null){
            username = usernameField.getText();
        }

        if ((repeatPasswordField.getText() != null)) {
            if (checkMatch(repeatPasswordField.getText(),passwordField.getText())){
                //continue
            }else{
                displayError("Passwords Do Not Match");
            }
            //possible bug - if text is entered on register screen it stays in the text field when login page is submitted
        }
    }

    private boolean checkMatch(String pw, String rptPW){
        return pw.equals(rptPW);
    }

    public void displayError(String msg){
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
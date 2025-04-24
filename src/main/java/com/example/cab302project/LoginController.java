package com.example.cab302project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import jdk.jshell.spi.ExecutionControl;

public class LoginController {
    @FXML
    private Label introText;
    private Boolean isLogin = true;

    public final String loginText = "Welcome Back!";
    public final String registerText = "Welcome Aboard!";


    @FXML
    protected void onLoginButtonClick() {
        if (isLogin){
            introText.setText(loginText);
        }else{
            introText.setText(registerText);
        }
        isLogin = !isLogin;

    }

    @FXML
    protected void onSubmitButtonClick() {
        System.out.println("Hello");
    }
}
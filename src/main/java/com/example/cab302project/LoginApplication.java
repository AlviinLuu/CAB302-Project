package com.example.cab302project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/// Launch this file to run the program. User input is stored in the username and password variables in LoginController
/// after the submit button is pushed and then a function can be called there to send things over to the database.
/// The isLogin bool shows if it was a login or sign up,
///
/// To do:
/// - add repeat password field in register screen
/// - check that email and password follow requirements
/// - make it all look a bit nicer

public class LoginApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginApplication.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
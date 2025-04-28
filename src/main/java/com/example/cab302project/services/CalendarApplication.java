package com.example.cab302project.services;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class CalendarApplication extends Application {
    public static final String TITLE = "Calendar Page";

    @Override
    public void start(Stage stage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/cab302project/calendar-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            stage.setTitle(TITLE);
            stage.setScene(scene);
            stage.setMaximized(true); // Keep window maximized
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();  // Print detailed stack trace if any error occurs
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

module com.example.cab302project {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;


    opens com.example.cab302project to javafx.fxml;
    exports com.example.cab302project;
    exports com.example.cab302project.controllers;
    exports com.example.cab302project.models;
    exports com.example.cab302project.services;
    opens com.example.cab302project.controllers to javafx.fxml;

}
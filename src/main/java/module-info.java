module com.example.cab302project {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.xerial.sqlitejdbc;
    requires jdk.jshell;
    requires com.google.api.services.calendar;
    requires com.google.api.client.auth;
    requires com.google.api.client;
    requires google.api.client;
    requires com.google.api.client.json.gson;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.extensions.java6.auth;


    opens com.example.cab302project to javafx.fxml;
    exports com.example.cab302project;
    exports com.example.cab302project.controllers;
    exports com.example.cab302project.models;
    exports com.example.cab302project.services;
    opens com.example.cab302project.controllers to javafx.fxml;

}
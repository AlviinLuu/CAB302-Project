module com.example.cab302project {
    requires javafx.controls;
    requires javafx.fxml;
    requires jdk.jshell;
    requires java.desktop;


    opens com.example.cab302project to javafx.fxml;
    exports com.example.cab302project;
}
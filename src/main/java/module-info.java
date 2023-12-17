module com.example.lab7v {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.lab7v3 to javafx.fxml;
    exports com.example.lab7v3;

    exports com.example.lab7v3.controllers;
    exports com.example.lab7v3.domain;

    opens com.example.lab7v3.controllers to javafx.fxml;
    opens com.example.lab7v3.domain to javafx.fxml;
}
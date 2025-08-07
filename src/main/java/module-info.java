module com.example.bar_management_application {
    // JavaFX
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // JDBC
    requires java.sql;

    // mở package cho JavaFX FXML truy cập
    opens com.example.bar_management_application to javafx.fxml;
    opens com.example.bar_management_application.controller to javafx.fxml;
    opens com.example.bar_management_application.model to javafx.base;

    // export các package để bên ngoài có thể sử dụng
    exports com.example.bar_management_application;
    exports com.example.bar_management_application.controller;
    exports com.example.bar_management_application.model;
}

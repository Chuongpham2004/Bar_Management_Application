package com.example.bar_management_application.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    @FXML
    private Label lblDateTime;

    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Hiển thị thời gian hiện tại
        lblDateTime.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }

    private void loadView(String fxmlFile) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource("/fxml/" + fxmlFile));
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleTablesClick() {
        loadView("TableView.fxml");
    }

    @FXML
    private void handleOrdersClick() {
        loadView("OrderView.fxml");
    }

    @FXML
    private void handleMenuClick() {
        loadView("MenuView.fxml");
    }

    @FXML
    private void handlePaymentsClick() {
        loadView("PaymentView.fxml");
    }

    @FXML
    private void handleReportsClick() {
        loadView("ReportView.fxml");
    }
}



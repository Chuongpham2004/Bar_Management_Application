package com.barmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class OrderController {

    @FXML
    private TableView<?> orderTable;

    @FXML
    private Button addItemButton;

    @FXML
    private Button paymentButton;

    @FXML
    private Button backButton;

    @FXML
    private void handleAddItem(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thêm món");
        alert.setHeaderText(null);
        alert.setContentText("Chức năng thêm món chưa được cài đặt.");
        alert.showAndWait();
    }

    @FXML
    private void handlePayment(ActionEvent event) {
        openScene("/fxml/payment.fxml", "Thanh toán");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        openScene("/fxml/table_management.fxml", "Quản lý bàn");
    }

    private void openScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


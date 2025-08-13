package com.barmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PaymentController {

    @FXML
    private Label totalLabel;

    @FXML
    private ComboBox<String> paymentMethodCombo;

    @FXML
    private Button confirmButton;

    @FXML
    private Button backButton;

    @FXML
    private void initialize() {
        paymentMethodCombo.getItems().addAll("Tiền mặt", "Thẻ", "Chuyển khoản");
    }

    @FXML
    private void handleConfirmPayment(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thanh toán");
        alert.setHeaderText(null);
        alert.setContentText("Thanh toán thành công!");
        alert.showAndWait();

        openScene("/fxml/dashboard.fxml", "Dashboard");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        openScene("/fxml/order.fxml", "Order");
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

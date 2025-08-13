package com.barmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TableManagementController {

    @FXML
    private Button table1Button;

    @FXML
    private Button table2Button;

    @FXML
    private Button backButton;

    @FXML
    private void handleSelectTable(ActionEvent event) {
        Button clicked = (Button) event.getSource();
        String tableName = clicked.getText();
        System.out.println("Đã chọn bàn: " + tableName);

        openScene("/fxml/order.fxml", "Order - " + tableName);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        openScene("/fxml/dashboard.fxml", "Dashboard");
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


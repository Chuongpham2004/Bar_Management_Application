package com.barmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Button manageTablesButton;

    @FXML
    private Button manageMenuButton;

    @FXML
    private Button revenueReportButton;

    @FXML
    private Button logoutButton;

    @FXML
    private void handleManageTables(ActionEvent event) {
        openScene("/fxml/table_management.fxml", "Quản lý bàn");
    }

    @FXML
    private void handleManageMenu(ActionEvent event) {
        openScene("/fxml/menu_management.fxml", "Quản lý thực đơn");
    }

    @FXML
    private void handleRevenueReport(ActionEvent event) {
        openScene("/fxml/revenue_report.fxml", "Báo cáo doanh thu");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        openScene("/fxml/login.fxml", "Đăng nhập");
    }

    private void openScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) manageTablesButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
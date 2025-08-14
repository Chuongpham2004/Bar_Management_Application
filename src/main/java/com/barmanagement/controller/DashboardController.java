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
        openScene(event, "/fxml/table_management.fxml", "Quản lý bàn");
    }

    @FXML
    private void handleManageMenu(ActionEvent event) {
        openScene(event, "/fxml/menu_management.fxml", "Quản lý thực đơn");
    }

    @FXML
    private void handleRevenueReport(ActionEvent event) {
        openScene(event, "/fxml/revenue_report.fxml", "Báo cáo doanh thu");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        openScene(event, "/fxml/login.fxml", "Đăng nhập");
    }

    private void openScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML private Button manageOrderButton;
    @FXML private void handleManageOrder(ActionEvent e){
        openScene(e, "/fxml/order_management.fxml", "Quản lý order");
    }

}
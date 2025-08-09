package com.barmanagement.controller;

import com.barmanagement.model.Staff;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class DashboardController {

    @FXML
    private Label lblWelcome;

    private Staff currentStaff;

    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        if (staff != null) {
            lblWelcome.setText("Xin chào, " + staff.getFullName() + " (" + staff.getRole() + ")");
        }
    }

    @FXML
    private void handleGoToTables(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/barmanagement/view/tablescreen.fxml"));
            Parent root = loader.load();

            // Truyền staff sang TableScreenController
            TableScreenController tableController = loader.getController();
            tableController.setCurrentStaff(currentStaff);

            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Chọn bàn - Quản lý quầy bar");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/barmanagement/view/login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lblWelcome.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Đăng nhập");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.barmanagement.controller;

import com.barmanagement.dao.StaffDAO;
import com.barmanagement.model.Staff;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    // Sự kiện khi nhấn nút "Đăng nhập"
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        Staff staff = StaffDAO.getInstance().login(username, password);

        if (staff != null) {
            try {
                // Load Dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/barmanagement/view/Dashboard.fxml"));
                Parent root = loader.load();

                // Truyền staff sang DashboardController
                DashboardController dashboardController = loader.getController();
                dashboardController.setCurrentStaff(staff);

                Stage stage = (Stage) txtUsername.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Dashboard - Quản lý quầy bar");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Không thể tải Dashboard.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Tên đăng nhập hoặc mật khẩu không chính xác.");
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

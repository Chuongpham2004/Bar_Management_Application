package com.barmanagement.controller;

import com.barmanagement.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    
    @FXML
    private TextField usernameField;
    
    @FXML
    private PasswordField passwordField;
    
    @FXML
    private Button loginButton;
    
    private AuthService authService;
    
    @FXML
    public void initialize() {
        authService = new AuthService();
        
        // Set default focus
        usernameField.requestFocus();
        
        // Add enter key handlers
        usernameField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập đầy đủ thông tin đăng nhập", Alert.AlertType.ERROR);
            return;
        }
        
        if (authService.login(username, password)) {
            try {
                // Load dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();
                
                DashboardController dashboardController = loader.getController();
                dashboardController.setCurrentUser(authService.getCurrentUser());
                
                Stage stage = (Stage) loginButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Bar Management - Dashboard");
                stage.show();
                
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Lỗi", "Không thể tải màn hình dashboard", Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Lỗi", "Tên đăng nhập hoặc mật khẩu không đúng", Alert.AlertType.ERROR);
            passwordField.clear();
            passwordField.requestFocus();
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
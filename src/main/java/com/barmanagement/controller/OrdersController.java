package com.barmanagement.controller;

import com.barmanagement.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class OrdersController {
    
    @FXML
    private Label welcomeLabel;
    
    @FXML
    private Button backButton;
    
    private User currentUser;
    
    @FXML
    public void initialize() {
        setupEventHandlers();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Quản lý Đơn hàng - " + user.getFullName());
    }
    
    private void setupEventHandlers() {
        backButton.setOnAction(e -> goBack());
    }
    
    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();
            
            DashboardController dashboardController = loader.getController();
            dashboardController.setCurrentUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Bar Management - Dashboard");
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

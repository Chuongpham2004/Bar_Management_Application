package com.barmanagement.util;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import java.io.IOException;

/**
 * Utility class for handling logout functionality across all controllers
 */
public class LogoutUtil {
    
    /**
     * Shows a confirmation dialog for logout and handles the logout process
     * @param sourceNode The node that triggered the logout (used to get the current stage)
     */
    public static void confirmLogout(Node sourceNode) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText("Bạn có chắc chắn muốn đăng xuất?");
        alert.setContentText("Bạn sẽ được chuyển về màn hình đăng nhập.");
        
        // Set button text
        ButtonType yesButton = new ButtonType("Có");
        ButtonType noButton = new ButtonType("Không");
        alert.getButtonTypes().setAll(yesButton, noButton);
        
        // Show dialog and wait for user response
        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                performLogout(sourceNode);
            }
        });
    }
    
    /**
     * Performs the actual logout by switching to login screen
     * @param sourceNode The node that triggered the logout
     */
    private static void performLogout(Node sourceNode) {
        try {
            // Get the current stage
            Stage currentStage = (Stage) sourceNode.getScene().getWindow();
            
            // Load login FXML
            FXMLLoader loader = new FXMLLoader(LogoutUtil.class.getResource("/fxml/login.fxml"));
            Scene loginScene = new Scene(loader.load());
            
            // Switch to login scene
            currentStage.setScene(loginScene);
            currentStage.setTitle("Đăng nhập - Bar Management System");
            currentStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: show error message
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Lỗi");
            errorAlert.setHeaderText("Không thể chuyển về màn hình đăng nhập");
            errorAlert.setContentText("Vui lòng thử lại hoặc khởi động lại ứng dụng.");
            errorAlert.showAndWait();
        }
    }
}


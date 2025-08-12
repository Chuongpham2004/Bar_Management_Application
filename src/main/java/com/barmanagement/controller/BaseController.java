package com.barmanagement.controller;

import com.barmanagement.model.Staff;
import com.barmanagement.util.SceneManager;
import com.barmanagement.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Base Controller - Common functionality for all controllers
 */
public abstract class BaseController {

    protected SceneManager sceneManager;
    protected SessionManager sessionManager;

    /**
     * Initialize base components (call from child initialize())
     */
    protected void initializeBase() {
        this.sceneManager = SceneManager.getInstance();
        this.sessionManager = SessionManager.getInstance();
    }

    /**
     * Get current logged in staff
     */
    protected Staff getCurrentStaff() {
        return sessionManager.getCurrentStaff();
    }

    /**
     * Check if user is logged in
     */
    protected boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    /**
     * Check if current user has admin role
     */
    protected boolean isAdmin() {
        Staff staff = getCurrentStaff();
        return staff != null && "admin".equalsIgnoreCase(staff.getRole());
    }

    /**
     * Check if current user has manager role or above
     */
    protected boolean isManagerOrAbove() {
        Staff staff = getCurrentStaff();
        return staff != null &&
                ("admin".equalsIgnoreCase(staff.getRole()) ||
                        "manager".equalsIgnoreCase(staff.getRole()));
    }

    /**
     * Navigation methods
     */
    @FXML
    protected void goToDashboard() {
        sceneManager.switchTo(SceneManager.DASHBOARD_SCREEN);
    }

    @FXML
    protected void goToTableSelection() {
        sceneManager.showTableSelection();
    }

    @FXML
    protected void goBack() {
        sceneManager.goBack();
    }

    @FXML
    protected void logout() {
        boolean confirm = showConfirmation(
                "Xác nhận đăng xuất",
                "Bạn có chắc chắn muốn đăng xuất?"
        );

        if (confirm) {
            sceneManager.logout();
        }
    }

    /**
     * Utility methods for alerts
     */
    protected void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait()
                .filter(response -> response == ButtonType.OK)
                .isPresent();
    }

    /**
     * Update user info display (override in child classes)
     */
    protected void updateUserDisplay() {
        // Override in child classes to update user info in UI
    }

    /**
     * Refresh data (override in child classes)
     */
    protected void refreshData() {
        // Override in child classes to refresh screen data
    }

    /**
     * Validate user permissions for action
     */
    protected boolean validatePermission(String action) {
        Staff staff = getCurrentStaff();

        if (staff == null) {
            showError("Lỗi", "Bạn chưa đăng nhập!");
            return false;
        }

        // Define permission rules
        switch (action.toLowerCase()) {
            case "admin_only":
                if (!isAdmin()) {
                    showError("Không có quyền", "Chỉ quản trị viên mới có thể thực hiện thao tác này!");
                    return false;
                }
                break;

            case "manager_above":
                if (!isManagerOrAbove()) {
                    showError("Không có quyền", "Bạn không có quyền thực hiện thao tác này!");
                    return false;
                }
                break;

            case "staff_above":
                // All logged in users can perform this action
                break;

            default:
                return true;
        }

        return true;
    }
}

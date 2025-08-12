package com.barmanagement.controller;

import com.barmanagement.model.Staff;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Dashboard Controller - Updated with SceneManager navigation
 */
public class DashboardController extends BaseController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblCurrentTime;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblTodayOrders;
    @FXML private Label lblActiveStaff;
    @FXML private Label lblAvailableTables;

    private Staff currentStaff;
    private Timeline clockTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize base controller
        initializeBase();

        // Setup clock
        setupClock();

        // Load dashboard data
        loadDashboardData();

        // Set current staff from session
        Staff sessionStaff = getCurrentStaff();
        if (sessionStaff != null) {
            setCurrentStaff(sessionStaff);
        }

        System.out.println("📊 Dashboard initialized");
    }

    /**
     * Set current staff and update display
     */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        updateUserDisplay();
    }

    @Override
    protected void updateUserDisplay() {
        if (currentStaff != null) {
            String welcomeMessage = String.format("Xin chào, %s (%s)",
                    currentStaff.getFullName(),
                    getRoleDisplayName(currentStaff.getRole()));
            lblWelcome.setText(welcomeMessage);

            System.out.println("👋 Welcome message updated for: " + currentStaff.getFullName());
        } else {
            lblWelcome.setText("Xin chào!");
        }
    }

    /**
     * Setup real-time clock
     */
    private void setupClock() {
        if (lblCurrentTime != null) {
            clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
                lblCurrentTime.setText(now.format(formatter));
            }));
            clockTimeline.setCycleCount(Animation.INDEFINITE);
            clockTimeline.play();
        }
    }

    /**
     * Load dashboard statistics
     */
    private void loadDashboardData() {
        // Simulate loading dashboard data
        // In real application, load from database

        if (lblTodayRevenue != null) {
            lblTodayRevenue.setText("15,500,000 VNĐ");
        }

        if (lblTodayOrders != null) {
            lblTodayOrders.setText("87 đơn");
        }

        if (lblActiveStaff != null) {
            lblActiveStaff.setText("12 người");
        }

        if (lblAvailableTables != null) {
            lblAvailableTables.setText("8/15 bàn");
        }

        System.out.println("📈 Dashboard data loaded");
    }

    /**
     * Navigate to table selection
     */
    @FXML
    private void handleGoToTables(ActionEvent event) {
        System.out.println("🪑 Navigating to table selection...");
        sceneManager.showTableSelection();
    }

    /**
     * Navigate to menu management
     */
    @FXML
    private void handleGoToMenu(ActionEvent event) {
        if (validatePermission("manager_above")) {
            System.out.println("🍸 Navigating to menu management...");
            // sceneManager.switchTo(SceneManager.MENU_SCREEN);
            showInfo("Thông báo", "Chức năng quản lý menu đang được phát triển!");
        }
    }

    /**
     * Navigate to staff management
     */
    @FXML
    private void handleGoToStaff(ActionEvent event) {
        if (validatePermission("admin_only")) {
            System.out.println("👥 Navigating to staff management...");
            // sceneManager.switchTo(SceneManager.STAFF_SCREEN);
            showInfo("Thông báo", "Chức năng quản lý nhân viên đang được phát triển!");
        }
    }

    /**
     * Navigate to inventory management
     */
    @FXML
    private void handleGoToInventory(ActionEvent event) {
        if (validatePermission("manager_above")) {
            System.out.println("📦 Navigating to inventory management...");
            // sceneManager.switchTo(SceneManager.INVENTORY_SCREEN);
            showInfo("Thông báo", "Chức năng quản lý kho đang được phát triển!");
        }
    }

    /**
     * Navigate to reports
     */
    @FXML
    private void handleGoToReports(ActionEvent event) {
        if (validatePermission("manager_above")) {
            System.out.println("📊 Navigating to reports...");
            // sceneManager.switchTo(SceneManager.REPORTS_SCREEN);
            showInfo("Thông báo", "Chức năng báo cáo đang được phát triển!");
        }
    }

    /**
     * Navigate to settings
     */
    @FXML
    private void handleGoToSettings(ActionEvent event) {
        System.out.println("⚙️ Navigating to settings...");
        // sceneManager.switchTo(SceneManager.SETTINGS_SCREEN);
        showInfo("Thông báo", "Chức năng cài đặt đang được phát triển!");
    }

    /**
     * Quick action - Create new order
     */
    @FXML
    private void handleQuickOrder(ActionEvent event) {
        System.out.println("🚀 Quick order action...");
        sceneManager.showTableSelection();
    }

    /**
     * Quick action - View today's orders
     */
    @FXML
    private void handleTodayOrders(ActionEvent event) {
        System.out.println("📋 Viewing today's orders...");
        showInfo("Đơn hàng hôm nay", "Có 87 đơn hàng trong ngày hôm nay.\n\n" +
                "• Hoàn thành: 65 đơn\n" +
                "• Đang xử lý: 15 đơn\n" +
                "• Đã hủy: 7 đơn");
    }

    /**
     * Quick action - Check low stock
     */
    @FXML
    private void handleLowStock(ActionEvent event) {
        System.out.println("⚠️ Checking low stock...");
        showWarning("Cảnh báo kho hàng", "Có 5 sản phẩm sắp hết hàng:\n\n" +
                "• Bia Tiger: 10 chai\n" +
                "• Whisky Jack Daniel's: 2 chai\n" +
                "• Vodka Smirnoff: 5 chai\n" +
                "• Cocktail Mix: 3 lít\n" +
                "• Nước ngọt Coca: 20 lon");
    }

    /**
     * Logout with confirmation
     */
    @FXML
    private void handleLogout(ActionEvent event) {
        System.out.println("🔓 Logout requested...");

        boolean confirm = showConfirmation(
                "Xác nhận đăng xuất",
                "Bạn có chắc chắn muốn đăng xuất?\n\n" +
                        "Phiên làm việc hiện tại sẽ kết thúc."
        );

        if (confirm) {
            // Stop clock
            if (clockTimeline != null) {
                clockTimeline.stop();
            }

            // Logout through scene manager
            sceneManager.logout();

            System.out.println("👋 Logged out successfully");
        }
    }

    /**
     * Refresh dashboard data
     */
    @FXML
    private void handleRefresh(ActionEvent event) {
        System.out.println("🔄 Refreshing dashboard...");
        refreshData();
        showInfo("Làm mới", "Dữ liệu dashboard đã được cập nhật!");
    }

    @Override
    protected void refreshData() {
        loadDashboardData();
        updateUserDisplay();
        System.out.println("🔄 Dashboard data refreshed");
    }

    /**
     * Get display name for role
     */
    private String getRoleDisplayName(String role) {
        if (role == null) return "N/A";

        switch (role.toLowerCase()) {
            case "admin":
                return "Quản trị viên";
            case "manager":
                return "Quản lý";
            case "staff":
                return "Nhân viên";
            default:
                return role;
        }
    }

    /**
     * Show user profile info
     */
    @FXML
    private void handleUserProfile(ActionEvent event) {
        if (currentStaff != null) {
            String profileInfo = String.format(
                    "Thông tin tài khoản:\n\n" +
                            "Họ tên: %s\n" +
                            "Vị trí: %s\n" +
                            "Vai trò: %s\n" +
                            "Email: %s\n" +
                            "Số điện thoại: %s",
                    currentStaff.getFullName(),
                    currentStaff.getPosition() != null ? currentStaff.getPosition() : "N/A",
                    getRoleDisplayName(currentStaff.getRole()),
                    currentStaff.getEmail() != null ? currentStaff.getEmail() : "N/A",
                    currentStaff.getPhone() != null ? currentStaff.getPhone() : "N/A"
            );

            showInfo("Hồ sơ cá nhân", profileInfo);
        }
    }

    /**
     * Cleanup when controller is destroyed
     */
    public void cleanup() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        System.out.println("🧹 Dashboard cleanup completed");
    }
}
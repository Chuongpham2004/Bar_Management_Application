package com.barmanagement.controller;

import com.barmanagement.model.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML private Button manageTablesButton;
    @FXML private Button manageMenuButton;
    @FXML private Button revenueReportButton;
    @FXML private Button logoutButton;
    @FXML private Button manageOrderButton;
    @FXML private Button manageUsersButton; // Thêm button quản lý users

    // Dashboard specific elements
    @FXML private Label currentTimeLabel;
    @FXML private Label welcomeTimeLabel;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblTodayOrders;
    @FXML private Label lblActiveTables;
    @FXML private Label lblMenuItems;

    // User info labels
    @FXML private Label welcomeUserLabel;
    @FXML private Label userRoleLabel;

    // Admin panel
    @FXML private VBox adminPanel;

    // Charts
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> ordersChart;
    @FXML private CategoryAxis revenueXAxis;
    @FXML private NumberAxis revenueYAxis;
    @FXML private CategoryAxis ordersXAxis;
    @FXML private NumberAxis ordersYAxis;

    // Current user
    private User currentUser;

    @FXML
    public void initialize() {
        // Initialize time display
        updateTimeDisplay();

        // Start clock update timer
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimeDisplay()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Initialize dashboard data
        loadDashboardData();
        initializeCharts();

        // Hide admin panel initially
        if (adminPanel != null) {
            adminPanel.setVisible(false);
            adminPanel.setManaged(false);
        }
    }

    /**
     * Set current user và cập nhật UI theo quyền
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInterface();
    }

    /**
     * Cập nhật giao diện theo user hiện tại
     */
    private void updateUserInterface() {
        if (currentUser != null) {
            // Cập nhật welcome message
            if (welcomeUserLabel != null) {
                welcomeUserLabel.setText("Xin chào, " + currentUser.getFullName() + "!");
            }

            // Hiển thị role
            if (userRoleLabel != null) {
                userRoleLabel.setText("Vai trò: " + getRoleDisplayName(currentUser.getRole()));
            }

            // Cấu hình quyền truy cập
            setupPermissions();
        }
    }

    /**
     * Cấu hình quyền truy cập theo role
     */
    private void setupPermissions() {
        if (currentUser == null) return;

        boolean isAdmin = currentUser.isAdmin();
        boolean isManager = currentUser.isManager();

        // Admin panel chỉ hiển thị cho admin
        if (adminPanel != null) {
            adminPanel.setVisible(isAdmin);
            adminPanel.setManaged(isAdmin);
        }

        // Manage Users button chỉ cho admin
        if (manageUsersButton != null) {
            manageUsersButton.setVisible(isAdmin);
            manageUsersButton.setManaged(isAdmin);
        }

        // Revenue report cho admin và manager
        if (revenueReportButton != null) {
            revenueReportButton.setVisible(isAdmin || isManager);
            revenueReportButton.setManaged(isAdmin || isManager);
        }

        // Các quyền khác có thể thêm ở đây
    }

    /**
     * Chuyển đổi role code thành tên hiển thị
     */
    private String getRoleDisplayName(String role) {
        switch (role.toLowerCase()) {
            case "admin": return "Quản trị viên";
            case "manager": return "Quản lý";
            case "employee": return "Nhân viên";
            default: return role;
        }
    }

    private void updateTimeDisplay() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (currentTimeLabel != null) {
            currentTimeLabel.setText(now.format(timeFormatter));
        }

        if (welcomeTimeLabel != null) {
            welcomeTimeLabel.setText(now.format(dateFormatter));
        }
    }

    private void loadDashboardData() {
        // Sample data - replace with actual database queries
        if (lblTodayRevenue != null) lblTodayRevenue.setText("2,500,000 VNĐ");
        if (lblTodayOrders != null) lblTodayOrders.setText("45");
        if (lblActiveTables != null) lblActiveTables.setText("8/15");
        if (lblMenuItems != null) lblMenuItems.setText("32");
    }

    private void initializeCharts() {
        if (revenueChart != null) {
            XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
            revenueSeries.setName("Doanh thu (triệu VNĐ)");

            revenueSeries.getData().add(new XYChart.Data<>("T2", 1.5));
            revenueSeries.getData().add(new XYChart.Data<>("T3", 2.1));
            revenueSeries.getData().add(new XYChart.Data<>("T4", 1.8));
            revenueSeries.getData().add(new XYChart.Data<>("T5", 2.5));
            revenueSeries.getData().add(new XYChart.Data<>("T6", 3.2));
            revenueSeries.getData().add(new XYChart.Data<>("T7", 4.1));
            revenueSeries.getData().add(new XYChart.Data<>("CN", 3.8));

            revenueChart.getData().add(revenueSeries);
        }

        if (ordersChart != null) {
            XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
            ordersSeries.setName("Số đơn hàng");

            ordersSeries.getData().add(new XYChart.Data<>("T2", 25));
            ordersSeries.getData().add(new XYChart.Data<>("T3", 32));
            ordersSeries.getData().add(new XYChart.Data<>("T4", 28));
            ordersSeries.getData().add(new XYChart.Data<>("T5", 38));
            ordersSeries.getData().add(new XYChart.Data<>("T6", 45));
            ordersSeries.getData().add(new XYChart.Data<>("T7", 52));
            ordersSeries.getData().add(new XYChart.Data<>("CN", 48));

            ordersChart.getData().add(ordersSeries);
        }
    }

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
        // Kiểm tra quyền trước khi mở
        if (!hasPermission("revenue_report")) {
            showPermissionDeniedAlert();
            return;
        }
        openScene(event, "/fxml/revenue_report.fxml", "Báo cáo doanh thu");
    }

    @FXML
    private void handleManageUsers(ActionEvent event) {
        // Chỉ admin mới được truy cập
        if (!currentUser.isAdmin()) {
            showPermissionDeniedAlert();
            return;
        }
        openScene(event, "/fxml/user_management.fxml", "Quản lý người dùng");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Hiển thị confirm dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc muốn đăng xuất?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Log thông tin đăng xuất
                System.out.println("User " + (currentUser != null ? currentUser.getUsername() : "Unknown") + " logged out");

                // Quay về màn hình login
                openScene(event, "/fxml/login.fxml", "Đăng nhập");
            }
        });
    }

    @FXML
    private void handleManageOrder(ActionEvent event) {
        openScene(event, "/fxml/order_management.fxml", "Quản lý order");
    }

    @FXML
    private void handleChangePassword(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/change_password.fxml"));
            Scene scene = new Scene(loader.load());

            // Truyền current user sang ChangePasswordController
            Object controller = loader.getController();
            if (controller instanceof ChangePasswordController) {
                ((ChangePasswordController) controller).setCurrentUser(currentUser);
            }

            Stage stage = new Stage();
            stage.setTitle("Đổi mật khẩu");
            stage.setScene(scene);
            stage.initOwner(((Button) event.getSource()).getScene().getWindow());
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể mở form đổi mật khẩu.");
        }
    }

    // Navigation methods for dashboard
    @FXML
    private void showHome() {
        // Already on dashboard
    }

    /**
     * Kiểm tra quyền truy cập
     */
    private boolean hasPermission(String permission) {
        if (currentUser == null) return false;

        switch (permission) {
            case "revenue_report":
                return currentUser.isAdmin() || currentUser.isManager();
            case "user_management":
                return currentUser.isAdmin();
            case "admin_panel":
                return currentUser.isAdmin();
            default:
                return true; // Mặc định cho phép
        }
    }

    /**
     * Hiển thị thông báo không có quyền
     */
    private void showPermissionDeniedAlert() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Không có quyền truy cập");
        alert.setHeaderText(null);
        alert.setContentText("Bạn không có quyền truy cập chức năng này.");
        alert.showAndWait();
    }

    /**
     * Hiển thị thông báo lỗi
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Truyền currentUser sang controller mới nếu có method setCurrentUser
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    // Sử dụng reflection để gọi setCurrentUser nếu có
                    controller.getClass().getMethod("setCurrentUser", User.class).invoke(controller, currentUser);
                } catch (Exception e) {
                    // Controller không có method setCurrentUser, bỏ qua
                }
            }

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Không thể mở " + title);
        }
    }

    // Getters
    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public boolean isCurrentUserManager() {
        return currentUser != null && currentUser.isManager();
    }
}
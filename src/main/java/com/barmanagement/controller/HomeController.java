package com.barmanagement.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HomeController implements Initializable {

    // FXML Elements - Navigation Steps
    @FXML private HBox dashboardStep;
    @FXML private HBox tableStep;
    @FXML private HBox orderStep;
    @FXML private HBox paymentStep;

    // FXML Elements - User Info
    @FXML private Label currentTimeLabel;
    @FXML private Label staffNameLabel;

    // FXML Elements - Status Labels
    @FXML private Label availableTablesLabel;
    @FXML private Label occupiedTablesLabel;
    @FXML private Label activeOrdersLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label todayRevenueLabel;
    @FXML private Label activeStaffLabel;

    // FXML Elements - Live Updates
    @FXML private Circle liveIndicator;
    @FXML private VBox liveUpdatesContainer;

    // Timeline for animations and updates
    private Timeline clockTimeline;
    private Timeline liveIndicatorTimeline;
    private ScheduledExecutorService updateScheduler;

    // Sample data (in real application, this would come from database/service)
    private int availableTables = 12;
    private int occupiedTables = 8;
    private int activeOrders = 15;
    private int pendingOrders = 3;
    private double todayRevenue = 12500000.0;
    private int activeStaff = 8;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupClock();
        setupLiveIndicator();
        setupStatusUpdates();
        updateAllStatus();
        startLiveUpdates();
    }

    /**
     * Setup real-time clock
     */
    private void setupClock() {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            String currentTime = LocalDateTime.now().format(timeFormatter);
            currentTimeLabel.setText(currentTime);
        }));
        clockTimeline.setCycleCount(Animation.INDEFINITE);
        clockTimeline.play();
    }

    /**
     * Setup live indicator animation
     */
    private void setupLiveIndicator() {
        liveIndicatorTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> liveIndicator.setFill(Color.web("#4CAF50"))),
                new KeyFrame(Duration.seconds(0.5), e -> liveIndicator.setFill(Color.web("#81C784"))),
                new KeyFrame(Duration.seconds(1), e -> liveIndicator.setFill(Color.web("#4CAF50")))
        );
        liveIndicatorTimeline.setCycleCount(Animation.INDEFINITE);
        liveIndicatorTimeline.play();
    }

    /**
     * Setup periodic status updates
     */
    private void setupStatusUpdates() {
        updateScheduler = Executors.newScheduledThreadPool(1);
        updateScheduler.scheduleAtFixedRate(this::updateAllStatus, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * Update all status displays
     */
    private void updateAllStatus() {
        Platform.runLater(() -> {
            // Update table status
            availableTablesLabel.setText(availableTables + " bàn trống");
            occupiedTablesLabel.setText(occupiedTables + " bàn đang sử dụng");

            // Update order status
            activeOrdersLabel.setText(activeOrders + " đơn đang xử lý");
            pendingOrdersLabel.setText(pendingOrders + " đơn chờ phục vụ");

            // Update revenue (format Vietnamese currency)
            String formattedRevenue = String.format("%,.0fđ", todayRevenue);
            todayRevenueLabel.setText(formattedRevenue);

            // Update staff status
            activeStaffLabel.setText(activeStaff + " người đang làm việc");
        });
    }

    /**
     * Start live updates simulation
     */
    private void startLiveUpdates() {
        updateScheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(this::addRandomLiveUpdate);
        }, 5, 15, TimeUnit.SECONDS);
    }

    /**
     * Add random live update to the feed
     */
    private void addRandomLiveUpdate() {
        String[] sampleUpdates = {
                "Bàn " + (int)(Math.random() * 20 + 1) + " vừa đặt thêm đồ uống",
                "Bàn VIP " + (int)(Math.random() * 5 + 1) + " yêu cầu thanh toán",
                "Bàn " + (int)(Math.random() * 20 + 1) + " vừa được dọn dẹp xong",
                "Đơn hàng mới từ bàn " + (int)(Math.random() * 20 + 1),
                "Nhân viên " + (int)(Math.random() * 8 + 1) + " đã hoàn thành phục vụ"
        };

        String update = sampleUpdates[(int)(Math.random() * sampleUpdates.length)];
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

        addLiveUpdate(currentTime, update);
    }

    /**
     * Add live update to the container
     */
    private void addLiveUpdate(String time, String message) {
        HBox updateItem = new HBox();
        updateItem.setSpacing(15);
        updateItem.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Circle indicator = new Circle(4);
        indicator.setFill(Color.web("#4CAF50"));

        Label timeLabel = new Label(time);
        timeLabel.setTextFill(Color.web("#718096"));
        timeLabel.setStyle("-fx-font-size: 11px;");

        Label messageLabel = new Label(message);
        messageLabel.setTextFill(Color.web("#4A5568"));
        messageLabel.setStyle("-fx-font-size: 12px;");

        updateItem.getChildren().addAll(indicator, timeLabel, messageLabel);

        // Add to top and limit to 5 items
        liveUpdatesContainer.getChildren().add(0, updateItem);
        if (liveUpdatesContainer.getChildren().size() > 5) {
            liveUpdatesContainer.getChildren().remove(5);
        }
    }

    // Navigation Methods
    @FXML
    private void goToDashboard() {
        try {
            navigateToScene("/fxml/dashboard.fxml", "Dashboard - BarFlow");
        } catch (IOException e) {
            showErrorAlert("Lỗi điều hướng", "Không thể mở trang Dashboard");
        }
    }

    @FXML
    private void goToTables() {
        try {
            navigateToScene("/fxml/tables.fxml", "Quản lý bàn - BarFlow");
        } catch (IOException e) {
            showErrorAlert("Lỗi điều hướng", "Không thể mở trang Quản lý bàn");
        }
    }

    @FXML
    private void goToOrders() {
        try {
            navigateToScene("/fxml/orders.fxml", "Gọi món - BarFlow");
        } catch (IOException e) {
            showErrorAlert("Lỗi điều hướng", "Không thể mở trang Gọi món");
        }
    }

    @FXML
    private void goToPayment() {
        try {
            navigateToScene("/fxml/payment.fxml", "Thanh toán - BarFlow");
        } catch (IOException e) {
            showErrorAlert("Lỗi điều hướng", "Không thể mở trang Thanh toán");
        }
    }

    // Quick Action Methods
    @FXML
    private void createNewOrder() {
        showInfoAlert("Tạo đơn mới", "Chuyển đến trang tạo đơn hàng mới...");
        goToOrders();
    }

    @FXML
    private void findAvailableTable() {
        showInfoAlert("Tìm bàn trống",
                "Hiện có " + availableTables + " bàn trống sẵn sàng phục vụ!");
        goToTables();
    }

    @FXML
    private void quickPayment() {
        showInfoAlert("Thanh toán nhanh", "Chuyển đến trang thanh toán...");
        goToPayment();
    }

    @FXML
    private void viewTodayReport() {
        String reportInfo = String.format(
                "📊 BÁO CÁO HÔM NAY\n\n" +
                        "💰 Doanh thu: %,.0fđ\n" +
                        "🪑 Bàn phục vụ: %d/%d\n" +
                        "📋 Đơn hàng: %d đang xử lý\n" +
                        "👥 Nhân viên: %d người",
                todayRevenue, occupiedTables, (availableTables + occupiedTables),
                activeOrders, activeStaff
        );
        showInfoAlert("Báo cáo hôm nay", reportInfo);
    }

    @FXML
    private void emergencyMode() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("🚨 Chế độ khẩn cấp");
        alert.setHeaderText("Kích hoạt chế độ khẩn cấp");
        alert.setContentText("Bạn có chắc chắn muốn kích hoạt chế độ khẩn cấp?\n" +
                "Điều này sẽ thông báo cho tất cả nhân viên và quản lý.");
        alert.showAndWait();
    }

    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Đăng xuất");
        alert.setHeaderText("Xác nhận đăng xuất");
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống?");

        alert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                cleanup();
                try {
                    navigateToScene("/fxml/login.fxml", "Đăng nhập - BarFlow");
                } catch (IOException e) {
                    showErrorAlert("Lỗi", "Không thể chuyển đến trang đăng nhập");
                }
            }
        });
    }

    /**
     * Navigate to a new scene
     */
    private void navigateToScene(String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) currentTimeLabel.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    /**
     * Show information alert
     */
    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show error alert
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Đã xảy ra lỗi");
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Cleanup resources when controller is destroyed
     */
    public void cleanup() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        if (liveIndicatorTimeline != null) {
            liveIndicatorTimeline.stop();
        }
        if (updateScheduler != null && !updateScheduler.isShutdown()) {
            updateScheduler.shutdown();
        }
    }

    // Getters and Setters for external access
    public void setStaffName(String staffName) {
        if (staffNameLabel != null) {
            staffNameLabel.setText(staffName);
        }
    }

    public void updateTableStatus(int available, int occupied) {
        this.availableTables = available;
        this.occupiedTables = occupied;
        updateAllStatus();
    }

    public void updateOrderStatus(int active, int pending) {
        this.activeOrders = active;
        this.pendingOrders = pending;
        updateAllStatus();
    }

    public void updateRevenue(double revenue) {
        this.todayRevenue = revenue;
        updateAllStatus();
    }

    public void updateStaffCount(int count) {
        this.activeStaff = count;
        updateAllStatus();
    }
}
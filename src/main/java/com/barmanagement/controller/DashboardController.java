package com.barmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.Node;
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
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // Navigation buttons
    @FXML private Button dashboardBtn;
    @FXML private Button manageTablesButton;
    @FXML private Button manageMenuButton;
    @FXML private Button managePaymentButton;
    @FXML private Button revenueReportButton;
    @FXML private Button logoutButton;
    @FXML private Button manageOrderButton;

    // Dashboard specific elements
    @FXML private Label currentTimeLabel;
    @FXML private Label welcomeTimeLabel;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblTodayOrders;
    @FXML private Label lblActiveTables;
    @FXML private Label lblMenuItems;

    // Charts
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> ordersChart;
    @FXML private CategoryAxis revenueXAxis;
    @FXML private NumberAxis revenueYAxis;
    @FXML private CategoryAxis ordersXAxis;
    @FXML private NumberAxis ordersYAxis;

    // Timeline for clock updates
    private Timeline clockTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load CSS styles for navigation hover effects
        loadNavigationStyles();

        // Initialize navigation button states
        initializeNavigationStates();

        // Initialize time display
        updateTimeDisplay();

        // Start clock update timer
        startClockTimer();

        // Initialize dashboard data
        loadDashboardData();

        // Initialize charts
        initializeCharts();

        // Setup chart styling
        styleCharts();
    }

    /**
     * Load CSS styles for navigation hover effects
     */
    private void loadNavigationStyles() {
        try {
            // Wait for scene to be available
            if (dashboardBtn != null && dashboardBtn.getScene() != null) {
                String css = getClass().getResource("/css/navigation-styles.css").toExternalForm();
                dashboardBtn.getScene().getStylesheets().add(css);
                System.out.println("Navigation styles loaded successfully");
            } else {
                // If scene is not ready, try again after a short delay
                Timeline delayedLoader = new Timeline(new KeyFrame(Duration.millis(100), e -> {
                    try {
                        if (dashboardBtn != null && dashboardBtn.getScene() != null) {
                            String css = getClass().getResource("/css/navigation-styles.css").toExternalForm();
                            dashboardBtn.getScene().getStylesheets().add(css);
                            System.out.println("Navigation styles loaded successfully (delayed)");
                        }
                    } catch (Exception ex) {
                        System.err.println("Could not load navigation styles: " + ex.getMessage());
                    }
                }));
                delayedLoader.play();
            }
        } catch (Exception e) {
            System.err.println("Could not load navigation styles: " + e.getMessage());
        }
    }

    /**
     * Initialize navigation button states and styles
     */
    private void initializeNavigationStates() {
        // Set dashboard button as active since we're on dashboard
        if (dashboardBtn != null) {
            dashboardBtn.getStyleClass().removeAll("nav-button");
            dashboardBtn.getStyleClass().add("nav-button-active");
        }

        // Add hover style classes to other navigation buttons
        if (managePaymentButton != null) {
            managePaymentButton.getStyleClass().addAll("nav-button", "nav-payment");
        }
        if (manageMenuButton != null) {
            manageMenuButton.getStyleClass().addAll("nav-button", "nav-menu");
        }
        if (manageOrderButton != null) {
            manageOrderButton.getStyleClass().addAll("nav-button", "nav-order");
        }
        if (manageTablesButton != null) {
            manageTablesButton.getStyleClass().addAll("nav-button", "nav-tables");
        }
    }

    /**
     * Start the clock timer for real-time updates
     */
    private void startClockTimer() {
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimeDisplay()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    /**
     * Update time display labels
     */
    private void updateTimeDisplay() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (currentTimeLabel != null) {
            currentTimeLabel.setText(now.format(timeFormatter));
        }

        if (welcomeTimeLabel != null) {
            welcomeTimeLabel.setText("Hôm nay - " + now.format(dateFormatter));
        }
    }

    /**
     * Load dashboard data from database or service
     * TODO: Replace with actual database queries
     */
    private void loadDashboardData() {
        try {
            // Sample data - replace with actual database queries
            if (lblTodayRevenue != null) {
                lblTodayRevenue.setText("2,500,000 VNĐ");
            }
            if (lblTodayOrders != null) {
                lblTodayOrders.setText("45");
            }
            if (lblActiveTables != null) {
                lblActiveTables.setText("8/15");
            }
            if (lblMenuItems != null) {
                lblMenuItems.setText("32");
            }
        } catch (Exception e) {
            System.err.println("Error loading dashboard data: " + e.getMessage());
        }
    }

    /**
     * Initialize charts with sample data
     */
    private void initializeCharts() {
        initializeRevenueChart();
        initializeOrdersChart();
    }

    /**
     * Initialize revenue chart
     */
    private void initializeRevenueChart() {
        if (revenueChart != null) {
            try {
                XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
                revenueSeries.setName("Doanh thu (triệu VNĐ)");

                // Sample data for the last 7 days
                revenueSeries.getData().add(new XYChart.Data<>("T2", 1.5));
                revenueSeries.getData().add(new XYChart.Data<>("T3", 2.1));
                revenueSeries.getData().add(new XYChart.Data<>("T4", 1.8));
                revenueSeries.getData().add(new XYChart.Data<>("T5", 2.5));
                revenueSeries.getData().add(new XYChart.Data<>("T6", 3.2));
                revenueSeries.getData().add(new XYChart.Data<>("T7", 4.1));
                revenueSeries.getData().add(new XYChart.Data<>("CN", 3.8));

                revenueChart.getData().add(revenueSeries);
                revenueChart.setTitle("Doanh Thu 7 Ngày Gần Đây");
            } catch (Exception e) {
                System.err.println("Error initializing revenue chart: " + e.getMessage());
            }
        }
    }

    /**
     * Initialize orders chart
     */
    private void initializeOrdersChart() {
        if (ordersChart != null) {
            try {
                XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
                ordersSeries.setName("Số đơn hàng");

                // Sample data for the last 7 days
                ordersSeries.getData().add(new XYChart.Data<>("T2", 25));
                ordersSeries.getData().add(new XYChart.Data<>("T3", 32));
                ordersSeries.getData().add(new XYChart.Data<>("T4", 28));
                ordersSeries.getData().add(new XYChart.Data<>("T5", 38));
                ordersSeries.getData().add(new XYChart.Data<>("T6", 45));
                ordersSeries.getData().add(new XYChart.Data<>("T7", 52));
                ordersSeries.getData().add(new XYChart.Data<>("CN", 48));

                ordersChart.getData().add(ordersSeries);
                ordersChart.setTitle("Số Đơn Hàng 7 Ngày Gần Đây");
            } catch (Exception e) {
                System.err.println("Error initializing orders chart: " + e.getMessage());
            }
        }
    }

    /**
     * Apply custom styling to charts
     */
    private void styleCharts() {
        if (revenueChart != null) {
            revenueChart.setStyle("-fx-background-color: transparent;");
        }
        if (ordersChart != null) {
            ordersChart.setStyle("-fx-background-color: transparent;");
        }
    }

    // =========================== Navigation Event Handlers ===========================

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
    private void handlePayment(ActionEvent event) {
        openScene(event, "/fxml/payment.fxml", "Thanh toán");
    }

    @FXML
    private void handleManageOrder(ActionEvent event) {
        openScene(event, "/fxml/order_management.fxml", "Quản lý order");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // Stop the clock timer before logging out
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        openScene(event, "/fxml/login.fxml", "Đăng nhập");
    }

    @FXML
    private void showHome() {
        // Already on dashboard - maybe refresh data?
        loadDashboardData();
    }

    // =========================== Utility Methods ===========================

    /**
     * Open a new scene with error handling
     */
    private void openScene(ActionEvent event, String fxmlPath, String title) {
        try {
            // Stop the clock timer when leaving dashboard
            if (clockTimeline != null) {
                clockTimeline.stop();
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());

            // Load CSS for the new scene if needed
            String css = getClass().getResource("/css/navigation-styles.css").toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            System.err.println("Error opening scene " + fxmlPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refresh dashboard data
     */
    public void refreshDashboard() {
        loadDashboardData();
        // Could also refresh charts here
    }

    /**
     * Clean up resources when controller is destroyed
     */
    public void cleanup() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
    }
}
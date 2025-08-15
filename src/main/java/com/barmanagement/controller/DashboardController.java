package com.barmanagement.controller;

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
import com.barmanagement.util.SceneUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DashboardController {

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
        openScene(event, "/fxml/revenue_report.fxml", "Báo cáo doanh thu");
    }

    @FXML
    private void handlePayment(ActionEvent event) {
        openScene(event, "/fxml/payment.fxml", "Thanh toán");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        openScene(event, "/fxml/login.fxml", "Đăng nhập");
    }

    @FXML
    private void handleManageOrder(ActionEvent event) {
        openScene(event, "/fxml/order_management.fxml", "Quản lý order");
    }

    // Navigation methods for dashboard
    @FXML
    private void showHome() {
        // Already on dashboard
    }

    private void openScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
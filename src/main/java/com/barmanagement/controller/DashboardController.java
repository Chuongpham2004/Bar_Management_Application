package com.barmanagement.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;
import com.barmanagement.dao.RevenueDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.dao.MenuItemDAO;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

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

    // DAOs
    private RevenueDAO revenueDAO;
    private TableDAO tableDAO;
    private MenuItemDAO menuItemDAO;

    // Formatter
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        // Initialize DAOs
        revenueDAO = new RevenueDAO();
        tableDAO = new TableDAO();
        menuItemDAO = new MenuItemDAO();

        // Setup formatter
        currencyFormatter.setMaximumFractionDigits(0);

        // Initialize time display
        updateTimeDisplay();

        // Start clock update timer (every second)
        Timeline clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimeDisplay()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        // Initialize dashboard data
        loadDashboardData();
        initializeCharts();

        // Start data refresh timer (every 30 seconds)
        Timeline dataTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> refreshDashboardData()));
        dataTimeline.setCycleCount(Timeline.INDEFINITE);
        dataTimeline.play();
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
        Platform.runLater(() -> {
            try {
                // Load today's revenue
                BigDecimal todayRevenue = revenueDAO.getTodayRevenue();
                if (lblTodayRevenue != null) {
                    lblTodayRevenue.setText(formatCurrency(todayRevenue.doubleValue()));
                }

                // Load today's orders count
                int todayOrders = revenueDAO.getTodayOrders();
                if (lblTodayOrders != null) {
                    lblTodayOrders.setText(String.valueOf(todayOrders));
                }

                // Load table status
                loadTableStatus();

                // Load menu items count
                loadMenuItemsCount();

            } catch (SQLException e) {
                e.printStackTrace();
                showErrorMessage("Không thể tải dữ liệu dashboard: " + e.getMessage());
            }
        });
    }

    private void loadTableStatus() throws SQLException {
        var tables = tableDAO.findAll();
        long activeTables = tables.stream()
                .filter(t -> "occupied".equals(t.getStatus()) || "ordering".equals(t.getStatus()))
                .count();

        if (lblActiveTables != null) {
            lblActiveTables.setText(activeTables + "/" + tables.size());
        }
    }

    private void loadMenuItemsCount() throws SQLException {
        var menuItems = menuItemDAO.findAll();
        if (lblMenuItems != null) {
            lblMenuItems.setText(String.valueOf(menuItems.size()));
        }
    }

    private void refreshDashboardData() {
        loadDashboardData();
        updateCharts();
    }

    private void initializeCharts() {
        updateCharts();
    }

    private void updateCharts() {
        Platform.runLater(() -> {
            try {
                updateRevenueChart();
                updateOrdersChart();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateRevenueChart() throws SQLException {
        if (revenueChart == null) return;

        revenueChart.getData().clear();

        Map<String, BigDecimal> weeklyRevenue = revenueDAO.getWeeklyRevenue();

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Doanh thu (VNĐ)");

        // Convert dates to day names and add data
        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        int dayIndex = 0;

        for (Map.Entry<String, BigDecimal> entry : weeklyRevenue.entrySet()) {
            String dayName = dayIndex < dayNames.length ? dayNames[dayIndex] : entry.getKey();
            double amount = entry.getValue().doubleValue() / 1000000; // Convert to millions
            revenueSeries.getData().add(new XYChart.Data<>(dayName, amount));
            dayIndex++;
        }

        // Fill missing days with zero if needed
        while (dayIndex < 7) {
            revenueSeries.getData().add(new XYChart.Data<>(dayNames[dayIndex], 0));
            dayIndex++;
        }

        revenueChart.getData().add(revenueSeries);
    }

    private void updateOrdersChart() throws SQLException {
        if (ordersChart == null) return;

        ordersChart.getData().clear();

        Map<String, Integer> weeklyOrders = revenueDAO.getWeeklyOrders();

        XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Số đơn hàng");

        // Convert dates to day names and add data
        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        int dayIndex = 0;

        for (Map.Entry<String, Integer> entry : weeklyOrders.entrySet()) {
            String dayName = dayIndex < dayNames.length ? dayNames[dayIndex] : entry.getKey();
            int orders = entry.getValue();
            ordersSeries.getData().add(new XYChart.Data<>(dayName, orders));
            dayIndex++;
        }

        // Fill missing days with zero if needed
        while (dayIndex < 7) {
            ordersSeries.getData().add(new XYChart.Data<>(dayNames[dayIndex], 0));
            dayIndex++;
        }

        ordersChart.getData().add(ordersSeries);
    }

    private String formatCurrency(double amount) {
        return currencyFormatter.format(amount) + " VNĐ";
    }

    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Lỗi tải dữ liệu");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // ===== EVENT HANDLERS =====

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
        LogoutUtil.confirmLogout(logoutButton);
    }

    @FXML
    private void handleManageOrder(ActionEvent event) {
        openScene(event, "/fxml/order_management.fxml", "Quản lý order");
    }

    // Navigation methods for dashboard
    @FXML
    private void showHome() {
        // Already on dashboard - refresh data
        refreshDashboardData();
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
            showErrorMessage("Không thể mở " + title + ": " + e.getMessage());
        }
    }
}
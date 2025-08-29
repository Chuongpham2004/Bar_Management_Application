package com.barmanagement.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;
import com.barmanagement.util.DashboardUpdateUtil;
import com.barmanagement.dao.RevenueDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.PaymentDAO;
import com.barmanagement.model.Order;
import com.barmanagement.model.Payment;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.List;

/**
 * Dashboard Controller - ENHANCED WITH REAL-TIME PAYMENT UPDATES
 * Handles main dashboard display with real-time statistics and notifications
 */
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

    // Charts - UPDATED với real data
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> ordersChart;
    @FXML private CategoryAxis revenueXAxis;
    @FXML private NumberAxis revenueYAxis;
    @FXML private CategoryAxis ordersXAxis;
    @FXML private NumberAxis ordersYAxis;

    // Activity Section - UPDATED với scrollable list
    @FXML private VBox activityContainer;
    @FXML private ScrollPane activityScrollPane;
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> tableColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> timeColumn;
    @FXML private TableColumn<Order, String> amountColumn;

    // Payment statistics labels - ENHANCED
    @FXML private Label lblCashPayments;
    @FXML private Label lblCardPayments;
    @FXML private Label lblAvgOrderValue;
    @FXML private Label lblPeakHour;

    // DAOs
    private RevenueDAO revenueDAO;
    private TableDAO tableDAO;
    private MenuItemDAO menuItemDAO;
    private OrderDAO orderDAO;
    private PaymentDAO paymentDAO;

    // Data
    private ObservableList<Order> recentOrders = FXCollections.observableArrayList();

    // Formatter
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    // State tracking
    private boolean isFirstLoad = true;
    private boolean isInitialized = false;

    // Timelines for auto-refresh
    private Timeline clockTimeline;
    private Timeline dataTimeline;

    @FXML
    public void initialize() {
        System.out.println("🏠 DASHBOARD CONTROLLER INITIALIZING...");

        // Initialize DAOs
        revenueDAO = new RevenueDAO();
        tableDAO = new TableDAO();
        menuItemDAO = new MenuItemDAO();
        orderDAO = new OrderDAO();
        paymentDAO = new PaymentDAO();

        // Setup formatter
        currencyFormatter.setMaximumFractionDigits(0);

        // Initialize time display
        updateTimeDisplay();

        // Start clock update timer (every second)
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimeDisplay()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        // Initialize table columns if table exists
        setupRecentOrdersTable();

        // Load initial dashboard data
        loadDashboardData();
        initializeCharts();

        // CRITICAL FIX: Load existing activity on startup if there are paid orders
        Platform.runLater(() -> {
            try {
                List<Order> existingPaidOrders = orderDAO.findByStatus("paid");
                if (!existingPaidOrders.isEmpty()) {
                    System.out.println("🔍 Found " + existingPaidOrders.size() + " existing paid orders, loading activity");
                    isFirstLoad = false;
                    loadRecentActivity();
                } else {
                    System.out.println("📋 No existing paid orders, showing empty state");
                    initializeEmptyActivity();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                initializeEmptyActivity();
            }
        });

        // Start data refresh timer (every 30 seconds for background updates)
        dataTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            loadDashboardData();
            updateCharts();
            // Only refresh activity if there have been payments
            if (!isFirstLoad) {
                loadRecentActivity();
            }
        }));
        dataTimeline.setCycleCount(Timeline.INDEFINITE);
        dataTimeline.play();

        // CRITICAL: Register for real-time payment updates
        DashboardUpdateUtil.addUpdateListener(this::refreshDashboardData);

        isInitialized = true;
        System.out.println("✅ DASHBOARD CONTROLLER INITIALIZED");
        System.out.println("📊 Registered for real-time payment updates");
    }

    /**
     * MAIN METHOD: Real-time dashboard refresh when payment occurs
     * Called by DashboardUpdateUtil when payment is processed
     */
    private void refreshDashboardData() {
        System.out.println("🔄 DASHBOARD REAL-TIME REFRESH TRIGGERED BY PAYMENT...");

        Platform.runLater(() -> {
            try {
                // Mark that we've had activity
                isFirstLoad = false;

                // Reload all statistics
                loadTodayStats();

                // Reload charts with new data
                loadRevenueChart();

                // Reload payment statistics
                loadPaymentMethodStatistics();

                // Reload table status
                loadTableStatus();

                // Load recent activity (will now show payments)
                loadRecentActivity();

                // Show subtle notification of update
                showUpdateNotification();

                System.out.println("✅ Dashboard real-time refresh completed successfully");

            } catch (Exception e) {
                System.err.println("❌ Error in dashboard real-time refresh: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Load today's statistics - ENHANCED VERSION
     */
    private void loadTodayStats() {
        try {
            // Load today's revenue
            BigDecimal todayRevenue = revenueDAO.getTodayRevenue();
            if (lblTodayRevenue != null) {
                lblTodayRevenue.setText(formatCurrency(todayRevenue.doubleValue()));
                // Add animation effect for revenue update
                addUpdateAnimation(lblTodayRevenue);
            }

            // Load today's orders count
            int todayOrders = revenueDAO.getTodayOrders();
            if (lblTodayOrders != null) {
                lblTodayOrders.setText(String.valueOf(todayOrders));
                addUpdateAnimation(lblTodayOrders);
            }

            // Load average order value
            BigDecimal avgOrderValue = revenueDAO.getAverageOrderValue();
            if (lblAvgOrderValue != null) {
                lblAvgOrderValue.setText(formatCurrency(avgOrderValue.doubleValue()));
            }

            System.out.println("📊 Today's stats updated - Revenue: " + todayRevenue + ", Orders: " + todayOrders);

        } catch (SQLException e) {
            System.err.println("❌ Error loading today's stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load revenue chart - ENHANCED VERSION
     */
    private void loadRevenueChart() {
        try {
            updateRevenueChart();
            updateOrdersChart();
            System.out.println("📈 Charts refreshed with latest data");
        } catch (SQLException e) {
            System.err.println("❌ Error loading charts: " + e.getMessage());
        }
    }

    /**
     * Add subtle animation to show data has been updated
     */
    private void addUpdateAnimation(Label label) {
        if (label == null) return;

        // Subtle scale animation
        javafx.animation.ScaleTransition scale = new javafx.animation.ScaleTransition(Duration.millis(200), label);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.1);
        scale.setToY(1.1);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    /**
     * Show subtle notification that dashboard has been updated
     */
    private void showUpdateNotification() {
        // Create a temporary notification label
        Platform.runLater(() -> {
            if (welcomeTimeLabel != null) {
                String originalText = welcomeTimeLabel.getText();
                welcomeTimeLabel.setText("📊 Cập nhật...");
                welcomeTimeLabel.setTextFill(Color.web("#4CAF50"));

                // Restore original text after 2 seconds
                Timeline restoreTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                    welcomeTimeLabel.setText(originalText);
                    welcomeTimeLabel.setTextFill(Color.WHITE);
                }));
                restoreTimeline.play();
            }
        });
    }

    // Initialize empty activity section
    private void initializeEmptyActivity() {
        if (activityContainer != null) {
            activityContainer.getChildren().clear();
            Label noActivityLabel = new Label("Chưa có hoạt động gần đây");
            noActivityLabel.setTextFill(Color.web("#B0B0B0"));
            noActivityLabel.setFont(Font.font("System", 14));
            noActivityLabel.setStyle("-fx-alignment: center; -fx-padding: 20;");
            activityContainer.getChildren().add(noActivityLabel);
        }
    }

    private void setupRecentOrdersTable() {
        if (recentOrdersTable != null) {
            // Setup columns
            orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            tableColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty("Bàn " + cellData.getValue().getTableId()));
            statusColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusDisplayName()));
            timeColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedOrderTime()));
            amountColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedTotal()));

            // Set custom cell factories for styling
            statusColumn.setCellFactory(column -> new TableCell<Order, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        Order order = getTableView().getItems().get(getIndex());
                        switch (order.getStatus()) {
                            case "paid":
                                setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                                break;
                            case "completed":
                                setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                                break;
                            case "cancelled":
                                setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        }
                    }
                }
            });

            // Bind data
            recentOrdersTable.setItems(recentOrders);

            // Add double-click handler to open order details
            recentOrdersTable.setRowFactory(tv -> {
                TableRow<Order> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Order selectedOrder = row.getItem();
                        openOrderDetails(selectedOrder);
                    }
                });
                return row;
            });
        }
    }

    private void openOrderDetails(Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/order_detail.fxml"));
            VBox detailRoot = loader.load();

            Stage detailStage = new Stage();
            detailStage.setTitle("Chi tiết đơn hàng #" + order.getId());
            detailStage.setScene(new Scene(detailRoot, 600, 500));
            detailStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Không thể mở chi tiết đơn hàng: " + e.getMessage());
        }
    }

    private void updateTimeDisplay() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (currentTimeLabel != null) {
            currentTimeLabel.setText(now.format(timeFormatter));
        }

        if (welcomeTimeLabel != null && isFirstLoad) {
            welcomeTimeLabel.setText(now.format(dateFormatter));
        }
    }

    private void loadDashboardData() {
        Platform.runLater(() -> {
            try {
                loadTodayStats();
                loadTableStatus();
                loadMenuItemsCount();
                loadPaymentMethodStatistics();

            } catch (Exception e) {
                e.printStackTrace();
                showErrorMessage("Không thể tải dữ liệu dashboard: " + e.getMessage());
            }
        });
    }

    /**
     * ENHANCED: Load payment method statistics
     */
    private void loadPaymentMethodStatistics() {
        try {
            Map<String, Integer> paymentStats = revenueDAO.getPaymentMethodStats();

            if (lblCashPayments != null) {
                int cashPayments = paymentStats.getOrDefault("Tiền mặt", 0);
                lblCashPayments.setText(String.valueOf(cashPayments));
            }

            if (lblCardPayments != null) {
                int cardPayments = paymentStats.getOrDefault("Thẻ tín dụng", 0) +
                        paymentStats.getOrDefault("Chuyển khoản", 0) +
                        paymentStats.getOrDefault("MOMO", 0) +
                        paymentStats.getOrDefault("ZaloPay", 0);
                lblCardPayments.setText(String.valueOf(cardPayments));
            }

            // Load peak hour info
            if (lblPeakHour != null) {
                Map<String, Object> peakData = revenueDAO.getPeakHoursAnalysis();
                String peakHour = (String) peakData.get("peak_hour");
                lblPeakHour.setText(peakHour);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    @FXML
    private void refreshData() {
        System.out.println("🔄 Manual dashboard refresh requested");
        loadDashboardData();
        updateCharts();
        if (!isFirstLoad) {
            loadRecentActivity();
        }
        System.out.println("✅ Manual dashboard refresh completed");
    }

    private void initializeCharts() {
        // Setup chart properties
        if (revenueChart != null) {
            revenueChart.setTitle("Doanh thu 7 ngày qua");
            revenueChart.setLegendVisible(false);
            revenueChart.setCreateSymbols(true);
            revenueChart.getStyleClass().add("revenue-chart");
            revenueChart.setAnimated(true);
        }

        if (ordersChart != null) {
            ordersChart.setTitle("Số đơn hàng 7 ngày qua");
            ordersChart.setLegendVisible(false);
            ordersChart.getStyleClass().add("orders-chart");
            ordersChart.setAnimated(true);
        }

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
        revenueSeries.setName("Doanh thu");

        // Convert dates to day names and add data with animation
        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        int dayIndex = 0;

        for (Map.Entry<String, BigDecimal> entry : weeklyRevenue.entrySet()) {
            String dayName = dayIndex < dayNames.length ? dayNames[dayIndex] : entry.getKey();
            double amount = entry.getValue().doubleValue() / 1000000; // Convert to millions

            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(dayName, amount);
            revenueSeries.getData().add(dataPoint);
            dayIndex++;
        }

        // Fill missing days with zero if needed
        while (dayIndex < 7) {
            revenueSeries.getData().add(new XYChart.Data<>(dayNames[dayIndex], 0));
            dayIndex++;
        }

        revenueChart.getData().add(revenueSeries);

        // Set Y-axis label
        if (revenueYAxis != null) {
            revenueYAxis.setLabel("Triệu VND");
        }

        // Apply custom styling to chart
        Platform.runLater(() -> {
            revenueChart.lookupAll(".chart-series-line").forEach(node ->
                    node.setStyle("-fx-stroke: #4CAF50; -fx-stroke-width: 3px;"));
            revenueChart.lookupAll(".chart-line-symbol").forEach(node ->
                    node.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 5px;"));
        });
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

        // Set Y-axis label
        if (ordersYAxis != null) {
            ordersYAxis.setLabel("Đơn hàng");
        }

        // Apply custom styling to bars
        Platform.runLater(() -> {
            ordersChart.lookupAll(".default-color0.chart-bar").forEach(node ->
                    node.setStyle("-fx-bar-fill: #2196F3;"));
        });
    }

    /**
     * ENHANCED: Load recent activity with real payment data
     */
    private void loadRecentActivity() {
        Platform.runLater(() -> {
            try {
                // Clear existing data
                recentOrders.clear();

                // Load completed and paid orders (actual activity)
                List<Order> paidOrders = orderDAO.findByStatus("paid");
                List<Order> completedOrders = orderDAO.findByStatus("completed");

                // Combine all orders
                paidOrders.addAll(completedOrders);

                if (paidOrders.isEmpty()) {
                    initializeEmptyActivity();
                    return;
                }

                // Sort by most recent first and take last 20
                paidOrders.stream()
                        .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                        .limit(20)
                        .forEach(recentOrders::add);

                // Update activity cards if container exists
                if (activityContainer != null) {
                    updateActivityCards();
                }

                System.out.println("📋 Recent activity loaded: " + paidOrders.size() + " orders");

            } catch (SQLException e) {
                e.printStackTrace();
                showErrorMessage("Không thể tải hoạt động gần đây: " + e.getMessage());
            }
        });
    }

    private void updateActivityCards() {
        activityContainer.getChildren().clear();

        if (recentOrders.isEmpty()) {
            initializeEmptyActivity();
            return;
        }

        // Take only first 5 for cards display
        recentOrders.stream()
                .limit(5)
                .forEach(order -> {
                    HBox activityItem = createActivityItem(order);
                    activityContainer.getChildren().add(activityItem);
                });
    }

    private HBox createActivityItem(Order order) {
        HBox itemBox = new HBox(15);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setStyle("-fx-padding: 10; -fx-background-color: #1a1a2e; -fx-background-radius: 8; -fx-cursor: hand;");

        // Status circle with animation
        Circle statusCircle = new Circle(8.0);
        switch (order.getStatus()) {
            case "paid":
                statusCircle.setFill(Color.web("#4CAF50"));
                break;
            case "completed":
                statusCircle.setFill(Color.web("#2196F3"));
                // Add pulsing animation for completed orders waiting payment
                javafx.animation.ScaleTransition pulse = new javafx.animation.ScaleTransition(Duration.seconds(1), statusCircle);
                pulse.setFromX(1.0);
                pulse.setFromY(1.0);
                pulse.setToX(1.3);
                pulse.setToY(1.3);
                pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
                pulse.setAutoReverse(true);
                pulse.play();
                break;
            case "cancelled":
                statusCircle.setFill(Color.web("#f44336"));
                break;
            default:
                statusCircle.setFill(Color.web("#FF9800"));
        }

        // Info container
        VBox infoContainer = new VBox(3);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoContainer, javafx.scene.layout.Priority.ALWAYS);

        Label titleLabel = new Label(getActivityTitle(order));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label timeLabel = new Label(getActivityTime(order));
        timeLabel.setTextFill(Color.web("#B0B0B0"));
        timeLabel.setFont(Font.font("System", 12));

        infoContainer.getChildren().addAll(titleLabel, timeLabel);

        // Amount/Status label
        Label amountLabel = new Label(getActivityAmount(order));
        amountLabel.setTextFill(getActivityAmountColor(order));
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        itemBox.getChildren().addAll(statusCircle, infoContainer, amountLabel);

        return itemBox;
    }

    private String getActivityTitle(Order order) {
        switch (order.getStatus()) {
            case "paid":
                return "Đơn hàng #" + order.getId() + " đã được thanh toán";
            case "completed":
                return "Đơn hàng #" + order.getId() + " chờ thanh toán";
            case "cancelled":
                return "Đơn hàng #" + order.getId() + " đã bị hủy";
            default:
                return "Đơn hàng #" + order.getId();
        }
    }

    private String getActivityTime(Order order) {
        if (order.getOrderTime() != null) {
            return "Bàn " + order.getTableId() + " - " +
                    order.getOrderTime().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return "Bàn " + order.getTableId();
    }

    private String getActivityAmount(Order order) {
        if (order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            return formatCurrency(order.getTotalAmount().doubleValue());
        }

        switch (order.getStatus()) {
            case "paid":
                return "Đã thanh toán";
            case "completed":
                return "Chờ thanh toán";
            case "cancelled":
                return "Đã hủy";
            default:
                return order.getStatusDisplayName();
        }
    }

    private Color getActivityAmountColor(Order order) {
        switch (order.getStatus()) {
            case "paid":
                return Color.web("#4CAF50");
            case "completed":
                return Color.web("#2196F3");
            case "cancelled":
                return Color.web("#f44336");
            default:
                return Color.web("#FF9800");
        }
    }

    private String formatCurrency(double amount) {
        return currencyFormatter.format(amount) + " VND";
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

    /**
     * PUBLIC: Method called after successful payment from external controllers
     */
    public void onPaymentCompleted() {
        System.out.println("🎉 Payment completion notification received by dashboard");

        // Mark that we've had activity
        isFirstLoad = false;

        // Trigger full refresh
        refreshDashboardData();

        // Show success notification
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText("Thanh toán thành công");
            alert.setContentText("Đơn hàng đã được thanh toán và biểu đồ đã được cập nhật!");
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
        // Cleanup before logout
        cleanup();
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
        refreshData();
    }

    // Quick action handlers for cards
    @FXML
    private void handleQuickOrder() {
        handleManageOrder(null);
    }

    @FXML
    private void handleQuickMenu() {
        handleManageMenu(null);
    }

    @FXML
    private void handleQuickTables() {
        handleManageTables(null);
    }

    private void openScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) manageOrderButton.getScene().getWindow();
            }

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

    /**
     * CRITICAL: Cleanup when controller is destroyed
     */
    public void cleanup() {
        System.out.println("🧹 Cleaning up Dashboard Controller...");

        // Stop timelines
        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        if (dataTimeline != null) {
            dataTimeline.stop();
        }

        // Remove update listener
        DashboardUpdateUtil.removeUpdateListener(this::refreshDashboardData);

        System.out.println("✅ Dashboard Controller cleanup completed");
    }
}
package com.barmanagement.controller;

import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.OrderItemDAO;
import com.barmanagement.dao.PaymentDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.dao.RevenueDAO;
import com.barmanagement.dao.JDBCConnect;
import com.barmanagement.model.MenuItem;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.Payment;
import com.barmanagement.util.DashboardUpdateUtil;

import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.net.URL;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PaymentController implements Initializable {

    @FXML private ComboBox<String> tableComboBox;
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> itemNameCol;
    @FXML private TableColumn<OrderItem, Integer> quantityCol;
    @FXML private TableColumn<OrderItem, Double> priceCol;
    @FXML private TableColumn<OrderItem, Double> totalCol;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;

    // Enhanced UI elements
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator paymentProgress;
    @FXML private VBox paymentSummaryBox;
    @FXML private VBox quickStatsBox;
    @FXML private Label lblTodayPayments;
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblCashPayments;
    @FXML private Label lblCardPayments;

    // DAOs
    private OrderDAO orderDAO = new OrderDAO();
    private OrderItemDAO orderItemDAO = new OrderItemDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private TableDAO tableDAO = new TableDAO();
    private RevenueDAO revenueDAO = new RevenueDAO();

    // Current data
    private Order currentOrder;
    private ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();

    // NEW: Flag to track if order was set from external source
    private boolean orderSetFromExternal = false;

    // Formatter
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== PAYMENT CONTROLLER INITIALIZE ===");

        // Setup formatter
        currencyFormatter.setMaximumFractionDigits(0);

        // Setup payment methods
        setupPaymentMethods();

        // Setup table columns
        setupOrderTable();

        // Load initial data
        loadPaymentStatistics();

        // Setup event handlers
        tableComboBox.setOnAction(e -> loadOrderBySelectedTable());

        // Hide progress indicator initially
        if (paymentProgress != null) {
            paymentProgress.setVisible(false);
        }

        // FIXED: Always start with empty state, don't auto-load orders
        Platform.runLater(() -> {
            if (!orderSetFromExternal) {
                System.out.println("No external order set, showing empty state");
                clearOrderDisplay();
                updateStatusLabel("Không có đơn hàng nào sẵn sàng thanh toán", Color.ORANGE);

                // Load table options but don't auto-select
                loadTableOptionsWithoutDisplay();
            } else {
                System.out.println("External order was set, skipping normal table loading");
            }
        });
    }

    // NEW: Load table options without auto-displaying orders - FIXED VERSION
    private void loadTableOptionsWithoutDisplay() {
        try {
            System.out.println("=== LOADING TABLE OPTIONS ===");

            // Clean up old completed orders first
            orderDAO.cleanupOldCompletedOrders();

            // FIXED: Get only completed orders that have NOT been paid yet
            String sql = "SELECT DISTINCT o.id, o.table_id, o.order_time, o.completed_time, o.status, o.total_amount, o.notes, o.created_by " +
                    "FROM orders o " +
                    "INNER JOIN order_items oi ON o.id = oi.order_id " +
                    "LEFT JOIN payments p ON o.id = p.order_id " +
                    "WHERE o.status = 'completed' " +
                    "AND DATE(o.order_time) = CURDATE() " +
                    "AND o.total_amount > 0 " +
                    "AND p.order_id IS NULL " + // NOT YET PAID
                    "ORDER BY o.order_time DESC";

            List<Order> unpaidCompletedOrders = new ArrayList<>();

            try (Connection conn = JDBCConnect.getJDBCConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setTableId(rs.getInt("table_id"));
                    order.setOrderTime(rs.getTimestamp("order_time"));
                    order.setCompletedTime(rs.getTimestamp("completed_time"));
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setNotes(rs.getString("notes"));
                    order.setCreatedBy(rs.getInt("created_by"));
                    unpaidCompletedOrders.add(order);
                }
            }

            ObservableList<String> tableNames = FXCollections.observableArrayList();

            System.out.println("Found " + unpaidCompletedOrders.size() + " unpaid completed orders for today");

            for (Order order : unpaidCompletedOrders) {
                String tableName = "Bàn " + order.getTableId() + " - Đơn #" + order.getId();
                tableNames.add(tableName);
                System.out.println("Added to payment options: " + tableName + " (Amount: " + order.getFormattedTotal() + ")");
            }

            tableComboBox.setItems(tableNames);

            // Update status message only
            if (tableNames.isEmpty()) {
                updateStatusLabel("Không có đơn hàng nào sẵn sàng thanh toán", Color.ORANGE);
                System.out.println("No unpaid orders available for payment");
            } else {
                updateStatusLabel("Chọn bàn để thanh toán (" + tableNames.size() + " đơn chờ)", Color.BLUE);
                System.out.println("Available unpaid orders for payment: " + tableNames.size());
            }

        } catch (SQLException e) {
            e.printStackTrace();
            updateStatusLabel("Lỗi khi tải danh sách đơn hàng", Color.RED);
        }

        System.out.println("=== END LOADING TABLE OPTIONS ===");
    }

    // FIXED: Keep original method for compatibility (alias to the new method)
    private void loadTableOptions() {
        loadTableOptionsWithoutDisplay();
    }

    // NEW: Clear order display when no orders available
    private void clearOrderDisplay() {
        orderItems.clear();
        totalLabel.setText("0 VND");
        currentOrder = null; // IMPORTANT: Reset current order

        if (paymentSummaryBox != null) {
            paymentSummaryBox.getChildren().clear();

            Label noOrderLabel = new Label("📋 Chưa có đơn hàng");
            noOrderLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #B0B0B0;");
            paymentSummaryBox.getChildren().add(noOrderLabel);

            Label instructionLabel = new Label("Hãy hoàn thành một đơn hàng từ trang Order trước");
            instructionLabel.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 14px;");
            paymentSummaryBox.getChildren().add(instructionLabel);
        }
    }

    @FXML
    private void onRefreshTables() {
        System.out.println("=== REFRESH TABLES CLICKED ===");

        // Reset everything
        orderSetFromExternal = false;
        clearOrderDisplay();

        // Load fresh table options
        loadTableOptions();
        loadPaymentStatistics();

        updateStatusLabel("Đã làm mới danh sách đơn hàng", Color.BLUE);
    }

    private void setupPaymentMethods() {
        paymentMethodComboBox.setItems(FXCollections.observableArrayList(
                "Tiền mặt", "Chuyển khoản", "MOMO", "Thẻ tín dụng", "ZaloPay"
        ));
        paymentMethodComboBox.getSelectionModel().select(0); // Default to cash
    }

    private void setupOrderTable() {
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalCol.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getPrice() * cellData.getValue().getQuantity())
        );

        // Format price columns
        priceCol.setCellFactory(column -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                }
            }
        });

        totalCol.setCellFactory(column -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatCurrency(item));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #4CAF50;");
                }
            }
        });

        orderTable.setItems(orderItems);
    }

    // FIXED: Parse table and order info from selection
    private void loadOrderBySelectedTable() {
        String selectedTableName = tableComboBox.getValue();
        System.out.println("=== LOAD ORDER BY SELECTED TABLE ===");
        System.out.println("Selected: " + selectedTableName);

        if (selectedTableName == null) {
            System.out.println("No table selected, clearing display");
            clearOrderDisplay();
            return;
        }

        // Extract order ID from selection (format: "Bàn X - Đơn #Y")
        try {
            String[] parts = selectedTableName.split(" - Đơn #");
            if (parts.length != 2) {
                System.out.println("Invalid table name format: " + selectedTableName);
                clearOrderDisplay();
                return;
            }

            int orderId = Integer.parseInt(parts[1]);
            System.out.println("Looking for order ID: " + orderId);

            currentOrder = orderDAO.findById(orderId);

            if (currentOrder != null) {
                System.out.println("Found order: #" + currentOrder.getId() + " with status: " + currentOrder.getStatus());
            } else {
                System.out.println("Order not found!");
            }

            updateOrderDisplay();

        } catch (Exception e) {
            System.err.println("Error loading order: " + e.getMessage());
            e.printStackTrace();
            currentOrder = null;
            clearOrderDisplay();
        }

        System.out.println("=== END LOAD ORDER BY SELECTED TABLE ===");
    }

    // FIXED: Update order display to only show unpaid completed orders
    private void updateOrderDisplay() {
        orderItems.clear();

        if (currentOrder == null) {
            totalLabel.setText("0 VND");
            updateStatusLabel("Không tìm thấy đơn hàng", Color.ORANGE);
            return;
        }

        // Only show completed orders that haven't been paid
        if (!"completed".equals(currentOrder.getStatus())) {
            totalLabel.setText("0 VND");
            updateStatusLabel("Đơn hàng này chưa hoàn thành", Color.ORANGE);
            return;
        }

        // FIXED: Check if order has already been paid
        try {
            String checkPaymentSql = "SELECT COUNT(*) FROM payments WHERE order_id = ?";
            try (Connection conn = JDBCConnect.getJDBCConnection();
                 PreparedStatement ps = conn.prepareStatement(checkPaymentSql)) {

                ps.setInt(1, currentOrder.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        totalLabel.setText("0 VND");
                        updateStatusLabel("Đơn hàng này đã được thanh toán rồi!", Color.RED);
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            List<OrderItem> items = orderItemDAO.findByOrderId(currentOrder.getId());
            orderItems.addAll(items);

            // Calculate total
            double total = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
            totalLabel.setText(formatCurrency(total));

            // Update status
            updateStatusLabel("Sẵn sàng thanh toán - Đơn hàng #" + currentOrder.getId(), Color.GREEN);

            // Update payment summary
            updatePaymentSummary(items, total);

        } catch (Exception e) {
            e.printStackTrace();
            updateStatusLabel("Lỗi khi tải danh sách món!", Color.RED);
        }
    }

    private void updatePaymentSummary(List<OrderItem> items, double total) {
        if (paymentSummaryBox == null) return;

        paymentSummaryBox.getChildren().clear();

        // Add summary header
        Label summaryHeader = new Label("📋 Tóm tắt đơn hàng");
        summaryHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: white;");
        paymentSummaryBox.getChildren().add(summaryHeader);

        // Add item count
        Label itemCount = new Label("Số món: " + items.size());
        itemCount.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 14px;");
        paymentSummaryBox.getChildren().add(itemCount);

        // Add total quantity
        int totalQty = items.stream().mapToInt(OrderItem::getQuantity).sum();
        Label totalQuantity = new Label("Tổng số lượng: " + totalQty);
        totalQuantity.setStyle("-fx-text-fill: #B0B0B0; -fx-font-size: 14px;");
        paymentSummaryBox.getChildren().add(totalQuantity);

        // Add total amount (large)
        Label totalAmount = new Label("Tổng tiền: " + formatCurrency(total));
        totalAmount.setStyle("-fx-font-weight: bold; -fx-font-size: 20px; -fx-text-fill: #4CAF50;");
        paymentSummaryBox.getChildren().add(totalAmount);
    }

    private void updateStatusLabel(String message, Color color) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setTextFill(color);

            // Add fade animation
            FadeTransition fade = new FadeTransition(Duration.millis(500), statusLabel);
            fade.setFromValue(0.0);
            fade.setToValue(1.0);
            fade.play();
        }
    }

    private void loadPaymentStatistics() {
        Platform.runLater(() -> {
            try {
                // Load today's revenue
                BigDecimal todayRevenue = revenueDAO.getTodayRevenue();
                if (lblTotalRevenue != null) {
                    lblTotalRevenue.setText(formatCurrency(todayRevenue.doubleValue()));
                }

                // Load today's orders
                int todayOrders = revenueDAO.getTodayOrders();
                if (lblTodayPayments != null) {
                    lblTodayPayments.setText(String.valueOf(todayOrders));
                }

                // Load payment method statistics
                var paymentStats = revenueDAO.getPaymentMethodStats();
                if (lblCashPayments != null) {
                    lblCashPayments.setText(String.valueOf(paymentStats.getOrDefault("Tiền mặt", 0)));
                }

                if (lblCardPayments != null) {
                    int cardTotal = paymentStats.getOrDefault("Thẻ tín dụng", 0) +
                            paymentStats.getOrDefault("Chuyển khoản", 0) +
                            paymentStats.getOrDefault("MOMO", 0) +
                            paymentStats.getOrDefault("ZaloPay", 0);
                    lblCardPayments.setText(String.valueOf(cardTotal));
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void onConfirmPayment() {
        if (currentOrder == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn đơn hàng để thanh toán.");
            return;
        }

        // Verify order is completed
        if (!"completed".equals(currentOrder.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Đơn hàng này chưa hoàn thành!");
            return;
        }

        String method = getSelectedPaymentMethod();
        double totalAmount = parseTotalAmount();

        if (totalAmount <= 0) {
            showAlert(Alert.AlertType.WARNING, "Số tiền thanh toán không hợp lệ!");
            return;
        }

        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận thanh toán");
        confirmAlert.setHeaderText("Bạn có chắc chắn muốn thanh toán?");
        confirmAlert.setContentText(
                "Đơn hàng #" + currentOrder.getId() +
                        "\nBàn " + currentOrder.getTableId() +
                        "\nSố tiền: " + formatCurrency(totalAmount) +
                        "\nPhương thức: " + method
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                processPayment(method, totalAmount);
            }
        });
    }

    private void processPayment(String method, double totalAmount) {
        // Show progress
        if (paymentProgress != null) {
            paymentProgress.setVisible(true);
        }

        // Process payment in background
        new Thread(() -> {
            try {
                // Simulate payment processing delay
                Thread.sleep(1000);

                Platform.runLater(() -> {
                    try {
                        // Process the payment using OrderDAO
                        orderDAO.processPayment(currentOrder.getId(), method, 1);

                        // Hide progress
                        if (paymentProgress != null) {
                            paymentProgress.setVisible(false);
                        }

                        // Show success message
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Thành công");
                        successAlert.setHeaderText("Thanh toán thành công!");
                        successAlert.setContentText(
                                "Đơn hàng #" + currentOrder.getId() + "\n" +
                                        "Số tiền: " + formatCurrency(totalAmount) + "\n" +
                                        "Phương thức: " + method + "\n\n" +
                                        "Bàn đã được giải phóng và dữ liệu đã được cập nhật."
                        );

                        successAlert.showAndWait().ifPresent(response -> {
                            // FIXED: Navigate back to dashboard after payment
                            goBackToDashboard();
                        });

                        // Notify dashboard to update
                        DashboardUpdateUtil.notifyDashboardUpdate();

                        // Reset form and reload data
                        resetPaymentForm();
                        loadTableOptions();
                        loadPaymentStatistics();

                    } catch (Exception e) {
                        e.printStackTrace();
                        if (paymentProgress != null) {
                            paymentProgress.setVisible(false);
                        }
                        showAlert(Alert.AlertType.ERROR, "Lỗi khi xử lý thanh toán: " + e.getMessage());
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    // NEW: Navigate back to dashboard
    private void goBackToDashboard() {
        try {
            SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resetPaymentForm() {
        currentOrder = null;
        orderItems.clear();
        totalLabel.setText("0 VND");

        if (paymentSummaryBox != null) {
            paymentSummaryBox.getChildren().clear();
        }

        updateStatusLabel("Chọn bàn để bắt đầu thanh toán", Color.GRAY);

        // Reset table selection
        tableComboBox.getSelectionModel().clearSelection();
        paymentMethodComboBox.getSelectionModel().select(0);
    }

    @FXML
    private void onCancel() {
        resetPaymentForm();
    }

    private double parseTotalAmount() {
        String totalText = totalLabel.getText().replaceAll("[^0-9]", "");
        if (totalText.isEmpty()) return 0;
        return Double.parseDouble(totalText);
    }

    private String getSelectedPaymentMethod() {
        String method = paymentMethodComboBox.getValue();
        return (method != null && !method.isEmpty()) ? method : "Tiền mặt";
    }

    private String formatCurrency(double amount) {
        return currencyFormatter.format(amount) + " VND";
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(type == Alert.AlertType.ERROR ? "Lỗi" :
                type == Alert.AlertType.WARNING ? "Cảnh báo" : "Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation methods
    @FXML
    private void showHome() {
        SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
    }

    @FXML
    private void goBack() {
        SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
    }

    @FXML
    private void showDashboard() {
        SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
    }

    @FXML
    private void showMenu() {
        SceneUtil.openScene("/fxml/menu_management.fxml", totalLabel);
    }

    @FXML
    private void showOrder() {
        SceneUtil.openScene("/fxml/order_management.fxml", totalLabel);
    }

    @FXML
    private void showTableManagement() {
        SceneUtil.openScene("/fxml/table_management.fxml", totalLabel);
    }

    @FXML
    private void handleLogout() {
        LogoutUtil.confirmLogout(totalLabel);
    }

    @FXML
    private void exportMenu() {
        showAlert(Alert.AlertType.INFORMATION,
                "Chức năng xuất báo cáo sẽ được phát triển trong phiên bản tới!");
    }

    @FXML
    private void importMenu() {
        showAlert(Alert.AlertType.INFORMATION,
                "Chức năng lịch sử thanh toán sẽ được phát triển trong phiên bản tới!");
    }

    // Method to set specific order for payment (can be called from dashboard)
    public void setOrderForPayment(Order order) {
        this.currentOrder = order;

        // Find and select the table
        String tableName = "Bàn " + order.getTableId() + " - Đơn #" + order.getId();

        // Refresh table options first to make sure this order is available
        loadTableOptions();

        // Then select it
        tableComboBox.getSelectionModel().select(tableName);

        // Update display
        updateOrderDisplay();
    }
}
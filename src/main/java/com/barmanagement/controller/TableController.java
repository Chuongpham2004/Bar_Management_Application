package com.barmanagement.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TableController implements Initializable {

    // FXML Elements - Sidebar
    @FXML private Button dashboardBtn;
    @FXML private Button tablesBtn;
    @FXML private Button ordersBtn;
    @FXML private Button paymentBtn;
    @FXML private Label staffNameLabel;

    // FXML Elements - Header & Filters
    @FXML private ComboBox<String> locationFilterCombo;
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button refreshBtn;

    // FXML Elements - Statistics
    @FXML private Label availableCountLabel;
    @FXML private Label occupiedCountLabel;
    @FXML private Label reservedCountLabel;
    @FXML private Label totalRevenueLabel;

    // FXML Elements - Table Grids
    @FXML private FlowPane vipTablesFlow;
    @FXML private FlowPane mainFloorTablesFlow;
    @FXML private FlowPane outdoorTablesFlow;
    @FXML private FlowPane barCounterTablesFlow;

    // FXML Elements - Controls
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button addTableBtn;

    // Data Management
    private Map<String, Table> tables = new HashMap<>();
    private ScheduledExecutorService updateScheduler;
    private String currentLocationFilter = "Tất cả";
    private String currentStatusFilter = "Tất cả";

    // Table Status Enum
    public enum TableStatus {
        AVAILABLE("Trống", "#4CAF50"),
        OCCUPIED("Đang sử dụng", "#FF5722"),
        RESERVED("Đã đặt", "#FF9800"),
        CLEANING("Đang dọn", "#9E9E9E"),
        MAINTENANCE("Bảo trì", "#795548");

        private final String displayName;
        private final String color;

        TableStatus(String displayName, String color) {
            this.displayName = displayName;
            this.color = color;
        }

        public String getDisplayName() { return displayName; }
        public String getColor() { return color; }
    }

    // Table Location Enum
    public enum TableLocation {
        VIP("VIP"),
        MAIN_FLOOR("Sảnh chính"),
        OUTDOOR("Ngoài trời"),
        BAR_COUNTER("Quầy bar");

        private final String displayName;

        TableLocation(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    // Table Model
    public static class Table {
        private String id;
        private String name;
        private TableLocation location;
        private TableStatus status;
        private int capacity;
        private String customerName;
        private LocalDateTime occupiedTime;
        private double currentBill;
        private String notes;

        public Table(String id, String name, TableLocation location, int capacity) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.capacity = capacity;
            this.status = TableStatus.AVAILABLE;
            this.currentBill = 0.0;
        }

        // Getters and Setters
        public String getId() { return id; }
        public String getName() { return name; }
        public TableLocation getLocation() { return location; }
        public TableStatus getStatus() { return status; }
        public void setStatus(TableStatus status) { this.status = status; }
        public int getCapacity() { return capacity; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public LocalDateTime getOccupiedTime() { return occupiedTime; }
        public void setOccupiedTime(LocalDateTime occupiedTime) { this.occupiedTime = occupiedTime; }
        public double getCurrentBill() { return currentBill; }
        public void setCurrentBill(double currentBill) { this.currentBill = currentBill; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }

        public String getOccupiedDuration() {
            if (occupiedTime == null) return "";
            long minutes = java.time.Duration.between(occupiedTime, LocalDateTime.now()).toMinutes();
            return String.format("%d:%02d", minutes / 60, minutes % 60);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        generateSampleTables();
        setupAutoRefresh();
        refreshTables();
        updateStatistics();
    }

    /**
     * Setup filter comboboxes
     */
    private void setupFilters() {
        // Location filter
        locationFilterCombo.getItems().addAll(
                "Tất cả", "VIP", "Sảnh chính", "Ngoài trời", "Quầy bar"
        );
        locationFilterCombo.setValue("Tất cả");
        locationFilterCombo.setOnAction(e -> {
            currentLocationFilter = locationFilterCombo.getValue();
            refreshTables();
        });

        // Status filter
        statusFilterCombo.getItems().addAll(
                "Tất cả", "Trống", "Đang sử dụng", "Đã đặt", "Đang dọn", "Bảo trì"
        );
        statusFilterCombo.setValue("Tất cả");
        statusFilterCombo.setOnAction(e -> {
            currentStatusFilter = statusFilterCombo.getValue();
            refreshTables();
        });
    }

    /**
     * Generate sample tables
     */
    private void generateSampleTables() {
        // VIP Tables
        for (int i = 1; i <= 5; i++) {
            Table table = new Table("VIP" + i, "VIP " + i, TableLocation.VIP, 4 + (i % 3));
            if (i <= 2) {
                table.setStatus(TableStatus.OCCUPIED);
                table.setCustomerName("Khách VIP " + i);
                table.setOccupiedTime(LocalDateTime.now().minusMinutes(30 + i * 15));
                table.setCurrentBill(500000 + i * 200000);
            } else if (i == 3) {
                table.setStatus(TableStatus.RESERVED);
                table.setCustomerName("Đặt trước - Mr. Smith");
            }
            tables.put(table.getId(), table);
        }

        // Main Floor Tables
        for (int i = 1; i <= 15; i++) {
            Table table = new Table("T" + i, "Bàn " + i, TableLocation.MAIN_FLOOR, 2 + (i % 4));
            if (i <= 8) {
                table.setStatus(TableStatus.OCCUPIED);
                table.setCustomerName("Khách " + i);
                table.setOccupiedTime(LocalDateTime.now().minusMinutes(i * 10));
                table.setCurrentBill(150000 + i * 50000);
            } else if (i <= 10) {
                table.setStatus(TableStatus.RESERVED);
                table.setCustomerName("Đặt trước");
            } else if (i == 11) {
                table.setStatus(TableStatus.CLEANING);
            }
            tables.put(table.getId(), table);
        }

        // Outdoor Tables
        for (int i = 1; i <= 8; i++) {
            Table table = new Table("OUT" + i, "Ngoài " + i, TableLocation.OUTDOOR, 3 + (i % 2));
            if (i <= 3) {
                table.setStatus(TableStatus.OCCUPIED);
                table.setCustomerName("Khách ngoài trời " + i);
                table.setOccupiedTime(LocalDateTime.now().minusMinutes(i * 20));
                table.setCurrentBill(200000 + i * 75000);
            } else if (i == 4) {
                table.setStatus(TableStatus.MAINTENANCE);
                table.setNotes("Sửa chữa ghế");
            }
            tables.put(table.getId(), table);
        }

        // Bar Counter
        for (int i = 1; i <= 10; i++) {
            Table table = new Table("BAR" + i, "Bar " + i, TableLocation.BAR_COUNTER, 1);
            if (i <= 6) {
                table.setStatus(TableStatus.OCCUPIED);
                table.setCustomerName("Khách bar " + i);
                table.setOccupiedTime(LocalDateTime.now().minusMinutes(i * 5));
                table.setCurrentBill(80000 + i * 30000);
            }
            tables.put(table.getId(), table);
        }
    }

    /**
     * Setup auto refresh
     */
    private void setupAutoRefresh() {
        updateScheduler = Executors.newScheduledThreadPool(1);
        updateScheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                updateStatistics();
                updateTableTimers();
            });
        }, 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Refresh tables display
     */
    @FXML
    private void refreshTables() {
        setLoadingState(true);

        // Simulate network delay
        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(e -> {
            clearAllTables();
            displayFilteredTables();
            updateStatistics();
            setLoadingState(false);
        });
        pause.play();
    }

    /**
     * Clear all table displays
     */
    private void clearAllTables() {
        vipTablesFlow.getChildren().clear();
        mainFloorTablesFlow.getChildren().clear();
        outdoorTablesFlow.getChildren().clear();
        barCounterTablesFlow.getChildren().clear();
    }

    /**
     * Display filtered tables
     */
    private void displayFilteredTables() {
        for (Table table : tables.values()) {
            if (shouldDisplayTable(table)) {
                Button tableBtn = createTableButton(table);

                switch (table.getLocation()) {
                    case VIP:
                        vipTablesFlow.getChildren().add(tableBtn);
                        break;
                    case MAIN_FLOOR:
                        mainFloorTablesFlow.getChildren().add(tableBtn);
                        break;
                    case OUTDOOR:
                        outdoorTablesFlow.getChildren().add(tableBtn);
                        break;
                    case BAR_COUNTER:
                        barCounterTablesFlow.getChildren().add(tableBtn);
                        break;
                }
            }
        }
    }

    /**
     * Check if table should be displayed based on filters
     */
    private boolean shouldDisplayTable(Table table) {
        // Location filter
        if (!"Tất cả".equals(currentLocationFilter)) {
            if (!table.getLocation().getDisplayName().equals(currentLocationFilter)) {
                return false;
            }
        }

        // Status filter
        if (!"Tất cả".equals(currentStatusFilter)) {
            if (!table.getStatus().getDisplayName().equals(currentStatusFilter)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Create table button
     */
    private Button createTableButton(Table table) {
        Button btn = new Button();
        btn.setPrefSize(130, 110);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-background-radius: 12; " +
                        "-fx-font-size: 11px; -fx-font-weight: bold; -fx-alignment: center;",
                table.getStatus().getColor()
        ));

        // Button content
        StringBuilder content = new StringBuilder();
        content.append(table.getName()).append("\n");
        content.append("👥 ").append(table.getCapacity()).append(" chỗ\n");
        content.append("📍 ").append(table.getStatus().getDisplayName());

        if (table.getStatus() == TableStatus.OCCUPIED) {
            content.append("\n⏰ ").append(table.getOccupiedDuration());
            if (table.getCurrentBill() > 0) {
                content.append("\n💰 ").append(String.format("%.0fk", table.getCurrentBill() / 1000));
            }
        } else if (table.getStatus() == TableStatus.RESERVED && table.getCustomerName() != null) {
            content.append("\n👤 ").append(table.getCustomerName());
        }

        btn.setText(content.toString());

        // Add drop shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(table.getStatus().getColor()));
        shadow.setRadius(8.0);
        btn.setEffect(shadow);

        // Click handler
        btn.setOnAction(e -> showTableDetails(table));

        return btn;
    }

    /**
     * Show table details dialog
     */
    private void showTableDetails(Table table) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Chi tiết bàn - " + table.getName());
        dialog.setHeaderText("Thông tin chi tiết bàn " + table.getName());

        StringBuilder details = new StringBuilder();
        details.append("📍 Vị trí: ").append(table.getLocation().getDisplayName()).append("\n");
        details.append("👥 Sức chứa: ").append(table.getCapacity()).append(" người\n");
        details.append("📊 Trạng thái: ").append(table.getStatus().getDisplayName()).append("\n");

        if (table.getStatus() == TableStatus.OCCUPIED) {
            details.append("👤 Khách hàng: ").append(table.getCustomerName()).append("\n");
            details.append("⏰ Thời gian: ").append(table.getOccupiedDuration()).append("\n");
            details.append("💰 Hóa đơn hiện tại: ").append(String.format("%,.0fđ", table.getCurrentBill()));
        } else if (table.getStatus() == TableStatus.RESERVED) {
            details.append("👤 Đặt trước: ").append(table.getCustomerName());
        } else if (table.getStatus() == TableStatus.MAINTENANCE && table.getNotes() != null) {
            details.append("📝 Ghi chú: ").append(table.getNotes());
        }

        dialog.setContentText(details.toString());

        // Add action buttons
        ButtonType actionBtn = null;
        switch (table.getStatus()) {
            case AVAILABLE:
                actionBtn = new ButtonType("🎯 Đặt bàn");
                break;
            case OCCUPIED:
                actionBtn = new ButtonType("💳 Thanh toán");
                break;
            case RESERVED:
                actionBtn = new ButtonType("✅ Check-in");
                break;
            case CLEANING:
                actionBtn = new ButtonType("✨ Hoàn thành dọn dẹp");
                break;
            case MAINTENANCE:
                actionBtn = new ButtonType("🔧 Hoàn thành sửa chữa");
                break;
        }

        if (actionBtn != null) {
            dialog.getButtonTypes().add(0, actionBtn);
        }
        dialog.getButtonTypes().add(new ButtonType("📝 Chỉnh sửa"));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent()) {
            handleTableAction(table, result.get().getText());
        }
    }

    /**
     * Handle table actions
     */
    private void handleTableAction(Table table, String action) {
        switch (action) {
            case "🎯 Đặt bàn":
                reserveTable(table);
                break;
            case "💳 Thanh toán":
                processPayment(table);
                break;
            case "✅ Check-in":
                checkInTable(table);
                break;
            case "✨ Hoàn thành dọn dẹp":
                table.setStatus(TableStatus.AVAILABLE);
                refreshTables();
                break;
            case "🔧 Hoàn thành sửa chữa":
                table.setStatus(TableStatus.AVAILABLE);
                table.setNotes(null);
                refreshTables();
                break;
            case "📝 Chỉnh sửa":
                editTable(table);
                break;
        }
    }

    /**
     * Reserve table
     */
    private void reserveTable(Table table) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đặt bàn");
        dialog.setHeaderText("Đặt bàn " + table.getName());
        dialog.setContentText("Tên khách hàng:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            table.setStatus(TableStatus.OCCUPIED);
            table.setCustomerName(result.get().trim());
            table.setOccupiedTime(LocalDateTime.now());
            table.setCurrentBill(0);
            refreshTables();
            showSuccessMessage("Đã đặt bàn " + table.getName() + " cho " + result.get());
        }
    }

    /**
     * Process payment
     */
    private void processPayment(Table table) {
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Thanh toán");
        dialog.setHeaderText("Thanh toán bàn " + table.getName());
        dialog.setContentText(String.format(
                "Khách hàng: %s\nThời gian: %s\nTổng tiền: %,.0fđ\n\nXác nhận thanh toán?",
                table.getCustomerName(), table.getOccupiedDuration(), table.getCurrentBill()
        ));

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            table.setStatus(TableStatus.CLEANING);
            table.setCustomerName(null);
            table.setOccupiedTime(null);
            table.setCurrentBill(0);
            refreshTables();
            showSuccessMessage("Thanh toán thành công cho bàn " + table.getName());
        }
    }

    /**
     * Check in reserved table
     */
    private void checkInTable(Table table) {
        table.setStatus(TableStatus.OCCUPIED);
        table.setOccupiedTime(LocalDateTime.now());
        refreshTables();
        showSuccessMessage("Check-in thành công bàn " + table.getName());
    }

    /**
     * Edit table details
     */
    private void editTable(Table table) {
        // For simplicity, just show change status options
        ChoiceDialog<TableStatus> dialog = new ChoiceDialog<>(table.getStatus(), TableStatus.values());
        dialog.setTitle("Chỉnh sửa bàn");
        dialog.setHeaderText("Thay đổi trạng thái bàn " + table.getName());
        dialog.setContentText("Trạng thái mới:");

        Optional<TableStatus> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != table.getStatus()) {
            table.setStatus(result.get());
            if (result.get() == TableStatus.AVAILABLE) {
                table.setCustomerName(null);
                table.setOccupiedTime(null);
                table.setCurrentBill(0);
            }
            refreshTables();
            showSuccessMessage("Đã cập nhật trạng thái bàn " + table.getName());
        }
    }

    /**
     * Add new table
     */
    @FXML
    private void addNewTable() {
        // Create a simple dialog for adding new table
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Thêm bàn mới");
        dialog.setHeaderText("Chức năng đang phát triển");
        dialog.setContentText("Tính năng thêm bàn mới sẽ được cập nhật trong phiên bản tiếp theo.");
        dialog.showAndWait();
    }

    /**
     * Update statistics
     */
    private void updateStatistics() {
        long available = tables.values().stream().filter(t -> t.getStatus() == TableStatus.AVAILABLE).count();
        long occupied = tables.values().stream().filter(t -> t.getStatus() == TableStatus.OCCUPIED).count();
        long reserved = tables.values().stream().filter(t -> t.getStatus() == TableStatus.RESERVED).count();

        double totalRevenue = tables.values().stream()
                .filter(t -> t.getStatus() == TableStatus.OCCUPIED)
                .mapToDouble(Table::getCurrentBill)
                .sum();

        Platform.runLater(() -> {
            availableCountLabel.setText(String.valueOf(available));
            occupiedCountLabel.setText(String.valueOf(occupied));
            reservedCountLabel.setText(String.valueOf(reserved));
            totalRevenueLabel.setText(String.format("%.1fM", totalRevenue / 1000000));
        });
    }

    /**
     * Update table timers for occupied tables
     */
    private void updateTableTimers() {
        // This would update the display of occupied tables with new timer values
        // For performance, we only refresh if there are occupied tables visible
        boolean hasOccupiedTables = tables.values().stream()
                .anyMatch(t -> t.getStatus() == TableStatus.OCCUPIED);

        if (hasOccupiedTables) {
            refreshTables();
        }
    }

    /**
     * Set loading state
     */
    private void setLoadingState(boolean loading) {
        loadingIndicator.setVisible(loading);
        refreshBtn.setDisable(loading);
        addTableBtn.setDisable(loading);
    }

    /**
     * Show success message
     */
    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thành công");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Navigation Methods
    @FXML
    private void showDashboard() {
        navigateToScene("/fxml/home.fxml", "Dashboard - BarFlow");
    }

    @FXML
    private void showTables() {
        // Already on tables page
    }

    @FXML
    private void showOrders() {
        navigateToScene("/fxml/orders.fxml", "Gọi món - BarFlow");
    }

    @FXML
    private void showPayment() {
        navigateToScene("/fxml/payment.fxml", "Thanh toán - BarFlow");
    }

    @FXML
    private void logout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Đăng xuất");
        alert.setHeaderText("Xác nhận đăng xuất");
        alert.setContentText("Bạn có chắc chắn muốn đăng xuất?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            cleanup();
            navigateToScene("/fxml/login.fxml", "Đăng nhập - BarFlow");
        }
    }

    /**
     * Navigate to scene
     */
    private void navigateToScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) refreshBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Không thể điều hướng");
            alert.setContentText("Đã xảy ra lỗi khi chuyển trang: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Cleanup resources
     */
    public void cleanup() {
        if (updateScheduler != null && !updateScheduler.isShutdown()) {
            updateScheduler.shutdown();
        }
    }

    // Getters for external access
    public Map<String, Table> getTables() {
        return new HashMap<>(tables);
    }

    public void setStaffName(String staffName) {
        if (staffNameLabel != null) {
            staffNameLabel.setText(staffName);
        }
    }
}

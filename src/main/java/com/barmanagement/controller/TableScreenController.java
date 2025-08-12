package com.barmanagement.controller;

import com.barmanagement.model.Staff;
import com.barmanagement.model.Table;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.util.AlertUtil;
import com.barmanagement.util.DateUtil;
import com.barmanagement.util.ValidationUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Table Screen Controller - Enhanced with better UX and performance
 * Manages table layout, selection, and real-time updates
 */
public class TableScreenController extends BaseController implements Initializable {

    // FXML Components
    @FXML private FlowPane tableFlowPane;
    @FXML private Label staffNameLabel;
    @FXML private Label currentTimeLabel;
    @FXML private Label totalTablesLabel;
    @FXML private Label availableTablesLabel;
    @FXML private Label occupiedTablesLabel;
    @FXML private Label reservedTablesLabel;
    @FXML private Button refreshButton;
    @FXML private Button addTableButton;
    @FXML private Button backButton;
    @FXML private ComboBox<String> filterComboBox;
    @FXML private ComboBox<String> locationFilterComboBox;
    @FXML private TextField searchField;
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private VBox statisticsBox;

    // Data and DAO
    private List<Table> tableList = new ArrayList<>();
    private List<Table> filteredTableList = new ArrayList<>();
    private TableDAO tableDAO;

    // UI State
    private boolean isLoading = false;
    private javafx.animation.Timeline clockTimeline;
    private javafx.animation.Timeline autoRefreshTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize base controller
        initializeBase();

        // Initialize DAO
        tableDAO = new TableDAO();

        // Setup UI components
        setupUI();

        // Setup event handlers
        setupEventHandlers();

        // Load initial data
        loadTablesAsync();

        // Setup auto-refresh
        setupAutoRefresh();

        System.out.println("🪑 Table Screen initialized successfully");
    }

    /**
     * Setup UI components and styling
     */
    private void setupUI() {
        // Initialize loading state
        setLoadingState(false);

        // Setup filter combo boxes
        setupFilterComboBoxes();

        // Setup search field
        setupSearchField();

        // Setup buttons
        setupButtons();

        // Setup statistics display
        setupStatisticsDisplay();

        // Update user display
        updateUserDisplay();

        // Start real-time clock
        startClock();

        // Apply styling
        applyCustomStyling();
    }

    /**
     * Setup filter combo boxes
     */
    private void setupFilterComboBoxes() {
        if (filterComboBox != null) {
            filterComboBox.getItems().addAll(
                    "Tất cả trạng thái",
                    "Bàn trống",
                    "Đang sử dụng",
                    "Đã đặt trước",
                    "Đang dọn dẹp"
            );
            filterComboBox.setValue("Tất cả trạng thái");
        }

        if (locationFilterComboBox != null) {
            locationFilterComboBox.getItems().addAll(
                    "Tất cả khu vực",
                    "Sảnh chính",
                    "Khu VIP",
                    "Ngoài trời",
                    "Quầy bar"
            );
            locationFilterComboBox.setValue("Tất cả khu vực");
        }
    }

    /**
     * Setup search field
     */
    private void setupSearchField() {
        if (searchField != null) {
            searchField.setPromptText("Tìm kiếm theo số bàn...");
            searchField.textProperty().addListener((obs, oldText, newText) -> {
                if (!isLoading) {
                    filterTables();
                }
            });
        }
    }

    /**
     * Setup buttons
     */
    private void setupButtons() {
        if (refreshButton != null) {
            refreshButton.setOnAction(e -> refreshTables());
        }

        if (addTableButton != null) {
            addTableButton.setOnAction(e -> handleAddTable());
            // Only managers and admins can add tables
            addTableButton.setVisible(isManagerOrAbove());
            addTableButton.setDisable(!isManagerOrAbove());
        }

        if (backButton != null) {
            backButton.setOnAction(e -> handleBackToDashboard());
        }
    }

    /**
     * Setup event handlers
     */
    private void setupEventHandlers() {
        if (filterComboBox != null) {
            filterComboBox.setOnAction(e -> {
                if (!isLoading) filterTables();
            });
        }

        if (locationFilterComboBox != null) {
            locationFilterComboBox.setOnAction(e -> {
                if (!isLoading) filterTables();
            });
        }
    }

    /**
     * Setup statistics display
     */
    private void setupStatisticsDisplay() {
        if (statisticsBox != null) {
            statisticsBox.setSpacing(10);
            statisticsBox.setPadding(new Insets(10));
        }
    }

    /**
     * Apply custom styling
     */
    private void applyCustomStyling() {
        if (tableFlowPane != null) {
            tableFlowPane.setHgap(15);
            tableFlowPane.setVgap(15);
            tableFlowPane.setPadding(new Insets(20));
        }
    }

    /**
     * Set current staff and update display
     */
    public void setCurrentStaff(Staff staff) {
        // Staff is managed by SessionManager, just update display
        updateUserDisplay();
    }

    /**
     * Initialize data when coming from other screens
     */
    public void initializeData() {
        if (!isLoading) {
            loadTablesAsync();
        }
    }

    @Override
    protected void updateUserDisplay() {
        Staff currentStaff = getCurrentStaff();
        if (currentStaff != null && staffNameLabel != null) {
            Platform.runLater(() -> {
                String displayText = String.format("👤 %s (%s)",
                        currentStaff.getFullName(),
                        getRoleDisplayName(currentStaff.getRole()));
                staffNameLabel.setText(displayText);
            });
        }
    }

    /**
     * Start real-time clock
     */
    private void startClock() {
        if (currentTimeLabel != null) {
            clockTimeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                        Platform.runLater(() -> {
                            currentTimeLabel.setText("🕐 " + DateUtil.formatDateTime(LocalDateTime.now()));
                        });
                    })
            );
            clockTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
            clockTimeline.play();
        }
    }

    /**
     * Set loading state
     */
    private void setLoadingState(boolean loading) {
        this.isLoading = loading;
        Platform.runLater(() -> {
            if (loadingIndicator != null) {
                loadingIndicator.setVisible(loading);
            }
            if (refreshButton != null) {
                refreshButton.setDisable(loading);
                refreshButton.setText(loading ? "Đang tải..." : "🔄 Làm mới");
            }
        });
    }

    /**
     * Load tables from database asynchronously
     */
    private void loadTablesAsync() {
        if (isLoading) return;

        setLoadingState(true);

        Task<List<Table>> loadTask = new Task<List<Table>>() {
            @Override
            protected List<Table> call() throws Exception {
                // Simulate network delay for better UX
                Thread.sleep(300);
                return tableDAO.getAllTables();
            }

            @Override
            protected void succeeded() {
                tableList = getValue();
                Platform.runLater(() -> {
                    setLoadingState(false);
                    filterTables(); // Apply current filters
                    updateStatistics();
                    System.out.println("✅ Loaded " + tableList.size() + " tables");
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setLoadingState(false);
                    AlertUtil.showError("Lỗi tải dữ liệu",
                            "Không thể tải danh sách bàn.\nSử dụng dữ liệu mẫu để demo.");
                    loadSampleData();
                });
            }
        };

        new Thread(loadTask).start();
    }

    /**
     * Load sample data for demonstration
     */
    private void loadSampleData() {
        tableList.clear();

        // Create sample tables with variety
        String[] locations = {"MAIN_FLOOR", "VIP", "OUTDOOR", "BAR_COUNTER"};
        int[] capacities = {2, 4, 6, 8};

        for (int i = 1; i <= 20; i++) {
            String tableNumber = (i <= 5) ? "VIP" + i :
                    (i <= 15) ? "B" + String.format("%02d", i - 5) :
                            "OUT" + (i - 15);

            Table table = new Table(tableNumber, capacities[i % 4], locations[i % 4]);
            table.setId(i);

            // Simulate realistic statuses
            if (i <= 8) {
                table.setStatus("available");
            } else if (i <= 14) {
                table.setStatus("occupied");
                table.occupy(1 + (i % 6), "Khách " + i);
            } else if (i <= 17) {
                table.setStatus("reserved");
                table.reserve("Khách đặt " + i, LocalDateTime.now().plusHours(1), "Đặt trước");
            } else {
                table.setStatus("cleaning");
            }

            tableList.add(table);
        }

        filterTables();
        updateStatistics();
        System.out.println("📋 Sample data loaded: " + tableList.size() + " tables");
    }

    /**
     * Filter tables based on search and filter criteria
     */
    private void filterTables() {
        if (tableList == null || tableList.isEmpty()) {
            displayTables(new ArrayList<>());
            return;
        }

        String searchText = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String statusFilter = filterComboBox != null ? filterComboBox.getValue() : "Tất cả trạng thái";
        String locationFilter = locationFilterComboBox != null ? locationFilterComboBox.getValue() : "Tất cả khu vực";

        filteredTableList = tableList.stream()
                .filter(table -> matchesSearchCriteria(table, searchText))
                .filter(table -> matchesStatusFilter(table, statusFilter))
                .filter(table -> matchesLocationFilter(table, locationFilter))
                .collect(Collectors.toList());

        displayTables(filteredTableList);

        System.out.println("🔍 Filtered: " + filteredTableList.size() + "/" + tableList.size() + " tables");
    }

    /**
     * Check if table matches search criteria
     */
    private boolean matchesSearchCriteria(Table table, String searchText) {
        if (searchText.isEmpty()) return true;

        return table.getTableNumber().toLowerCase().contains(searchText) ||
                table.getStatusDisplay().toLowerCase().contains(searchText) ||
                table.getLocationDisplay().toLowerCase().contains(searchText);
    }

    /**
     * Check if table matches status filter
     */
    private boolean matchesStatusFilter(Table table, String filterValue) {
        switch (filterValue) {
            case "Bàn trống": return table.isAvailable();
            case "Đang sử dụng": return table.isOccupied();
            case "Đã đặt trước": return table.isReserved();
            case "Đang dọn dẹp": return table.isCleaning();
            default: return true;
        }
    }

    /**
     * Check if table matches location filter
     */
    private boolean matchesLocationFilter(Table table, String filterValue) {
        switch (filterValue) {
            case "Sảnh chính": return "MAIN_FLOOR".equals(table.getLocation());
            case "Khu VIP": return "VIP".equals(table.getLocation());
            case "Ngoài trời": return "OUTDOOR".equals(table.getLocation());
            case "Quầy bar": return "BAR_COUNTER".equals(table.getLocation());
            default: return true;
        }
    }

    /**
     * Display tables in the flow pane
     */
    private void displayTables(List<Table> tables) {
        if (tableFlowPane == null) return;

        Platform.runLater(() -> {
            tableFlowPane.getChildren().clear();

            if (tables.isEmpty()) {
                Label noTablesLabel = new Label("Không tìm thấy bàn nào");
                noTablesLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
                tableFlowPane.getChildren().add(noTablesLabel);
                return;
            }

            for (Table table : tables) {
                VBox tableCard = createTableCard(table);
                tableFlowPane.getChildren().add(tableCard);
            }
        });
    }

    /**
     * Create enhanced visual card for a table
     */
    private VBox createTableCard(Table table) {
        VBox card = new VBox(6);
        card.setPrefSize(150, 160);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(10));

        // Enhanced styling with animations
        String baseStyle = "-fx-background-radius: 20; -fx-cursor: hand; " +
                "-fx-border-radius: 20; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 3);";

        String statusStyle = getEnhancedStatusStyle(table.getStatus());
        card.setStyle(baseStyle + statusStyle);

        // Status indicator with glow effect
        Circle statusIndicator = createStatusIndicator(table.getStatus());

        // Table number with better typography
        Label tableNumberLabel = new Label(table.getTableNumber());
        tableNumberLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        tableNumberLabel.setTextFill(Color.WHITE);

        // Capacity and location info
        Label infoLabel = new Label(table.getCapacity() + " chỗ • " + table.getLocationDisplay());
        infoLabel.setFont(Font.font("System", 10));
        infoLabel.setTextFill(Color.LIGHTGRAY);

        // Status label
        Label statusLabel = new Label(table.getStatusDisplay());
        statusLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.WHITE);

        // Additional info section
        VBox additionalInfo = createAdditionalInfo(table);

        // Assembly
        card.getChildren().addAll(statusIndicator, tableNumberLabel, infoLabel, statusLabel, additionalInfo);

        // Enhanced interactions
        setupCardInteractions(card, table, baseStyle, statusStyle);

        return card;
    }

    /**
     * Create status indicator with glow effect
     */
    private Circle createStatusIndicator(String status) {
        Circle indicator = new Circle(15);
        indicator.setFill(getStatusColor(status));
        indicator.setStroke(Color.WHITE);
        indicator.setStrokeWidth(3);

        // Add glow effect
        DropShadow glow = new DropShadow();
        glow.setColor(getStatusColor(status));
        glow.setRadius(8);
        indicator.setEffect(glow);

        return indicator;
    }

    /**
     * Create additional info for table card
     */
    private VBox createAdditionalInfo(Table table) {
        VBox info = new VBox(2);
        info.setAlignment(Pos.CENTER);

        if (table.isOccupied()) {
            if (table.getGuestCount() > 0) {
                Label guestLabel = new Label("👥 " + table.getGuestCount() + " khách");
                guestLabel.setFont(Font.font("System", 9));
                guestLabel.setTextFill(Color.YELLOW);
                info.getChildren().add(guestLabel);
            }

            if (table.getOccupiedSince() != null) {
                Label timeLabel = new Label("⏱️ " + table.getOccupiedDurationFormatted());
                timeLabel.setFont(Font.font("System", 9));
                timeLabel.setTextFill(table.isOverdue() ? Color.RED : Color.ORANGE);
                info.getChildren().add(timeLabel);
            }
        } else if (table.isReserved() && table.getReservedBy() != null) {
            Label reservedLabel = new Label("📅 " + table.getReservedBy());
            reservedLabel.setFont(Font.font("System", 9));
            reservedLabel.setTextFill(Color.LIGHTBLUE);
            info.getChildren().add(reservedLabel);
        }

        return info;
    }

    /**
     * Setup card interactions with animations
     */
    private void setupCardInteractions(VBox card, Table table, String baseStyle, String statusStyle) {
        // Click handler
        card.setOnMouseClicked(e -> handleTableClick(table));

        // Enhanced hover effects
        card.setOnMouseEntered(e -> {
            card.setStyle(baseStyle + statusStyle +
                    "-fx-scale-x: 1.08; -fx-scale-y: 1.08; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 12, 0, 0, 4);");
        });

        card.setOnMouseExited(e -> {
            card.setStyle(baseStyle + statusStyle);
        });

        // Context menu for advanced operations (managers only)
        if (isManagerOrAbove()) {
            ContextMenu contextMenu = createTableContextMenu(table);
            card.setOnContextMenuRequested(e -> {
                contextMenu.show(card, e.getScreenX(), e.getScreenY());
            });
        }
    }

    /**
     * Create context menu for table operations
     */
    private ContextMenu createTableContextMenu(Table table) {
        ContextMenu contextMenu = new ContextMenu();

        if (table.isOccupied()) {
            MenuItem freeTableItem = new MenuItem("🔓 Giải phóng bàn");
            freeTableItem.setOnAction(e -> handleFreeTable(table));
            contextMenu.getItems().add(freeTableItem);

            MenuItem viewDetailsItem = new MenuItem("📋 Xem chi tiết");
            viewDetailsItem.setOnAction(e -> showTableDetails(table));
            contextMenu.getItems().add(viewDetailsItem);
        }

        if (table.isAvailable()) {
            MenuItem reserveItem = new MenuItem("📅 Đặt trước");
            reserveItem.setOnAction(e -> handleReserveTable(table));
            contextMenu.getItems().add(reserveItem);
        }

        if (table.isReserved()) {
            MenuItem cancelReservationItem = new MenuItem("❌ Hủy đặt bàn");
            cancelReservationItem.setOnAction(e -> handleCancelReservation(table));
            contextMenu.getItems().add(cancelReservationItem);
        }

        MenuItem editTableItem = new MenuItem("✏️ Chỉnh sửa bàn");
        editTableItem.setOnAction(e -> handleEditTable(table));
        contextMenu.getItems().add(editTableItem);

        return contextMenu;
    }

    /**
     * Get enhanced status-based styling
     */
    private String getEnhancedStatusStyle(String status) {
        switch (status.toLowerCase()) {
            case "available":
                return "-fx-background-color: linear-gradient(45deg, #27ae60, #2ecc71); " +
                        "-fx-border-color: #27ae60;";
            case "occupied":
                return "-fx-background-color: linear-gradient(45deg, #e74c3c, #c0392b); " +
                        "-fx-border-color: #e74c3c;";
            case "reserved":
                return "-fx-background-color: linear-gradient(45deg, #f39c12, #e67e22); " +
                        "-fx-border-color: #f39c12;";
            case "cleaning":
                return "-fx-background-color: linear-gradient(45deg, #95a5a6, #7f8c8d); " +
                        "-fx-border-color: #95a5a6;";
            default:
                return "-fx-background-color: linear-gradient(45deg, #34495e, #2c3e50); " +
                        "-fx-border-color: #34495e;";
        }
    }

    /**
     * Get status indicator color
     */
    private Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "available": return Color.LIMEGREEN;
            case "occupied": return Color.RED;
            case "reserved": return Color.ORANGE;
            case "cleaning": return Color.GRAY;
            default: return Color.DARKGRAY;
        }
    }

    /**
     * Handle table click with enhanced logic
     */
    private void handleTableClick(Table table) {
        System.out.println("🖱️ Table clicked: " + table.getTableNumber() + " - " + table.getStatusDisplay());

        if (table.isOutOfOrder()) {
            AlertUtil.showWarning("Bàn không khả dụng",
                    "Bàn " + table.getTableNumber() + " hiện tại không thể sử dụng.\n" +
                            "Lý do: " + (table.getNotes() != null ? table.getNotes() : "Không rõ"));
            return;
        }

        switch (table.getStatus().toLowerCase()) {
            case "available":
                showGuestCountDialog(table);
                break;
            case "occupied":
                sceneManager.showOrderScreen(table);
                break;
            case "reserved":
                handleReservedTableClick(table);
                break;
            case "cleaning":
                AlertUtil.showInfo("Bàn đang dọn dẹp",
                        "Bàn " + table.getTableNumber() + " đang được dọn dẹp.\n" +
                                "Vui lòng chờ hoặc chọn bàn khác.");
                break;
        }
    }

    /**
     * Handle reserved table click
     */
    private void handleReservedTableClick(Table table) {
        String message = "Bàn " + table.getTableNumber() + " đã được đặt trước";
        if (table.getReservedBy() != null) {
            message += " bởi " + table.getReservedBy();
        }
        if (table.getReservationTime() != null) {
            message += "\nThời gian: " + DateUtil.formatDateTime(table.getReservationTime());
        }
        message += "\n\nBạn có muốn hủy đặt bàn và sử dụng ngay không?";

        boolean confirm = AlertUtil.showConfirmation("Bàn đã được đặt trước", message);

        if (confirm) {
            table.free();
            updateTableInDatabase(table);
            showGuestCountDialog(table);
        }
    }

    /**
     * Show enhanced guest count dialog
     */
    private void showGuestCountDialog(Table table) {
        List<String> choices = new ArrayList<>();
        for (int i = 1; i <= table.getCapacity(); i++) {
            choices.add(i + " khách");
        }

        Optional<String> result = AlertUtil.showChoiceDialog(
                "Số lượng khách",
                "Chọn số lượng khách cho bàn " + table.getTableNumber() +
                        " (Sức chứa: " + table.getCapacity() + " chỗ):",
                Math.min(2, table.getCapacity()) + " khách",
                choices.toArray(new String[0])
        );

        if (result.isPresent()) {
            String selected = result.get();
            int guestCount = Integer.parseInt(selected.split(" ")[0]);

            // Occupy table
            String notes = "Phục vụ bởi " + getCurrentStaff().getFullName() +
                    " lúc " + DateUtil.formatDateTime(LocalDateTime.now());
            table.occupy(guestCount, notes);

            updateTableInDatabase(table);
            sceneManager.showOrderScreen(table);
        }
    }

    /**
     * Update table in database with better error handling
     */
    private void updateTableInDatabase(Table table) {
        Task<Boolean> updateTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return tableDAO.updateTable(table);
            }

            @Override
            protected void succeeded() {
                if (getValue()) {
                    Platform.runLater(() -> {
                        System.out.println("✅ Table updated: " + table.getTableNumber());
                        // Refresh only if update successful
                        refreshTables();
                    });
                } else {
                    Platform.runLater(() -> {
                        AlertUtil.showError("Cập nhật thất bại",
                                "Không thể cập nhật bàn " + table.getTableNumber());
                    });
                }
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    AlertUtil.showError("Lỗi cập nhật",
                            "Lỗi khi cập nhật bàn " + table.getTableNumber() + ":\n" +
                                    getException().getMessage());
                });
            }
        };

        new Thread(updateTask).start();
    }

    /**
     * Update statistics display with enhanced info
     */
    private void updateStatistics() {
        if (tableList == null || tableList.isEmpty()) return;

        int total = tableList.size();
        int available = (int) tableList.stream().filter(Table::isAvailable).count();
        int occupied = (int) tableList.stream().filter(Table::isOccupied).count();
        int reserved = (int) tableList.stream().filter(Table::isReserved).count();
        int cleaning = (int) tableList.stream().filter(Table::isCleaning).count();

        double occupancyRate = total > 0 ? (double) occupied / total * 100 : 0;

        Platform.runLater(() -> {
            if (totalTablesLabel != null) {
                totalTablesLabel.setText(String.valueOf(total));
            }
            if (availableTablesLabel != null) {
                availableTablesLabel.setText(String.valueOf(available));
            }
            if (occupiedTablesLabel != null) {
                occupiedTablesLabel.setText(String.format("%d (%.1f%%)", occupied, occupancyRate));
            }
            if (reservedTablesLabel != null) {
                reservedTablesLabel.setText(String.valueOf(reserved));
            }
        });
    }

    // Table Management Methods

    @FXML
    private void handleAddTable() {
        if (!validatePermission("manager_above")) return;

        AlertUtil.MultiInputDialog dialog = new AlertUtil.MultiInputDialog(
                "Thêm bàn mới", "Nhập thông tin chi tiết cho bàn mới"
        );

        TextField tableNumberField = dialog.addTextField("Số bàn", "");
        ComboBox<String> capacityField = dialog.addComboBox("Sức chứa", "4", "2", "4", "6", "8", "10", "12");
        ComboBox<String> locationField = dialog.addComboBox("Khu vực", "MAIN_FLOOR",
                "MAIN_FLOOR", "VIP", "OUTDOOR", "BAR_COUNTER");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String tableNumber = tableNumberField.getText().trim();
            String capacity = capacityField.getValue();
            String location = locationField.getValue();

            // Enhanced validation
            String validationError = ValidationUtil.validateTableNumber(tableNumber);
            if (validationError != null) {
                AlertUtil.showValidationError("Số bàn", validationError);
                return;
            }

            // Check for duplicate table numbers
            boolean exists = tableList.stream()
                    .anyMatch(t -> t.getTableNumber().equalsIgnoreCase(tableNumber));
            if (exists) {
                AlertUtil.showValidationError("Số bàn", "Số bàn này đã tồn tại!");
                return;
            }

            Table newTable = new Table(tableNumber, Integer.parseInt(capacity), location);
            addTableToDatabase(newTable);
        }
    }

    /**
     * Add new table to database
     */
    private void addTableToDatabase(Table table) {
        Task<Boolean> addTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return tableDAO.addTable(table);
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (getValue()) {
                        AlertUtil.showSuccess("Thêm bàn " + table.getTableNumber() + " thành công!");
                        refreshTables();
                    } else {
                        AlertUtil.showError("Lỗi thêm bàn", "Không thể thêm bàn mới");
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    AlertUtil.showError("Lỗi thêm bàn",
                            "Lỗi khi thêm bàn: " + getException().getMessage());
                });
            }
        };

        new Thread(addTask).start();
    }

    // Context Menu Actions

    private void handleFreeTable(Table table) {
        boolean confirm = AlertUtil.showConfirmation(
                "Giải phóng bàn",
                "Bạn có chắc muốn giải phóng bàn " + table.getTableNumber() + "?\n" +
                        "Thao tác này sẽ xóa tất cả thông tin hiện tại của bàn."
        );

        if (confirm) {
            table.free();
            updateTableInDatabase(table);
        }
    }

    private void handleReserveTable(Table table) {
        AlertUtil.MultiInputDialog dialog = new AlertUtil.MultiInputDialog(
                "Đặt bàn trước", "Nhập thông tin đặt bàn"
        );

        TextField customerField = dialog.addTextField("Tên khách hàng", "");
        TextField notesField = dialog.addTextField("Ghi chú", "");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String customer = customerField.getText().trim();
            String notes = notesField.getText().trim();

            if (customer.isEmpty()) {
                AlertUtil.showValidationError("Tên khách hàng", "Vui lòng nhập tên khách hàng");
                return;
            }

            table.reserve(customer, LocalDateTime.now().plusHours(1), notes);
            updateTableInDatabase(table);
        }
    }

    private void handleCancelReservation(Table table) {
        boolean confirm = AlertUtil.showConfirmation(
                "Hủy đặt bàn",
                "Bạn có chắc muốn hủy đặt bàn " + table.getTableNumber() + "?"
        );

        if (confirm) {
            table.free();
            updateTableInDatabase(table);
        }
    }

    private void handleEditTable(Table table) {
        AlertUtil.MultiInputDialog dialog = new AlertUtil.MultiInputDialog(
                "Chỉnh sửa bàn", "Chỉnh sửa thông tin bàn " + table.getTableNumber()
        );

        TextField tableNumberField = dialog.addTextField("Số bàn", table.getTableNumber());
        ComboBox<String> capacityField = dialog.addComboBox("Sức chứa",
                String.valueOf(table.getCapacity()), "2", "4", "6", "8", "10", "12");
        ComboBox<String> locationField = dialog.addComboBox("Khu vực",
                table.getLocation(), "MAIN_FLOOR", "VIP", "OUTDOOR", "BAR_COUNTER");

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            String newTableNumber = tableNumberField.getText().trim();
            String newCapacity = capacityField.getValue();
            String newLocation = locationField.getValue();

            table.setTableNumber(newTableNumber);
            table.setCapacity(Integer.parseInt(newCapacity));
            table.setLocation(newLocation);

            updateTableInDatabase(table);
        }
    }

    private void showTableDetails(Table table) {
        AlertUtil.showInfo("Chi tiết bàn " + table.getTableNumber(), table.getDetailedInfo());
    }

    // Utility Methods

    @FXML
    private void refreshTables() {
        System.out.println("🔄 Manual refresh requested");
        loadTablesAsync();
    }

    @Override
    protected void refreshData() {
        refreshTables();
    }

    private void setupAutoRefresh() {
        autoRefreshTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(45), e -> {
                    if (!isLoading) {
                        System.out.println("🔄 Auto-refreshing tables...");
                        loadTablesAsync();
                    }
                })
        );
        autoRefreshTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        autoRefreshTimeline.play();
    }

    private String getRoleDisplayName(String role) {
        switch (role.toLowerCase()) {
            case "admin": return "Quản trị viên";
            case "manager": return "Quản lý";
            case "staff": return "Nhân viên";
            default: return role;
        }
    }

    // Navigation Methods

    @FXML
    private void handleBackToDashboard() {
        // Stop timelines to prevent memory leaks
        if (clockTimeline != null) clockTimeline.stop();
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();

        sceneManager.switchTo(SceneManager.DASHBOARD_SCREEN);
    }

    @FXML
    private void handleLogout() {
        logout();
    }

    // Cleanup
    public void cleanup() {
        if (clockTimeline != null) clockTimeline.stop();
        if (autoRefreshTimeline != null) autoRefreshTimeline.stop();
        System.out.println("🧹 TableScreenController cleanup completed");
    }
}
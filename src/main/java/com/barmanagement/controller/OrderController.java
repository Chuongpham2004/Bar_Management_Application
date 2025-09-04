package com.barmanagement.controller;

import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.dao.RevenueDAO;
import com.barmanagement.model.MenuItem;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.Table;
import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.application.Platform;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Order Controller - COMPLETELY FIXED VERSION
 * Fixed table selection, order loading, and payment flow
 * Added dynamic table creation
 */
public class OrderController {

    // ===== FXML Elements =====
    @FXML private ComboBox<Table> cbTable;
    @FXML private ComboBox<String> cbCategory;
    @FXML private Label selectedTableLabel;
    @FXML private GridPane tableGrid;
    @FXML private VBox menuContainer;
    @FXML private VBox orderContainer;
    @FXML private Spinner<Integer> spQty;
    @FXML private Label lblOrderId, lblTotal;

    // ===== Popup Elements =====
    @FXML private VBox tableStatusPopup;
    @FXML private VBox popupContent;
    @FXML private Label popupTableTitle;
    @FXML private Button btnStatusEmpty;
    @FXML private Button btnStatusOccupied;
    @FXML private Button btnStatusReserved;
    @FXML private Button btnStatusOrdering;

    // ===== NEW: Confirmation Dialog Elements =====
    @FXML private VBox confirmationDialog;
    @FXML private Label confirmationMessage;
    @FXML private Button btnConfirmYes;
    @FXML private Button btnConfirmNo;
    @FXML private Button btnCompleteOrder;
    @FXML private Button btnPayment;

    // ===== Data và Services =====
    private final TableDAO tableDAO = new TableDAO();
    private final MenuItemDAO menuDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();
    private final RevenueDAO revenueDAO = new RevenueDAO();

    private final ObservableList<MenuItem> menuData = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> itemData = FXCollections.observableArrayList();
    private Map<Integer, MenuItem> menuMap = new HashMap<>();

    private Order current;
    private int selectedTableId = -1;
    private int popupTableId = -1;
    private MenuItem selectedMenuItem;

    // Payment flow state
    private boolean orderCompleted = false;

    // Formatter cho tiền tệ
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        System.out.println("🚀 ORDER CONTROLLER INITIALIZING...");
        setupComponents();
        loadData();
        setupEventHandlers();
        setupPopupEvents();
        System.out.println("✅ ORDER CONTROLLER INITIALIZED");
    }

    private void setupComponents() {
        // Setup Spinner
        spQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        // Setup Category ComboBox
        cbCategory.getItems().clear();
        cbCategory.getItems().addAll("Tất cả", "Đồ uống", "Khai vị", "Món chính", "Tráng miệng");
        cbCategory.setValue("Tất cả");

        currencyFormatter.setMaximumFractionDigits(0);

        // Hide payment button initially
        if (btnPayment != null) {
            btnPayment.setVisible(false);
        }

        // Hide confirmation dialog initially
        if (confirmationDialog != null) {
            confirmationDialog.setVisible(false);
        }

        // Initialize empty state
        lblOrderId.setText("(chưa có)");
        lblTotal.setText("0 VNĐ");
        if (selectedTableLabel != null) {
            selectedTableLabel.setText("(Chưa chọn bàn)");
        }
    }

    private void setupPopupEvents() {
        if (popupContent != null) {
            popupContent.setOnMouseClicked(this::preventPopupClose);
        }
    }

    private void loadData() {
        loadTables();
        loadMenu();
        initializeRevenue();
    }

    private void initializeRevenue() {
        try {
            revenueDAO.initTodayRevenue();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupEventHandlers() {
        // Category filter listener
        cbCategory.valueProperty().addListener((obs, oldVal, newVal) -> displayMenuItems());

        // Table selection listener - FIXED
        cbTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> {
                    if (b != null) {
                        selectedTableId = b.getId();
                        loadOrCreatePending(b);
                    }
                });
    }

    private void loadTables() {
        try {
            cbTable.setItems(FXCollections.observableArrayList(tableDAO.findAll()));
            dynamicallyCreateTables();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void refreshTables() {
        Platform.runLater(() -> {
            loadTables();
        });
    }

    /**
     * Tạo động các bàn trong lưới từ dữ liệu cơ sở dữ liệu
     */
    private void dynamicallyCreateTables() {
        if (tableGrid == null) return;

        try {
            // Xóa tất cả các bàn hiện tại
            tableGrid.getChildren().clear();

            // Lấy danh sách bàn từ database
            List<Table> tables = tableDAO.findAll();

            // Số bàn tối đa trên mỗi hàng (có thể điều chỉnh)
            int maxTablesPerRow = 4;

            // Tạo các bàn trong lưới
            for (int i = 0; i < tables.size(); i++) {
                Table table = tables.get(i);

                // Tính toán vị trí
                int row = i / maxTablesPerRow;
                int col = i % maxTablesPerRow;

                // Tạo bàn với ghế
                StackPane tableWithChairs = createTableWithChairs(table);

                // Thêm vào grid
                tableGrid.add(tableWithChairs, col, row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError(e);
        }
    }

    /**
     * Tạo một bàn với ghế xung quanh
     */
    private StackPane createTableWithChairs(Table table) {
        StackPane tableWithChairs = new StackPane();

        // Main table rectangle
        String tableColor = getTableColorByStatus(table.getStatus());

        Rectangle tableRect = new Rectangle(80, 60);
        tableRect.setArcWidth(10);
        tableRect.setArcHeight(10);
        tableRect.setFill(Color.web(tableColor));
        tableRect.setStroke(Color.WHITE);
        tableRect.setStrokeWidth(2);

        // Top chair
        Rectangle topChair = new Rectangle(25, 15);
        topChair.setArcWidth(5);
        topChair.setArcHeight(5);
        topChair.setFill(Color.web(getChairColorByStatus(table.getStatus())));
        topChair.setTranslateY(-37.5);

        // Bottom chair
        Rectangle bottomChair = new Rectangle(25, 15);
        bottomChair.setArcWidth(5);
        bottomChair.setArcHeight(5);
        bottomChair.setFill(Color.web(getChairColorByStatus(table.getStatus())));
        bottomChair.setTranslateY(37.5);

        // Left chair
        Rectangle leftChair = new Rectangle(15, 25);
        leftChair.setArcWidth(5);
        leftChair.setArcHeight(5);
        leftChair.setFill(Color.web(getChairColorByStatus(table.getStatus())));
        leftChair.setTranslateX(-47.5);

        // Right chair
        Rectangle rightChair = new Rectangle(15, 25);
        rightChair.setArcWidth(5);
        rightChair.setArcHeight(5);
        rightChair.setFill(Color.web(getChairColorByStatus(table.getStatus())));
        rightChair.setTranslateX(47.5);

        // Table label
        Label tableLabel = new Label(table.getTableName());
        tableLabel.setTextFill(Color.WHITE);
        tableLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        // Add all to StackPane
        tableWithChairs.getChildren().addAll(tableRect, topChair, bottomChair, leftChair, rightChair, tableLabel);

        // Set user data for table ID
        tableWithChairs.setUserData(String.valueOf(table.getId()));

        // Add click handler
        tableWithChairs.setOnMouseClicked(this::selectTable);

        // Add hover effect
        tableWithChairs.setStyle("-fx-cursor: hand;");

        return tableWithChairs;
    }

    private void refreshTableGrid() {
        if (tableGrid == null) return;
        dynamicallyCreateTables();
    }

    private String getTableColorByStatus(String status) {
        switch (status) {
            case "empty": return "#4CAF50";
            case "occupied": return "#f44336";
            case "reserved": return "#FF9800";
            case "ordering": return "#9C27B0";
            default: return "#4CAF50";
        }
    }

    private String getChairColorByStatus(String status) {
        switch (status) {
            case "empty": return "#2E7D32";
            case "occupied": return "#B71C1C";
            case "reserved": return "#E65100";
            case "ordering": return "#4A148C";
            default: return "#455A64";
        }
    }

    private void loadMenu() {
        try {
            System.out.println("📋 Loading menu items...");
            menuData.setAll(menuDAO.findAll());

            menuMap.clear();
            for (MenuItem item : menuData) {
                menuMap.put(item.getId(), item);
            }

            System.out.println("✅ Menu loaded: " + menuData.size() + " items");
            displayMenuItems();
        } catch (Exception e) {
            System.err.println("❌ Error loading menu: " + e.getMessage());
            e.printStackTrace();
            showError(e);
        }
    }

    private void displayMenuItems() {
        if (menuContainer == null) return;

        menuContainer.getChildren().clear();

        String selectedCategory = cbCategory.getValue();
        List<MenuItem> filteredItems;

        if ("Tất cả".equals(selectedCategory)) {
            filteredItems = new ArrayList<>(menuData);
        } else {
            filteredItems = menuData.stream()
                    .filter(item -> item.getCategory().equals(selectedCategory))
                    .collect(Collectors.toList());
        }

        for (MenuItem item : filteredItems) {
            HBox menuItemBox = createMenuItemUI(item);
            menuContainer.getChildren().add(menuItemBox);
        }

        if (filteredItems.isEmpty() && !"Tất cả".equals(selectedCategory)) {
            Label noItemsLabel = new Label("Không có món nào trong danh mục này");
            noItemsLabel.setTextFill(Color.WHITE);
            noItemsLabel.setFont(Font.font("System", 14));
            noItemsLabel.setStyle("-fx-padding: 20; -fx-alignment: center;");
            menuContainer.getChildren().add(noItemsLabel);
        }
    }

    private HBox createMenuItemUI(MenuItem item) {
        HBox itemBox = new HBox(15);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
        itemBox.setUserData(item);

        // Hover effects
        itemBox.setOnMouseEntered(e ->
                itemBox.setStyle("-fx-background-color: #1a5490; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;"));
        itemBox.setOnMouseExited(e ->
                itemBox.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;"));

        // Click handler
        itemBox.setOnMouseClicked(e -> selectMenuItem(item));

        // Food Image Container
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 8;");
        imageContainer.setPadding(new Insets(5));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(60);
        imageView.setFitWidth(60);
        imageView.setPreserveRatio(true);

        // Load image
        try {
            String imagePath = "/images/menu/" + item.getImagePath();
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                imageView.setImage(createPlaceholderImage());
            } else {
                imageView.setImage(image);
            }
        } catch (Exception e) {
            imageView.setImage(createPlaceholderImage());
        }

        imageContainer.getChildren().add(imageView);

        // Food Info Container
        VBox infoContainer = new VBox(5);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoContainer, javafx.scene.layout.Priority.ALWAYS);

        Label nameLabel = new Label(item.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setTextFill(Color.web("#B0B0B0"));
        categoryLabel.setFont(Font.font("System", 11));

        String description = item.getDescription();
        if (description == null || description.trim().isEmpty()) {
            description = "Món ngon từ " + item.getCategory();
        }
        Label descLabel = new Label(description);
        descLabel.setTextFill(Color.web("#B0B0B0"));
        descLabel.setFont(Font.font("System", 10));
        descLabel.setWrapText(true);

        infoContainer.getChildren().addAll(nameLabel, categoryLabel, descLabel);

        // Price Label
        Label priceLabel = new Label(item.getFormattedPrice());
        priceLabel.setTextFill(Color.web("#4CAF50"));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        itemBox.getChildren().addAll(imageContainer, infoContainer, priceLabel);

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#0f3460"));
        dropShadow.setRadius(3);
        itemBox.setEffect(dropShadow);

        return itemBox;
    }

    private Image createPlaceholderImage() {
        try {
            String[] fallbackImages = {
                    "/images/menu/default.png",
                    "/images/menu/Snack.png",
                    "/images/menu/CocaCola.png",
                    "/images/bar-logo.png"
            };

            for (String path : fallbackImages) {
                try {
                    Image img = new Image(getClass().getResourceAsStream(path));
                    if (!img.isError()) {
                        return img;
                    }
                } catch (Exception e) {
                    // Continue to next fallback
                }
            }
        } catch (Exception e) {
            // Return null if all fails
        }
        return null;
    }

    @FXML
    public void selectMenuItem(MenuItem item) {
        System.out.println("🔍 SELECT MENU ITEM: " + item.getName());

        selectedMenuItem = item;

        // Visual feedback
        for (javafx.scene.Node node : menuContainer.getChildren()) {
            if (node instanceof HBox) {
                HBox hbox = (HBox) node;
                if (hbox.getUserData() == item) {
                    hbox.setStyle("-fx-background-color: #e16428; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
                } else {
                    hbox.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
                }
            }
        }

        // Auto-add nếu có order
        if (current != null) {
            try {
                int quantity = spQty.getValue();
                System.out.println("➕ Adding to order: " + item.getName() + " x" + quantity);

                orderDAO.addItem(current.getId(), selectedMenuItem.getId(), quantity);
                reloadItems();
                showInfo("✅ Đã thêm " + item.getName() + " x" + quantity + " vào order");

                // Reset UI
                spQty.getValueFactory().setValue(1);
                Platform.runLater(() -> {
                    for (javafx.scene.Node node : menuContainer.getChildren()) {
                        if (node instanceof HBox) {
                            HBox hbox = (HBox) node;
                            hbox.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
                        }
                    }
                });

            } catch (SQLException e) {
                System.err.println("❌ Error adding item: " + e.getMessage());
                showError(e);
            }
        } else {
            showInfo("📸 Đã chọn: " + item.getName() + "\n💡 Tạo order trước để thêm món!");
        }
    }

    private void displayOrderItems() {
        System.out.println("🍽️ DISPLAY ORDER ITEMS: " + itemData.size() + " items");

        if (orderContainer == null) return;
        orderContainer.getChildren().clear();

        if (itemData.isEmpty()) {
            Label noItemsLabel = new Label("Chưa có món nào trong order");
            noItemsLabel.setTextFill(Color.web("#B0B0B0"));
            noItemsLabel.setFont(Font.font("System", 14));
            noItemsLabel.setStyle("-fx-alignment: center; -fx-padding: 10;");
            orderContainer.getChildren().add(noItemsLabel);
            return;
        }

        for (OrderItem item : itemData) {
            HBox orderItemBox = createOrderItemUI(item);
            orderContainer.getChildren().add(orderItemBox);
        }
    }

    private HBox createOrderItemUI(OrderItem orderItem) {
        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 8; -fx-padding: 8;");
        itemBox.setUserData(orderItem);

        // Item Image Container
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 5;");
        imageContainer.setPadding(new Insets(3));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(45);
        imageView.setFitWidth(45);
        imageView.setPreserveRatio(true);

        // Get MenuItem từ menuMap để có thông tin ảnh
        MenuItem menuItem = menuMap.get(orderItem.getMenuItemId());

        if (menuItem != null) {
            try {
                String imagePath = "/images/menu/" + menuItem.getImagePath();
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                if (image.isError()) {
                    imageView.setImage(createPlaceholderImage());
                } else {
                    imageView.setImage(image);
                }
            } catch (Exception e) {
                imageView.setImage(createPlaceholderImage());
            }
        } else {
            imageView.setImage(createPlaceholderImage());
        }

        imageContainer.getChildren().add(imageView);

        // Item Info Container
        VBox infoContainer = new VBox(2);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoContainer, javafx.scene.layout.Priority.ALWAYS);

        // Tên món
        String itemName = orderItem.getDisplayName();
        if (menuItem != null && (itemName == null || itemName.startsWith("Món #"))) {
            itemName = menuItem.getName();
        }

        Label nameLabel = new Label(itemName);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        // Chi tiết món
        HBox detailBox = new HBox(10);
        Label qtyLabel = new Label("SL: " + orderItem.getQuantity());
        qtyLabel.setTextFill(Color.web("#B0B0B0"));
        qtyLabel.setFont(Font.font("System", 10));

        Label priceLabel = new Label("Đơn giá: " + orderItem.getFormattedPrice());
        priceLabel.setTextFill(Color.web("#B0B0B0"));
        priceLabel.setFont(Font.font("System", 10));

        detailBox.getChildren().addAll(qtyLabel, priceLabel);
        infoContainer.getChildren().addAll(nameLabel, detailBox);

        // Subtotal and Remove Button Container
        VBox actionContainer = new VBox(5);
        actionContainer.setAlignment(Pos.CENTER_RIGHT);

        Label subtotalLabel = new Label(orderItem.getFormattedSubtotal());
        subtotalLabel.setTextFill(Color.web("#4CAF50"));
        subtotalLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        Button removeBtn = new Button("×");
        removeBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 8px;");
        removeBtn.setPrefHeight(20);
        removeBtn.setPrefWidth(20);
        removeBtn.setOnAction(e -> removeOrderItem(orderItem));

        actionContainer.getChildren().addAll(subtotalLabel, removeBtn);

        itemBox.getChildren().addAll(imageContainer, infoContainer, actionContainer);

        // Add drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#0f3460"));
        dropShadow.setRadius(2);
        itemBox.setEffect(dropShadow);

        return itemBox;
    }

    // ===== Event Handlers cho Bàn =====

    @FXML
    public void selectTable(MouseEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        String tableIdStr = (String) source.getUserData();

        if (tableIdStr != null) {
            try {
                int tableId = Integer.parseInt(tableIdStr);

                if (event.getButton() == MouseButton.PRIMARY) {
                    selectTableForOrder(tableId);
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    showTableStatusPopup(tableId);
                }

            } catch (NumberFormatException e) {
                showInfo("Lỗi chọn bàn: " + tableIdStr);
            }
        }
    }

    private void showTableStatusPopup(int tableId) {
        popupTableId = tableId;
        popupTableTitle.setText("Chọn trạng thái cho Bàn " + tableId);
        tableStatusPopup.setVisible(true);
        tableStatusPopup.toFront();
    }

    @FXML
    public void hideTableStatusPopup() {
        tableStatusPopup.setVisible(false);
        popupTableId = -1;
    }

    @FXML
    public void preventPopupClose(MouseEvent event) {
        event.consume();
    }

    // ===== Popup Status Button Handlers =====

    @FXML
    public void setTableStatusEmpty() {
        updateTableStatus("empty");
    }

    @FXML
    public void setTableStatusOccupied() {
        updateTableStatus("occupied");
    }

    @FXML
    public void setTableStatusReserved() {
        updateTableStatus("reserved");
    }

    @FXML
    public void setTableStatusOrdering() {
        updateTableStatus("ordering");
    }

    private void updateTableStatus(String newStatus) {
        if (popupTableId == -1) return;

        try {
            tableDAO.updateStatus(popupTableId, newStatus);
            showInfo("✅ Đã cập nhật bàn " + popupTableId + " thành: " + getStatusDisplayName(newStatus));

            refreshTableGrid();
            hideTableStatusPopup();

        } catch (SQLException e) {
            showError(e);
        }
    }

    private String getStatusDisplayName(String status) {
        switch (status) {
            case "empty": return "Trống";
            case "occupied": return "Đang sử dụng";
            case "reserved": return "Đặt trước";
            case "ordering": return "Đang chọn";
            default: return status;
        }
    }

    /**
     * COMPLETELY FIXED: Select table for order with proper completed order handling
     */
    private void selectTableForOrder(int tableId) {
        try {
            System.out.println("🎯 SELECTING TABLE FOR ORDER: " + tableId);

            // Get current table status
            List<Table> tables = tableDAO.findAll();
            Table table = tables.stream()
                    .filter(t -> t.getId() == tableId)
                    .findFirst()
                    .orElse(null);

            if (table == null) {
                showInfo("Không tìm thấy thông tin bàn!");
                return;
            }

            System.out.println("📊 Table " + tableId + " status: " + table.getStatus());

            // CRITICAL FIX: Check for ANY existing order for this table (pending, completed, etc.)
            Order existingOrder = orderDAO.findPendingByTable(tableId);

            if (existingOrder != null) {
                System.out.println("🔍 Found existing order: #" + existingOrder.getId() + " status: " + existingOrder.getStatus());

                if ("completed".equals(existingOrder.getStatus())) {
                    // Show completed order ready for payment
                    selectTableWithOrder(table, existingOrder);
                    showInfo("✅ Bàn " + tableId + " có đơn hàng hoàn thành sẵn sàng thanh toán!");
                    return;
                } else if ("pending".equals(existingOrder.getStatus())) {
                    // Show pending order for editing
                    selectTableWithOrder(table, existingOrder);
                    showInfo("📝 Tiếp tục chỉnh sửa đơn hàng #" + existingOrder.getId());
                    return;
                }
            }

            // Handle occupied tables without order
            if ("occupied".equals(table.getStatus())) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Thông tin");
                alert.setHeaderText("Bàn đang được sử dụng");
                alert.setContentText("Bàn này đã có khách nhưng chưa có đơn hàng trong hệ thống.\nBạn có muốn tạo đơn hàng mới không?");

                ButtonType createOrder = new ButtonType("Tạo Order Mới");
                ButtonType cancel = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
                alert.getButtonTypes().setAll(createOrder, cancel);

                alert.showAndWait().ifPresent(response -> {
                    if (response == createOrder) {
                        proceedWithTableSelection(table, tableId);
                    }
                });
                return;
            }

            // Proceed with normal table selection
            proceedWithTableSelection(table, tableId);

        } catch (SQLException e) {
            System.err.println("❌ Error selecting table: " + e.getMessage());
            showError(e);
        }
    }

    /**
     * HELPER: Proceed with table selection after validation
     */
    private void proceedWithTableSelection(Table table, int tableId) {
        try {
            // Reset previous table if needed
            if (selectedTableId != -1 && selectedTableId != tableId && current == null) {
                tableDAO.updateStatus(selectedTableId, "empty");
            }

            // Update selected table
            selectedTableId = tableId;

            // Set to ordering if it's empty
            if ("empty".equals(table.getStatus())) {
                tableDAO.updateStatus(tableId, "ordering");
            }

            // Update UI
            if (selectedTableLabel != null) {
                selectedTableLabel.setText("Đã chọn bàn " + tableId);
            }

            // Find and select in ComboBox
            for (Table t : cbTable.getItems()) {
                if (t.getId() == tableId) {
                    cbTable.getSelectionModel().select(t);
                    break;
                }
            }

            // Clear current order state
            current = null;
            lblOrderId.setText("(chưa có)");
            lblTotal.setText("0 VNĐ");
            itemData.clear();
            displayOrderItems();
            orderCompleted = false;
            updatePaymentButtonVisibility();

            refreshTableGrid();
            loadMenuByCategory();
            showInfo("✅ Đã chọn bàn " + tableId + " để tạo order");

        } catch (SQLException e) {
            showError(e);
        }
    }

    /**
     * HELPER: Select table with existing order
     */
    private void selectTableWithOrder(Table table, Order order) {
        selectedTableId = table.getId();
        current = order;

        // Update UI
        if (selectedTableLabel != null) {
            selectedTableLabel.setText("Đã chọn bàn " + table.getId());
        }

        cbTable.getSelectionModel().select(table);

        // Load order data
        lblOrderId.setText("#" + order.getId());
        reloadItems();

        // Check order status for payment button
        orderCompleted = "completed".equals(order.getStatus());
        updatePaymentButtonVisibility();

        loadMenuByCategory();
        refreshTableGrid();

        System.out.println("✅ Loaded existing order: #" + order.getId() + " (" + order.getStatus() + ")");
    }

    private void loadMenuByCategory() {
        if (selectedTableId == -1) {
            if (menuContainer != null) {
                menuContainer.getChildren().clear();
            }
            return;
        }
        if (menuContainer != null && !menuData.isEmpty()) {
            displayMenuItems();
        }
    }

    // ===== Order Management Methods =====

    @FXML
    public void addItem() {
        if (current == null) {
            showInfo("Hãy tạo order trước khi thêm món!");
            return;
        }
        if (selectedMenuItem == null) {
            showInfo("Vui lòng chọn món từ menu!");
            return;
        }
        try {
            orderDAO.addItem(current.getId(), selectedMenuItem.getId(), spQty.getValue());
            reloadItems();
            showInfo("✅ Đã thêm " + selectedMenuItem.getName() + " x" + spQty.getValue());
            spQty.getValueFactory().setValue(1);
        } catch (SQLException e) {
            showError(e);
        }
    }

    @FXML
    public void removeOrderItem(OrderItem orderItem) {
        try {
            orderDAO.removeItem(orderItem.getId());
            reloadItems();
            showInfo("✅ Đã xóa món khỏi order!");
        } catch (SQLException e) {
            showError(e);
        }
    }

    @FXML
    public void clearOrder() {
        if (current == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận");
        alert.setHeaderText("Xóa tất cả món trong order?");
        alert.setContentText("Hành động này không thể hoàn tác.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    for (OrderItem item : itemData) {
                        orderDAO.removeItem(item.getId());
                    }
                    reloadItems();
                    showInfo("✅ Đã xóa tất cả món!");
                } catch (SQLException e) {
                    showError(e);
                }
            }
        });
    }

    @FXML
    public void newOrder() {
        if (selectedTableId == -1) {
            showInfo("Vui lòng chọn bàn trước!");
            return;
        }

        try {
            Table table = tableDAO.findAll().stream()
                    .filter(t -> t.getId() == selectedTableId)
                    .findFirst()
                    .orElse(null);

            if (table == null) {
                showInfo("Không tìm thấy thông tin bàn!");
                return;
            }

            Integer id = orderDAO.createEmptyOrder(table.getId());
            if (id != null) {
                tableDAO.updateStatus(table.getId(), "occupied");

                current = new Order();
                current.setId(id);
                current.setTableId(table.getId());
                current.setStatus("pending");

                lblOrderId.setText("#" + id);
                reloadItems();
                refreshTableGrid();

                orderCompleted = false;
                updatePaymentButtonVisibility();

                showInfo("✅ Đã tạo order mới #" + id + " cho bàn " + table.getTableName() +
                        "\n🍽️ Bây giờ bạn có thể chọn món từ menu!");
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    @FXML
    public void completeOrder() {
        if (current == null) {
            showInfo("Không có order nào để hoàn thành!");
            return;
        }

        if (itemData.isEmpty()) {
            showInfo("Order này chưa có món nào! Vui lòng thêm món trước khi hoàn thành.");
            return;
        }

        showOrderCompletionDialog();
    }

    private void showOrderCompletionDialog() {
        if (confirmationDialog != null) {
            double totalAmount = parseTotalAmount();
            String message = "Bạn có chắc chắn muốn hoàn thành order này không?\n\n" +
                    "Order #" + current.getId() + "\n" +
                    "Tổng tiền: " + formatCurrency(totalAmount) + "\n\n" +
                    "Sau khi hoàn thành, sẽ có nút thanh toán xuất hiện.";

            confirmationMessage.setText(message);
            confirmationDialog.setVisible(true);
            confirmationDialog.toFront();
        }
    }

    @FXML
    public void confirmOrderCompletion() {
        hideConfirmationDialog();

        try {
            orderDAO.markCompleted(current.getId());
            orderCompleted = true;
            updatePaymentButtonVisibility();
            showInfo("✅ Order đã hoàn thành! Bây giờ bạn có thể thanh toán.");

        } catch (SQLException e) {
            showError(e);
        }
    }

    @FXML
    public void cancelOrderCompletion() {
        hideConfirmationDialog();
    }

    private void hideConfirmationDialog() {
        if (confirmationDialog != null) {
            confirmationDialog.setVisible(false);
        }
    }

    private void updatePaymentButtonVisibility() {
        System.out.println("🔄 UPDATING PAYMENT BUTTON VISIBILITY");
        System.out.println("orderCompleted: " + orderCompleted);
        System.out.println("current order: " + (current != null ? "Order #" + current.getId() : "null"));

        if (btnPayment != null && btnCompleteOrder != null) {
            if (orderCompleted && current != null) {
                btnPayment.setVisible(true);
                btnCompleteOrder.setText("✅ Đã Hoàn Thành");
                btnCompleteOrder.setStyle("-fx-background-color: #607D8B; -fx-text-fill: white; -fx-background-radius: 8;");
                btnCompleteOrder.setDisable(true);
                System.out.println("✅ Payment button VISIBLE");
            } else {
                btnPayment.setVisible(false);
                btnCompleteOrder.setText("✅ Hoàn Thành");
                btnCompleteOrder.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8;");
                btnCompleteOrder.setDisable(false);
                System.out.println("❌ Payment button HIDDEN");
            }
        }
    }

    @FXML
    public void goToPayment() {
        if (current == null || !orderCompleted) {
            showInfo("Vui lòng hoàn thành order trước khi thanh toán!");
            return;
        }

        try {
            System.out.println("🧾 Navigating to payment for order: " + current.getId());
            SceneUtil.openScene("/fxml/payment.fxml", lblTotal);
            showInfo("Đã chuyển đến trang thanh toán. Vui lòng chọn bàn " + current.getTableId() + " - Đơn #" + current.getId());

        } catch (Exception e) {
            System.err.println("❌ Error navigating to payment: " + e.getMessage());
            showInfo("Lỗi khi chuyển đến trang thanh toán: " + e.getMessage());
        }
    }

    private double parseTotalAmount() {
        String totalText = lblTotal.getText().replaceAll("[^0-9]", "");
        if (totalText.isEmpty()) return 0;
        return Double.parseDouble(totalText);
    }

    @FXML
    public void reload() {
        afterComplete();
        loadData();
        showInfo("🔄 Đã làm mới dữ liệu!");
    }

    // ===== Navigation Methods =====
    @FXML
    private void goBack() {
        SceneUtil.openScene("/fxml/dashboard.fxml", lblTotal);
    }

    @FXML
    private void showPayment() {
        SceneUtil.openScene("/fxml/payment.fxml", lblTotal);
    }

    @FXML
    private void showMenu() {
        SceneUtil.openScene("/fxml/menu_management.fxml", lblTotal);
    }

    @FXML
    private void handleLogout() {
        LogoutUtil.confirmLogout(lblTotal);
    }

    @FXML
    private void showAllOrders() {
        showInfo("Chức năng xem tất cả orders sẽ được phát triển trong phiên bản tới!");
    }

    @FXML
    private void showReports() {
        SceneUtil.openScene("/fxml/revenue_report.fxml", lblTotal);
    }

    @FXML
    private void showTableManagement() {
        SceneUtil.openScene("/fxml/table_management.fxml", lblTotal);
    }

    // ===== Helper Methods =====

    /**
     * CRITICAL FIX: Load or create pending orders correctly
     */
    private void loadOrCreatePending(Table t) {
        if (t == null) return;

        try {
            System.out.println("🔍 LOADING ORDER FOR TABLE: " + t.getId());

            current = orderDAO.findPendingByTable(t.getId());

            if (current == null) {
                System.out.println("❌ No existing order found for table " + t.getId());
                lblOrderId.setText("(chưa có)");
                itemData.clear();
                lblTotal.setText("0 VNĐ");
                displayOrderItems();
                orderCompleted = false;
                updatePaymentButtonVisibility();
                showInfo("💡 Bàn này chưa có order. Nhấn 'Tạo Order Mới' để bắt đầu!");
            } else {
                System.out.println("✅ Found existing order: #" + current.getId() + " (" + current.getStatus() + ")");
                lblOrderId.setText("#" + current.getId());
                reloadItems();
                orderCompleted = "completed".equals(current.getStatus());
                updatePaymentButtonVisibility();

                if (orderCompleted) {
                    showInfo("🎯 Đơn hàng #" + current.getId() + " đã hoàn thành, sẵn sàng thanh toán!");
                }
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    /**
     * ENHANCED: Reload items with better error handling
     */
    private void reloadItems() {
        System.out.println("🔄 RELOADING ORDER ITEMS");

        try {
            if (current == null) {
                System.out.println("❌ No current order");
                itemData.clear();
                displayOrderItems();
                return;
            }

            List<OrderItem> freshItems = orderDAO.findItems(current.getId());
            System.out.println("📋 Loaded " + freshItems.size() + " items from database");

            itemData.setAll(freshItems);

            BigDecimal total = orderDAO.calcTotal(current.getId());
            lblTotal.setText(formatCurrency(total.doubleValue()));

            Platform.runLater(() -> {
                displayOrderItems();
            });

            System.out.println("✅ Order items reloaded successfully, total: " + total);

        } catch (SQLException e) {
            System.err.println("❌ Error reloading items: " + e.getMessage());
            showError(e);
        }
    }

    private void afterComplete() {
        itemData.clear();
        lblOrderId.setText("(chưa có)");
        lblTotal.setText("0 VNĐ");
        selectedTableId = -1;
        selectedMenuItem = null;
        orderCompleted = false;
        updatePaymentButtonVisibility();
        current = null;

        if (selectedTableLabel != null) {
            selectedTableLabel.setText("(Chưa chọn bàn)");
        }
        displayOrderItems();
        loadTables();
    }

    private String formatCurrency(double amount) {
        return currencyFormatter.format(amount) + " VNĐ";
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText("Có lỗi xảy ra");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        e.printStackTrace();
    }
}
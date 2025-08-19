package com.barmanagement.controller;

import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.dao.RevenueDAO;
import com.barmanagement.dao.JDBCConnect;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private int popupTableId = -1; // Bàn đang hiển thị popup
    private MenuItem selectedMenuItem;

    // Formatter cho tiền tệ
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        setupComponents();
        loadData();
        setupEventHandlers();
        setupPopupEvents();
    }

    private void setupComponents() {
        // Setup Spinner
        spQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        // Setup Category ComboBox - FIX: dùng đúng tên category từ database
        cbCategory.getItems().clear();
        cbCategory.getItems().addAll("Tất cả", "Đồ uống", "Khai vị", "Món chính", "Tráng miệng");
        cbCategory.setValue("Tất cả");

        currencyFormatter.setMaximumFractionDigits(0);
    }

    private void setupPopupEvents() {
        // Đảm bảo popup không bị đóng khi click vào nội dung
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

        // Table selection listener
        cbTable.getSelectionModel().selectedItemProperty()
                .addListener((o, a, b) -> loadOrCreatePending(b));
    }

    private void loadTables() {
        try {
            cbTable.setItems(FXCollections.observableArrayList(tableDAO.findAll()));
            refreshTableGrid();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void refreshTables() {
        Platform.runLater(() -> {
            loadTables();
        });
    }

    private void refreshTableGrid() {
        if (tableGrid == null) return;

        try {
            List<Table> tables = tableDAO.findAll();
            // Cập nhật trạng thái visual cho các bàn trong grid
            for (javafx.scene.Node node : tableGrid.getChildren()) {
                if (node.getUserData() != null) {
                    String tableIdStr = (String) node.getUserData();
                    try {
                        int tableId = Integer.parseInt(tableIdStr);
                        Table table = tables.stream()
                                .filter(t -> t.getId() == tableId)
                                .findFirst()
                                .orElse(null);

                        if (table != null) {
                            updateTableVisualStyle(node, getTableColorByStatus(table.getStatus()));
                        }
                    } catch (NumberFormatException e) {
                        // Ignore invalid table IDs
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    // Fix method loadMenu() để đảm bảo menuMap được populate
    private void loadMenu() {
        try {
            System.out.println("Loading menu items...");
            menuData.setAll(menuDAO.findAll());

            // Populate menuMap - QUAN TRỌNG cho việc hiển thị order items
            menuMap.clear();
            for (MenuItem item : menuData) {
                menuMap.put(item.getId(), item);
                System.out.println("Added to menuMap: ID=" + item.getId() + ", Name=" + item.getName() + ", ImagePath=" + item.getImagePath());
            }

            System.out.println("Menu loaded: " + menuData.size() + " items, menuMap size: " + menuMap.size());
            displayMenuItems();
        } catch (Exception e) {
            System.err.println("Error loading menu: " + e.getMessage());
            e.printStackTrace();
            showError(e);
        }
    }

    // Thay thế method displayMenuItems()
    private void displayMenuItems() {
        if (menuContainer == null) return;

        menuContainer.getChildren().clear();

        String selectedCategory = cbCategory.getValue();
        System.out.println("Selected category: " + selectedCategory); // Debug log

        List<MenuItem> filteredItems;

        if ("Tất cả".equals(selectedCategory)) {
            filteredItems = new ArrayList<>(menuData);
        } else {
            filteredItems = menuData.stream()
                    .filter(item -> {
                        boolean matches = item.getCategory().equals(selectedCategory);
                        System.out.println("Item: " + item.getName() + ", Category: " + item.getCategory() + ", Matches: " + matches);
                        return matches;
                    })
                    .collect(Collectors.toList());
        }

        System.out.println("Filtered items count: " + filteredItems.size()); // Debug log

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

    // Thay thế method createMenuItemUI() - fix image loading
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

        // Load image - FIX: xử lý path chính xác
        try {
            String imagePath = "/images/menu/" + item.getImagePath();
            System.out.println("Loading image: " + imagePath); // Debug log

            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                System.out.println("Image error for: " + imagePath);
                imageView.setImage(createPlaceholderImage());
            } else {
                imageView.setImage(image);
            }
        } catch (Exception e) {
            System.out.println("Exception loading image for: " + item.getName() + " - " + e.getMessage());
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

    // Fix method createPlaceholderImage()
    private Image createPlaceholderImage() {
        try {
            // Thử các ảnh có sẵn
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

            // Tạo ảnh đơn giản bằng code nếu không có ảnh nào
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    // Fix method selectMenuItem() để đảm bảo order item hiển thị ngay
    @FXML
    public void selectMenuItem(MenuItem item) {
        System.out.println("=== SELECT MENU ITEM ===");
        System.out.println("Selected item: " + item.getName() + " (ID: " + item.getId() + ")");

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
                System.out.println("Adding to order: " + item.getName() + " x" + quantity);

                orderDAO.addItem(current.getId(), selectedMenuItem.getId(), quantity);

                // Reload items NGAY LẬP TỨC
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
                System.err.println("Error adding item: " + e.getMessage());
                e.printStackTrace();
                showError(e);
            }
        } else {
            showInfo("🔸 Đã chọn: " + item.getName() + "\n💡 Tạo order trước để thêm món!");
        }
    }

    private void displayOrderItems() {
        System.out.println("=== DISPLAY ORDER ITEMS ===");
        System.out.println("Number of items to display: " + itemData.size());

        if (orderContainer == null) {
            System.out.println("orderContainer is null!");
            return;
        }

        orderContainer.getChildren().clear();

        for (OrderItem item : itemData) {
            System.out.println("Creating UI for item: " + item.getDisplayName() + " x" + item.getQuantity());
            HBox orderItemBox = createOrderItemUI(item);
            orderContainer.getChildren().add(orderItemBox);
        }

        System.out.println("Order items displayed successfully!");
        System.out.println("=== END DISPLAY ORDER ITEMS ===");
    }

    // Thay thế method createOrderItemUI() trong OrderController.java
    private HBox createOrderItemUI(OrderItem orderItem) {
        System.out.println("Creating UI for OrderItem: " + orderItem.getDisplayName());

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

        // FIX: Lấy MenuItem từ menuMap để có thông tin ảnh
        MenuItem menuItem = menuMap.get(orderItem.getMenuItemId());
        System.out.println("Found MenuItem for OrderItem: " + (menuItem != null ? menuItem.getName() : "null"));

        if (menuItem != null) {
            try {
                // Sử dụng imagePath từ MenuItem
                String imagePath = "/images/menu/" + menuItem.getImagePath();
                System.out.println("Loading order item image: " + imagePath);

                Image image = new Image(getClass().getResourceAsStream(imagePath));
                if (image.isError()) {
                    System.out.println("Image error, using placeholder");
                    imageView.setImage(createPlaceholderImage());
                } else {
                    imageView.setImage(image);
                    System.out.println("Image loaded successfully");
                }
            } catch (Exception e) {
                System.out.println("Exception loading order item image: " + e.getMessage());
                imageView.setImage(createPlaceholderImage());
            }
        } else {
            System.out.println("MenuItem not found in menuMap for ID: " + orderItem.getMenuItemId());
            imageView.setImage(createPlaceholderImage());
        }

        imageContainer.getChildren().add(imageView);

        // Item Info Container
        VBox infoContainer = new VBox(2);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoContainer, javafx.scene.layout.Priority.ALWAYS);

        // Tên món - sử dụng tên từ OrderItem hoặc MenuItem
        String itemName = orderItem.getDisplayName();
        if (menuItem != null && (itemName == null || itemName.startsWith("Món #"))) {
            itemName = menuItem.getName();
        }

        Label nameLabel = new Label(itemName);
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        // Chi tiết món (số lượng và giá đơn vị)
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
                    // Click trái - chọn bàn để order
                    selectTableForOrder(tableId);
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    // Click phải - hiển thị popup trạng thái
                    showTableStatusPopup(tableId);
                }

            } catch (NumberFormatException e) {
                showInfo("Lỗi chọn bàn: " + tableIdStr);
            }
        }
    }

    // ===== Popup Management =====

    private void showTableStatusPopup(int tableId) {
        popupTableId = tableId;
        popupTableTitle.setText("Chọn trạng thái cho Bàn " + tableId);
        tableStatusPopup.setVisible(true);

        // Đưa popup lên trên cùng
        tableStatusPopup.toFront();
    }

    @FXML
    public void hideTableStatusPopup() {
        tableStatusPopup.setVisible(false);
        popupTableId = -1;
    }

    @FXML
    public void preventPopupClose(MouseEvent event) {
        // Ngăn việc đóng popup khi click vào nội dung
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

            // Refresh table grid
            refreshTableGrid();

            // Đóng popup
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

    private void selectTableForOrder(int tableId) {
        try {
            // Kiểm tra trạng thái bàn từ database
            List<Table> tables = tableDAO.findAll();
            Table table = tables.stream()
                    .filter(t -> t.getId() == tableId)
                    .findFirst()
                    .orElse(null);

            if (table == null) {
                showInfo("Không tìm thấy thông tin bàn!");
                return;
            }

            // Kiểm tra trạng thái bàn
            if ("occupied".equals(table.getStatus())) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Cảnh báo");
                alert.setHeaderText("Bàn đang được sử dụng");
                alert.setContentText("Bàn này đã có khách. Vui lòng chọn bàn khác hoặc thay đổi trạng thái bàn!");
                alert.showAndWait();
                return;
            }

            // Reset bàn cũ nếu có
            if (selectedTableId != -1 && selectedTableId != tableId && current == null) {
                tableDAO.updateStatus(selectedTableId, "empty");
            }

            // Cập nhật bàn mới
            selectedTableId = tableId;
            tableDAO.updateStatus(tableId, "ordering");

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

            refreshTableGrid();
            loadMenuByCategory();
            showInfo("✅ Đã chọn bàn " + tableId + " để tạo order");

        } catch (SQLException e) {
            showError(e);
        }
    }

    private void loadMenuByCategory() {
        if (selectedTableId == -1) {
            menuContainer.getChildren().clear();
            return;
        }
        displayMenuItems();
    }

    private void updateTableVisualStyle(javafx.scene.Node node, String color) {
        if (node instanceof javafx.scene.layout.StackPane) {
            javafx.scene.layout.StackPane stackPane = (javafx.scene.layout.StackPane) node;
            for (javafx.scene.Node child : stackPane.getChildren()) {
                if (child instanceof javafx.scene.shape.Rectangle) {
                    javafx.scene.shape.Rectangle rect = (javafx.scene.shape.Rectangle) child;
                    rect.setFill(Color.web(color));
                }
            }
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

            // Reset quantity về 1 sau khi thêm
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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận thanh toán");
        confirm.setHeaderText("Hoàn thành order #" + current.getId());
        confirm.setContentText("Bạn có chắc chắn muốn hoàn thành order này và thanh toán?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Tính tổng tiền trước khi complete
                    BigDecimal total = orderDAO.calcTotal(current.getId());

                    // Complete order (sẽ tự động tạo payment và giải phóng bàn)
                    orderDAO.complete(current.getId());

                    // Cập nhật revenue
                    revenueDAO.updateDailyRevenue(LocalDate.now(), total);

                    // Reset UI
                    current = null;
                    afterComplete();

                    showInfo("✅ Đã hoàn thành order và thanh toán thành công!\n💰 Tổng tiền: " +
                            formatCurrency(total.doubleValue()));

                } catch (SQLException e) {
                    showError(e);
                }
            }
        });
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

    private void loadOrCreatePending(Table t) {
        if (t == null) return;
        try {
            current = orderDAO.findPendingByTable(t.getId());
            if (current == null) {
                lblOrderId.setText("(chưa có)");
                itemData.clear();
                lblTotal.setText("0 VNĐ");
                displayOrderItems();
            } else {
                lblOrderId.setText("#" + current.getId());
                reloadItems();
            }
        } catch (SQLException e) {
            showError(e);
        }
    }

    // Fix method reloadItems() với force refresh
    private void reloadItems() {
        System.out.println("=== RELOAD ORDER ITEMS ===");

        try {
            if (current == null) {
                System.out.println("No current order");
                itemData.clear();
                displayOrderItems();
                return;
            }

            // Force reload từ database
            List<OrderItem> freshItems = orderDAO.findItems(current.getId());
            System.out.println("Loaded " + freshItems.size() + " items from database");

            // Debug: in ra thông tin từng item
            for (OrderItem item : freshItems) {
                System.out.println("OrderItem: ID=" + item.getId() +
                        ", MenuItemID=" + item.getMenuItemId() +
                        ", Name=" + item.getDisplayName() +
                        ", Qty=" + item.getQuantity());
            }

            itemData.setAll(freshItems);

            BigDecimal total = orderDAO.calcTotal(current.getId());
            lblTotal.setText(formatCurrency(total.doubleValue()));

            // Force refresh UI
            Platform.runLater(() -> {
                displayOrderItems();
            });

            System.out.println("Order items reloaded successfully, total: " + total);

        } catch (SQLException e) {
            System.err.println("Error reloading items: " + e.getMessage());
            e.printStackTrace();
            showError(e);
        }
    }

    private void afterComplete() {
        itemData.clear();
        lblOrderId.setText("(chưa có)");
        lblTotal.setText("0 VNĐ");
        selectedTableId = -1;
        selectedMenuItem = null;
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
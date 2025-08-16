package com.barmanagement.controller;

import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.TableDAO;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class OrderController {

    // ===== FXML Elements từ thiết kế mới =====
    @FXML private ComboBox<Table> cbTable;
    @FXML private ComboBox<String> cbCategory;
    @FXML private Label selectedTableLabel;
    @FXML private GridPane tableGrid;

    // Menu và Order containers (VBox thay vì TableView)
    @FXML private VBox menuContainer;
    @FXML private VBox orderContainer;

    @FXML private Spinner<Integer> spQty;
    @FXML private Label lblOrderId, lblTotal;

    // ===== Data và Services =====
    private final TableDAO tableDAO = new TableDAO();
    private final MenuItemDAO menuDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private final ObservableList<MenuItem> menuData = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> itemData = FXCollections.observableArrayList();
    private Map<Integer, MenuItem> menuMap = new HashMap<>();

    private Order current;
    private int selectedTableId = -1;
    private MenuItem selectedMenuItem;

    // Formatter cho tiền tệ
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    @FXML
    public void initialize() {
        setupComponents();
        loadData();
        setupEventHandlers();
    }

    private void setupComponents() {
        // Setup Spinner
        spQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        // Setup Category ComboBox
        cbCategory.getItems().addAll("Tất cả", "Đồ uống", "Món chính", "Tráng miệng", "Khai vị");
        cbCategory.setValue("Tất cả");

        currencyFormatter.setMaximumFractionDigits(0);
    }

    private void loadData() {
        loadTables();
        loadMenu();
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
        } catch (SQLException e) {
            showError(e);
        }
    }

    private void loadMenu() {
        try {
            menuData.setAll(menuDAO.findAll());
            menuMap = menuData.stream().collect(Collectors.toMap(MenuItem::getId, m -> m));
            displayMenuItems();
        } catch (Exception e) {
            showError(e);
        }
    }

    /**
     * Hiển thị menu items dưới dạng custom UI với ảnh
     */
    private void displayMenuItems() {
        if (menuContainer == null) return;

        menuContainer.getChildren().clear();

        String selectedCategory = cbCategory.getValue();
        List<MenuItem> filteredItems = menuData.stream()
                .filter(item -> selectedCategory.equals("Tất cả") ||
                        item.getCategory().equals(selectedCategory))
                .collect(Collectors.toList());

        for (MenuItem item : filteredItems) {
            HBox menuItemBox = createMenuItemUI(item);
            menuContainer.getChildren().add(menuItemBox);
        }
    }

    /**
     * Tạo UI cho một menu item với ảnh
     */
    private HBox createMenuItemUI(MenuItem item) {
        HBox itemBox = new HBox(15);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 10; -fx-padding: 10; -fx-cursor: hand;");
        itemBox.setUserData(item);

        // Thêm hover effect
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

        // Load image (với fallback nếu không tìm thấy)
        try {
            String imagePath = getImagePath(item);
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            imageView.setImage(image);
        } catch (Exception e) {
            // Fallback: tạo placeholder
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

        Label descLabel = new Label(getItemDescription(item));
        descLabel.setTextFill(Color.web("#B0B0B0"));
        descLabel.setFont(Font.font("System", 10));
        descLabel.setWrapText(true);

        infoContainer.getChildren().addAll(nameLabel, categoryLabel, descLabel);

        // Price Label
        Label priceLabel = new Label(item.getFormattedPrice());
        priceLabel.setTextFill(Color.web("#4CAF50"));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Add components to main container
        itemBox.getChildren().addAll(imageContainer, infoContainer, priceLabel);

        // Add drop shadow effect
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#0f3460"));
        dropShadow.setRadius(3);
        itemBox.setEffect(dropShadow);

        return itemBox;
    }

    /**
     * Lấy đường dẫn ảnh cho menu item
     */
    private String getImagePath(MenuItem item) {
        // Sử dụng imagePath từ model hoặc fallback
        return item.getFullImagePath();
    }

    /**
     * Tạo ảnh placeholder khi không tìm thấy ảnh
     */
    private Image createPlaceholderImage() {
        // Trả về ảnh mặc định hoặc tạo ảnh đơn giản
        try {
            return new Image(getClass().getResourceAsStream("/images/menu/default.jpg"));
        } catch (Exception e) {
            // Nếu không có ảnh default, có thể return null và hiển thị text thay thế
            return null;
        }
    }

    /**
     * Lấy mô tả món ăn
     */
    private String getItemDescription(MenuItem item) {
        // Sử dụng description từ model hoặc fallback
        return item.getShortDescription();
    }

    /**
     * Handler khi click chọn menu item
     */
    @FXML
    public void selectMenuItem(MenuItem item) {
        selectedMenuItem = item;

        // Visual feedback - highlight selected item
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

        showInfo("Đã chọn: " + item.getName());
    }

    /**
     * Hiển thị order items với ảnh
     */
    private void displayOrderItems() {
        if (orderContainer == null) return;

        orderContainer.getChildren().clear();

        for (OrderItem item : itemData) {
            HBox orderItemBox = createOrderItemUI(item);
            orderContainer.getChildren().add(orderItemBox);
        }
    }

    /**
     * Tạo UI cho order item
     */
    private HBox createOrderItemUI(OrderItem orderItem) {
        HBox itemBox = new HBox(10);
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 8; -fx-padding: 8;");
        itemBox.setUserData(orderItem);

        // Item Image
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-background-color: #1a1a2e; -fx-background-radius: 5;");
        imageContainer.setPadding(new Insets(3));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(45);
        imageView.setFitWidth(45);
        imageView.setPreserveRatio(true);

        MenuItem menuItem = menuMap.get(orderItem.getMenuItemId());
        if (menuItem != null) {
            try {
                String imagePath = menuItem.getFullImagePath();
                Image image = new Image(getClass().getResourceAsStream(imagePath));
                imageView.setImage(image);
            } catch (Exception e) {
                imageView.setImage(createPlaceholderImage());
            }
        }

        imageContainer.getChildren().add(imageView);

        // Item Info
        VBox infoContainer = new VBox(2);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoContainer, javafx.scene.layout.Priority.ALWAYS);

        Label nameLabel = new Label(orderItem.getDisplayName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

        HBox detailBox = new HBox(10);
        Label qtyLabel = new Label("SL: " + orderItem.getQuantity());
        qtyLabel.setTextFill(Color.web("#B0B0B0"));
        qtyLabel.setFont(Font.font("System", 10));

        Label priceLabel = new Label(orderItem.getFormattedPrice());
        priceLabel.setTextFill(Color.web("#B0B0B0"));
        priceLabel.setFont(Font.font("System", 10));

        detailBox.getChildren().addAll(qtyLabel, priceLabel);
        infoContainer.getChildren().addAll(nameLabel, detailBox);

        // Subtotal and Remove Button
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

    // ===== Event Handlers =====

    @FXML
    public void selectTable(javafx.scene.input.MouseEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        String tableIdStr = (String) source.getUserData();

        if (tableIdStr != null) {
            try {
                int tableId = Integer.parseInt(tableIdStr);
                selectTableById(tableId);
            } catch (NumberFormatException e) {
                showInfo("Lỗi chọn bàn: " + tableIdStr);
            }
        }
    }

    private void selectTableById(int tableId) {
        selectedTableId = tableId;

        // Update label
        if (selectedTableLabel != null) {
            selectedTableLabel.setText("Đã chọn bàn " + tableId);
        }

        // Find and select in ComboBox
        for (Table table : cbTable.getItems()) {
            if (table.getId() == tableId) {
                cbTable.getSelectionModel().select(table);
                break;
            }
        }

        updateTableSelection(tableId);
        showInfo("Đã chọn bàn " + tableId);
    }

    private void updateTableSelection(int tableId) {
        if (tableGrid != null) {
            for (javafx.scene.Node node : tableGrid.getChildren()) {
                if (node.getUserData() != null) {
                    String userData = (String) node.getUserData();
                    if (userData.equals(String.valueOf(tableId))) {
                        // Selected table style
                        updateTableVisualStyle(node, "#e16428");
                    } else {
                        // Reset to default
                        resetTableStyle(node, userData);
                    }
                }
            }
        }
    }

    private void updateTableVisualStyle(javafx.scene.Node node, String color) {
        if (node instanceof javafx.scene.layout.StackPane) {
            javafx.scene.layout.StackPane stackPane = (javafx.scene.layout.StackPane) node;
            // Update all rectangles in the StackPane
            for (javafx.scene.Node child : stackPane.getChildren()) {
                if (child instanceof javafx.scene.shape.Rectangle) {
                    javafx.scene.shape.Rectangle rect = (javafx.scene.shape.Rectangle) child;
                    rect.setFill(Color.web(color));
                }
            }
        }
    }

    private void resetTableStyle(javafx.scene.Node node, String tableIdStr) {
        if (tableIdStr != null) {
            try {
                int tableId = Integer.parseInt(tableIdStr);
                String color;

                // VIP tables (6, 12) - purple
                if (tableId == 6 || tableId == 12) {
                    color = "#9C27B0";
                }
                // Occupied tables (3, 8) - red
                else if (tableId == 3 || tableId == 8) {
                    color = "#f44336";
                }
                // Reserved tables (5, 11) - orange
                else if (tableId == 5 || tableId == 11) {
                    color = "#FF9800";
                }
                // Available tables - green
                else {
                    color = "#4CAF50";
                }

                updateTableVisualStyle(node, color);
            } catch (NumberFormatException e) {
                updateTableVisualStyle(node, "#4CAF50");
            }
        }
    }

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
            showInfo("Đã thêm " + selectedMenuItem.getName() + " x" + spQty.getValue());
        } catch (SQLException e) {
            showError(e);
        }
    }

    @FXML
    public void removeOrderItem(OrderItem orderItem) {
        try {
            orderDAO.removeItem(orderItem.getId());
            reloadItems();
            showInfo("Đã xóa món khỏi order!");
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
                    showInfo("Đã xóa tất cả món!");
                } catch (SQLException e) {
                    showError(e);
                }
            }
        });
    }

    @FXML
    public void newOrder() {
        Table t = cbTable.getSelectionModel().getSelectedItem();
        if (t == null) {
            showInfo("Vui lòng chọn bàn trước!");
            return;
        }
        try {
            Integer id = orderDAO.createEmptyOrder(t.getId());
            if (id != null) {
                new TableDAO().updateStatus(t.getId(), "occupied");
                t.setStatus("occupied");
                current = new Order();
                current.setId(id);
                current.setTableId(t.getId());
                current.setStatus("pending");
                lblOrderId.setText("#" + id);
                reloadItems();
                showInfo("Đã tạo order mới #" + id);
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
        try {
            orderDAO.complete(current.getId());
            current = null;
            afterComplete();
            showInfo("Đã hoàn thành order và giải phóng bàn!");
        } catch (SQLException e) {
            showError(e);
        }
    }

    @FXML
    public void reload() {
        afterComplete();
        loadData();
        showInfo("Đã làm mới dữ liệu!");
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

    private void reloadItems() {
        try {
            itemData.setAll(orderDAO.findItems(current.getId()));
            BigDecimal total = orderDAO.calcTotal(current.getId());
            lblTotal.setText(formatCurrency(total.doubleValue()));
            displayOrderItems();
        } catch (SQLException e) {
            showError(e);
        }
    }

    private double getUnitPrice(OrderItem it) {
        double p = it.getPrice();
        if (p > 0) return p;
        MenuItem m = menuMap.get(it.getMenuItemId());
        return (m != null) ? m.getPrice() : 0.0;
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
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    private void showError(Exception e) {
        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        e.printStackTrace();
    }
}
package com.barmanagement.controller;

import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.model.MenuItem;
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
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Quản lý thực đơn (menu_items) với hiển thị dạng Grid có ảnh
 */
public class MenuManagementController {

    // Form controls
    @FXML private TextField txtName, txtPrice;
    @FXML private ComboBox<String> cbCategory;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnRefresh;

    // Statistics labels
    @FXML private Label lblTotalItems, lblDrinkCount, lblFoodCount, lblAvgPrice;

    // Menu display controls - THAY ĐỔI từ TableView thành FlowPane
    @FXML private FlowPane menuGridContainer;
    @FXML private ComboBox<String> cbCategoryFilter;

    private final MenuItemDAO dao = new MenuItemDAO();
    private final ObservableList<MenuItem> data = FXCollections.observableArrayList();
    private MenuItem selectedMenuItem = null;

    @FXML
    public void initialize() {
        setupComponents();
        setupEventHandlers();
        refresh();
    }

    private void setupComponents() {
        // Setup Category ComboBox cho form
        cbCategory.getItems().clear();
        cbCategory.getItems().addAll("Đồ uống", "Khai vị", "Món chính", "Tráng miệng");

        // Setup Category Filter cho hiển thị
        if (cbCategoryFilter != null) {
            cbCategoryFilter.getItems().clear();
            cbCategoryFilter.getItems().addAll("Tất cả", "Đồ uống", "Khai vị", "Món chính", "Tráng miệng");
            cbCategoryFilter.setValue("Tất cả");
        }

        // Setup FlowPane properties
        if (menuGridContainer != null) {
            menuGridContainer.setHgap(15);
            menuGridContainer.setVgap(15);
            menuGridContainer.setPadding(new Insets(10));
            menuGridContainer.setPrefWrapLength(900);
        }
    }

    private void setupEventHandlers() {
        // Category filter listener
        if (cbCategoryFilter != null) {
            cbCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> displayMenuItems());
        }
    }

    @FXML
    public void add() {
        try {
            // Validate input
            if (txtName.getText().trim().isEmpty()) {
                showInfo("Vui lòng nhập tên món!");
                return;
            }

            if (txtPrice.getText().trim().isEmpty()) {
                showInfo("Vui lòng nhập giá món!");
                return;
            }

            MenuItem m = new MenuItem();
            m.setName(txtName.getText().trim());
            m.setCategory(cbCategory.getValue() == null ? "Đồ uống" : cbCategory.getValue());
            m.setPrice(Double.parseDouble(txtPrice.getText().trim()));

            // Set default values
            m.setImagePath("default.png");
            m.setDescription("Món ngon từ " + m.getCategory());
            m.setAvailable(true);
            m.setPreparationTime(15);

            Integer id = dao.insert(m);
            if (id != null) {
                m.setId(id);
                data.add(m);
                clear();
                displayMenuItems();
                updateStatistics();
                showInfo("✅ Đã thêm món: " + m.getName());
            }
        } catch (NumberFormatException e) {
            showInfo("Giá phải là số hợp lệ!");
        } catch (Exception e) {
            err(e);
        }
    }

    @FXML
    public void update() {
        if (selectedMenuItem == null) {
            showInfo("Vui lòng chọn món cần sửa!");
            return;
        }

        try {
            selectedMenuItem.setName(txtName.getText().trim());
            selectedMenuItem.setCategory(cbCategory.getValue() == null ? "Đồ uống" : cbCategory.getValue());
            selectedMenuItem.setPrice(Double.parseDouble(txtPrice.getText().trim()));

            dao.update(selectedMenuItem);
            displayMenuItems();
            updateStatistics();
            showInfo("✅ Đã cập nhật món: " + selectedMenuItem.getName());
        } catch (NumberFormatException e) {
            showInfo("Giá phải là số hợp lệ!");
        } catch (Exception e) {
            err(e);
        }
    }

    @FXML
    public void delete() {
        if (selectedMenuItem == null) {
            showInfo("Vui lòng chọn món cần xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa món: " + selectedMenuItem.getName());
        confirm.setContentText("Bạn có chắc chắn muốn xóa món này?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    dao.delete(selectedMenuItem.getId());
                    data.remove(selectedMenuItem);
                    selectedMenuItem = null;
                    clear();
                    displayMenuItems();
                    updateStatistics();
                    showInfo("✅ Đã xóa món thành công!");
                } catch (Exception e) {
                    err(e);
                }
            }
        });
    }

    @FXML
    public void refresh() {
        data.clear();
        selectedMenuItem = null;
        try {
            data.addAll(dao.findAll());
            displayMenuItems();
            updateStatistics();
            clear();
            showInfo("🔄 Đã làm mới dữ liệu!");
        } catch (Exception e) {
            err(e);
        }
    }

    // Hiển thị menu items dạng grid với ảnh
    private void displayMenuItems() {
        if (menuGridContainer == null) return;

        menuGridContainer.getChildren().clear();

        String selectedCategory = cbCategoryFilter != null ? cbCategoryFilter.getValue() : "Tất cả";

        List<MenuItem> filteredItems;
        if ("Tất cả".equals(selectedCategory)) {
            filteredItems = data.stream().collect(Collectors.toList());
        } else {
            filteredItems = data.stream()
                    .filter(item -> item.getCategory().equals(selectedCategory))
                    .collect(Collectors.toList());
        }

        for (MenuItem item : filteredItems) {
            VBox menuCard = createMenuCard(item);
            menuGridContainer.getChildren().add(menuCard);
        }

        if (filteredItems.isEmpty()) {
            Label noItemsLabel = new Label("Không có món nào trong danh mục này");
            noItemsLabel.setTextFill(Color.WHITE);
            noItemsLabel.setFont(Font.font("System", 16));
            noItemsLabel.setStyle("-fx-padding: 20; -fx-alignment: center;");
            menuGridContainer.getChildren().add(noItemsLabel);
        }
    }

    // Tạo card cho từng menu item
    private VBox createMenuCard(MenuItem item) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.setPrefHeight(280);
        card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;");
        card.setUserData(item);

        // Hover effects
        card.setOnMouseEntered(e -> {
            if (selectedMenuItem == item) {
                card.setStyle("-fx-background-color: #e16428; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;");
            } else {
                card.setStyle("-fx-background-color: #1a5490; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;");
            }
        });

        card.setOnMouseExited(e -> {
            if (selectedMenuItem == item) {
                card.setStyle("-fx-background-color: #e16428; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;");
            } else {
                card.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;");
            }
        });

        // Click handler
        card.setOnMouseClicked(e -> selectMenuItem(item));

        // Image Container
        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 8;");
        imageContainer.setPadding(new Insets(8));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);

        // Load image
        try {
            String imagePath = "/images/menu/" + (item.getImagePath() != null ? item.getImagePath() : "default.png");
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

        // Item Info
        VBox infoContainer = new VBox(5);
        infoContainer.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(item.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);

        Label categoryLabel = new Label(item.getCategory());
        categoryLabel.setTextFill(Color.web("#B0B0B0"));
        categoryLabel.setFont(Font.font("System", 11));

        Label priceLabel = new Label(item.getFormattedPrice());
        priceLabel.setTextFill(Color.web("#4CAF50"));
        priceLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        // Status indicator
        Label statusLabel = new Label(item.isAvailable() ? "✅ Có sẵn" : "❌ Hết món");
        statusLabel.setTextFill(item.isAvailable() ? Color.web("#4CAF50") : Color.web("#f44336"));
        statusLabel.setFont(Font.font("System", 10));

        infoContainer.getChildren().addAll(nameLabel, categoryLabel, priceLabel, statusLabel);

        card.getChildren().addAll(imageContainer, infoContainer);

        // Add drop shadow
        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#0f3460"));
        dropShadow.setRadius(5);
        card.setEffect(dropShadow);

        // Update style if selected
        if (selectedMenuItem == item) {
            card.setStyle("-fx-background-color: #e16428; -fx-background-radius: 12; -fx-padding: 15; -fx-cursor: hand;");
        }

        return card;
    }

    private Image createPlaceholderImage() {
        try {
            String[] fallbackImages = {
                    "/images/menu/default.png",
                    "/images/menu/Snack.png",
                    "/images/menu/CocaCola.png"
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

    private void selectMenuItem(MenuItem item) {
        selectedMenuItem = item;
        fillForm(item);
        displayMenuItems(); // Refresh to show selection
    }

    private void fillForm(MenuItem m) {
        if (m == null) return;
        txtName.setText(m.getName());
        txtPrice.setText(String.format(java.util.Locale.US, "%.0f", m.getPrice()));
        cbCategory.getSelectionModel().select(m.getCategory());
    }

    private void updateStatistics() {
        if (lblTotalItems != null) {
            int total = data.size();

            // Fix category names để match với database
            int drinks = (int) data.stream().filter(m -> "Đồ uống".equals(m.getCategory())).count();
            int foods = (int) data.stream().filter(m ->
                    "Khai vị".equals(m.getCategory()) ||
                            "Món chính".equals(m.getCategory()) ||
                            "Tráng miệng".equals(m.getCategory())).count();

            double avgPrice = data.stream().mapToDouble(MenuItem::getPrice).average().orElse(0.0);

            lblTotalItems.setText(String.valueOf(total));
            lblDrinkCount.setText(String.valueOf(drinks));
            lblFoodCount.setText(String.valueOf(foods));
            lblAvgPrice.setText(String.format("%.0f", avgPrice / 1000)); // Convert to thousands
        }
    }

    private void clear() {
        txtName.clear();
        txtPrice.clear();
        cbCategory.getSelectionModel().clearSelection();
        selectedMenuItem = null;
    }

    // Navigation methods
    @FXML
    private void goBack() {
        SceneUtil.openScene("/fxml/dashboard.fxml", txtName);
    }

    @FXML
    private void showPayment() {
        SceneUtil.openScene("/fxml/payment.fxml", txtName);
    }

    @FXML
    private void showOrder() {
        SceneUtil.openScene("/fxml/order_management.fxml", txtName);
    }

    @FXML
    private void showSettings() {
        SceneUtil.openScene("/fxml/table_management.fxml", txtName);
    }

    @FXML
    private void handleLogout() {
        LogoutUtil.confirmLogout(txtName);
    }

    @FXML
    private void exportMenu() {
        showInfo("Chức năng xuất menu sẽ được phát triển trong phiên bản tới!");
    }

    @FXML
    private void importMenu() {
        showInfo("Chức năng nhập menu sẽ được phát triển trong phiên bản tới!");
    }

    private void err(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText("Có lỗi xảy ra");
        alert.setContentText(e.getMessage());
        alert.showAndWait();
        e.printStackTrace();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.barmanagement.controller;

import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.model.MenuItem;
import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;
import com.barmanagement.util.ImageStoreUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import com.barmanagement.util.TimeService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
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
    @FXML private Button btnChooseImage;
    @FXML private ImageView imgPreview;

    // Statistics labels
    @FXML private Label lblTotalItems, lblDrinkCount, lblFoodCount, lblAvgPrice;

    @FXML private Label currentTimeLabel;   // label giờ ở header
    @FXML private Label welcomeTimeLabel;   // label ngày (nếu có)

    // Menu display controls
    @FXML private FlowPane menuGridContainer;
    @FXML private ComboBox<String> cbCategoryFilter;

    private final MenuItemDAO dao = new MenuItemDAO();
    private final ObservableList<MenuItem> data = FXCollections.observableArrayList();
    private MenuItem selectedMenuItem = null;

    // Ảnh vừa chọn (absolute path)
    private String pickedImageAbsolutePath = null;

    @FXML
    public void initialize() {

        if (currentTimeLabel != null) {
            currentTimeLabel.textProperty().bind(TimeService.get().timeTextProperty());
        }
        if (welcomeTimeLabel != null) {
            welcomeTimeLabel.textProperty().bind(TimeService.get().dateTextProperty());
        }

        setupComponents();
        setupEventHandlers();
        refresh();
    }

    private void setupComponents() {
        cbCategory.getItems().clear();
        cbCategory.getItems().addAll("Đồ uống", "Khai vị", "Món chính", "Tráng miệng");

        if (cbCategoryFilter != null) {
            cbCategoryFilter.getItems().clear();
            cbCategoryFilter.getItems().addAll("Tất cả", "Đồ uống", "Khai vị", "Món chính", "Tráng miệng");
            cbCategoryFilter.setValue("Tất cả");
        }

        if (menuGridContainer != null) {
            menuGridContainer.setHgap(15);
            menuGridContainer.setVgap(15);
            menuGridContainer.setPadding(new Insets(10));
            menuGridContainer.setPrefWrapLength(900);
        }
    }

    private void setupEventHandlers() {
        if (cbCategoryFilter != null) {
            cbCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> displayMenuItems());
        }
    }

    /** Handler chọn ảnh */
    @FXML
    public void handleChooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh món");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );
        File f = fc.showOpenDialog(btnChooseImage != null ? btnChooseImage.getScene().getWindow() : null);
        if (f == null) return;

        try {
            Path copied = ImageStoreUtil.copyToAppImages(f);
            pickedImageAbsolutePath = copied.toAbsolutePath().toString();

            if (imgPreview != null) {
                imgPreview.setImage(new Image(new File(pickedImageAbsolutePath).toURI().toString(), 80, 80, true, true));
            }

            showInfo("✅ Đã chọn ảnh: " + copied.getFileName());
        } catch (Exception ex) {
            pickedImageAbsolutePath = null;
            if (imgPreview != null) imgPreview.setImage(null);
            err(ex);
        }
    }

    @FXML
    public void add() {
        try {
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

            // set ảnh
            if (pickedImageAbsolutePath != null && !pickedImageAbsolutePath.isEmpty()) {
                m.setImagePath(pickedImageAbsolutePath);
            } else {
                m.setImagePath("default.png");
            }
            m.setDescription("Món ngon từ " + m.getCategory());
            m.setAvailable(true);
            m.setPreparationTime(15);

            Integer id = dao.insert(m);
            if (id != null) {
                m.setId(id);
                data.add(m);
                pickedImageAbsolutePath = null;
                if (imgPreview != null) imgPreview.setImage(null);

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

            if (pickedImageAbsolutePath != null && !pickedImageAbsolutePath.isEmpty()) {
                selectedMenuItem.setImagePath(pickedImageAbsolutePath);
            }

            dao.update(selectedMenuItem);
            pickedImageAbsolutePath = null;
            if (imgPreview != null) imgPreview.setImage(null);

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
        } catch (Exception e) {
            err(e);
        }
    }

    // Hiển thị menu items dạng grid
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
        card.setOnMouseClicked(e -> selectMenuItem(item));

        VBox imageContainer = new VBox();
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 8;");
        imageContainer.setPadding(new Insets(8));

        ImageView imageView = new ImageView();
        imageView.setFitHeight(80);
        imageView.setFitWidth(80);
        imageView.setPreserveRatio(true);

        try {
            String rawPath = item.getImagePath();
            Image image = null;

            if (rawPath != null && !rawPath.isEmpty()) {
                File asFile = new File(rawPath);
                if (asFile.isAbsolute() && asFile.exists()) {
                    image = new Image(asFile.toURI().toString());
                } else {
                    String resourcePath = rawPath.startsWith("/") ? rawPath : "/images/menu/" + rawPath;
                    image = new Image(getClass().getResourceAsStream(resourcePath));
                }
            }

            if (image == null || image.isError()) {
                imageView.setImage(createPlaceholderImage());
            } else {
                imageView.setImage(image);
            }
        } catch (Exception e) {
            imageView.setImage(createPlaceholderImage());
        }

        imageContainer.getChildren().add(imageView);

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

        Label statusLabel = new Label(item.isAvailable() ? "✅ Có sẵn" : "❌ Hết món");
        statusLabel.setTextFill(item.isAvailable() ? Color.web("#4CAF50") : Color.web("#f44336"));
        statusLabel.setFont(Font.font("System", 10));

        infoContainer.getChildren().addAll(nameLabel, categoryLabel, priceLabel, statusLabel);

        card.getChildren().addAll(imageContainer, infoContainer);

        DropShadow dropShadow = new DropShadow();
        dropShadow.setColor(Color.web("#0f3460"));
        dropShadow.setRadius(5);
        card.setEffect(dropShadow);

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
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}
        return null;
    }

    private void selectMenuItem(MenuItem item) {
        selectedMenuItem = item;
        fillForm(item);
        displayMenuItems();
    }

    private void fillForm(MenuItem m) {
        if (m == null) return;
        txtName.setText(m.getName());
        txtPrice.setText(String.format(java.util.Locale.US, "%.0f", m.getPrice()));
        cbCategory.getSelectionModel().select(m.getCategory());

        if (m.getImagePath() != null) {
            File f = new File(m.getImagePath());
            if (f.isAbsolute() && f.exists()) {
                imgPreview.setImage(new Image(f.toURI().toString(), 80, 80, true, true));
            } else {
                imgPreview.setImage(null);
            }
        }
    }

    private void updateStatistics() {
        if (lblTotalItems != null) {
            int total = data.size();
            int drinks = (int) data.stream().filter(m -> "Đồ uống".equals(m.getCategory())).count();
            int foods = (int) data.stream().filter(m ->
                    "Khai vị".equals(m.getCategory()) ||
                            "Món chính".equals(m.getCategory()) ||
                            "Tráng miệng".equals(m.getCategory())).count();
            double avgPrice = data.stream().mapToDouble(MenuItem::getPrice).average().orElse(0.0);

            lblTotalItems.setText(String.valueOf(total));
            lblDrinkCount.setText(String.valueOf(drinks));
            lblFoodCount.setText(String.valueOf(foods));
            lblAvgPrice.setText(String.format("%.0f", avgPrice / 1000));
        }
    }

    private void clear() {
        txtName.clear();
        txtPrice.clear();
        cbCategory.getSelectionModel().clearSelection();
        selectedMenuItem = null;
        pickedImageAbsolutePath = null;
        if (imgPreview != null) imgPreview.setImage(null);
    }

    // Navigation methods
    @FXML private void goBack() { SceneUtil.openScene("/fxml/dashboard.fxml", txtName); }
    @FXML private void showPayment() { SceneUtil.openScene("/fxml/payment.fxml", txtName); }
    @FXML private void showOrder() { SceneUtil.openScene("/fxml/order_management.fxml", txtName); }
    @FXML private void showSettings() { SceneUtil.openScene("/fxml/table_management.fxml", txtName); }
    @FXML private void handleLogout() { LogoutUtil.confirmLogout(txtName); }
    @FXML private void exportMenu() { showInfo("Chức năng xuất menu sẽ được phát triển trong phiên bản tới!"); }
    @FXML private void importMenu() { showInfo("Chức năng nhập menu sẽ được phát triển trong phiên bản tới!"); }

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

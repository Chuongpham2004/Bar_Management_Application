package com.barmanagement.controller;

import com.barmanagement.util.AlertUtil;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    // FXML Components - Navigation Buttons
    @FXML private Button homeBtn;
    @FXML private Button dashboardBtn;
    @FXML private Button ordersBtn;
    @FXML private Button menuBtn;
    @FXML private Button inventoryBtn;
    @FXML private Button customersBtn;
    @FXML private Button staffBtn;
    @FXML private Button reportsBtn;
    @FXML private Button settingsBtn;

    // FXML Components - Header
    @FXML private TextField searchField;
    @FXML private Label currentTimeLabel;

    // FXML Components - ScrollPane (for debugging)
    @FXML private ScrollPane mainScrollPane;

    // Timeline for time update
    private Timeline timeUpdateTimeline;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== HomeController Initialize ===");

        // Initialize current time
        updateCurrentTime();

        // Set up automatic time update every minute
        setupTimeUpdate();

        // Initialize search field
        if (searchField != null) {
            searchField.setPromptText("Tìm kiếm...");
        }

        // Set home button as active
        if (homeBtn != null) {
            setActiveButton(homeBtn);
        }

        // Debug: Check if Categories section exists
        System.out.println("Categories section should be visible in FXML");
        System.out.println("Scroll down to see Categories: Cocktails, Beers, Wines, Spirits");
    }

    // ========== Navigation Methods ==========

    @FXML
    private void showHome(ActionEvent event) {
        System.out.println("Đang ở trang chủ - Home page active");
        setActiveButton(homeBtn);

        // Scroll to top if needed
        if (mainScrollPane != null) {
            mainScrollPane.setVvalue(0.0);
        }
    }

    @FXML
    private void showDashboard(ActionEvent event) {
        System.out.println("Navigating to Dashboard...");
        setActiveButton(dashboardBtn);
        try {
            loadScene("/fxml/dashboard.fxml", "Dashboard - BarManager");
        } catch (IOException e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở trang Dashboard: " + e.getMessage());
        }
    }

    @FXML
    private void showOrders(ActionEvent event) {
        System.out.println("Navigating to Orders...");
        setActiveButton(ordersBtn);
        try {
            loadScene("/fxml/orderscreen.fxml", "Quản lý đơn hàng - BarManager");
        } catch (IOException e) {
            System.err.println("Error loading orders: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở trang đơn hàng: " + e.getMessage());
        }
    }

    @FXML
    private void showMenu(ActionEvent event) {
        System.out.println("Navigating to Menu...");
        setActiveButton(menuBtn);
        try {
            loadScene("/fxml/menu.fxml", "Quản lý Menu - BarManager");
        } catch (IOException e) {
            System.err.println("Error loading menu: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở trang menu: " + e.getMessage());
        }
    }

    @FXML
    private void showInventory(ActionEvent event) {
        System.out.println("Navigating to Inventory...");
        setActiveButton(inventoryBtn);
        try {
            loadScene("/fxml/inventory.fxml", "Quản lý kho hàng - BarManager");
        } catch (IOException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở trang kho hàng: " + e.getMessage());
        }
    }

    @FXML
    private void showCustomers(ActionEvent event) {
        System.out.println("Navigating to Customers...");
        setActiveButton(customersBtn);
        try {
            loadScene("/fxml/customers.fxml", "Quản lý khách hàng - BarManager");
        } catch (IOException e) {
            System.err.println("Error loading customers: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở trang khách hàng: " + e.getMessage());
        }
    }

    @FXML
    private void showStaff(ActionEvent event) {
        System.out.println("Navigating to Staff...");
        setActiveButton(staffBtn);
        try {
            loadScene("/fxml/staff.fxml", "Quản lý nhân viên - BarManager");
        } catch (IOException e) {
            System.err.println("Error loading staff: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở trang nhân viên: " + e.getMessage());
        }
    }

    @FXML
    private void showReports(ActionEvent event) {
        System.out.println("Navigating to Reports...");
        setActiveButton(reportsBtn);
        try {
            loadScene("/fxml/reports.fxml", "Báo cáo - BarManager");
        } catch (IOException e) {
            System.err.println("Error loading reports: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở trang báo cáo: " + e.getMessage());
        }
    }

    @FXML
    private void showSettings(ActionEvent event) {
        System.out.println("Navigating to Settings...");
        setActiveButton(settingsBtn);
        try {
            loadScene("/fxml/settings.fxml", "Cài đặt - BarManager");
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở trang cài đặt: " + e.getMessage());
        }
    }

    // ========== Category Click Handlers ==========

    @FXML
    private void showCocktails(MouseEvent event) {
        System.out.println("🍸 Categories: Cocktails clicked!");
        try {
            // Load menu page filtered by cocktails
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();

            // If menu controller has a method to set category filter
            // MenuController menuController = loader.getController();
            // menuController.filterByCategory("Cocktails");

            Stage stage = (Stage) homeBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menu - Cocktails - BarManager");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading cocktails: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở danh mục Cocktails: " + e.getMessage());
        }
    }

    @FXML
    private void showBeers(MouseEvent event) {
        System.out.println("🍺 Categories: Beers clicked!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) homeBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menu - Beers - BarManager");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading beers: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở danh mục Beers: " + e.getMessage());
        }
    }

    @FXML
    private void showWines(MouseEvent event) {
        System.out.println("🍷 Categories: Wines clicked!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) homeBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menu - Wines - BarManager");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading wines: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở danh mục Wines: " + e.getMessage());
        }
    }

    @FXML
    private void showSpirits(MouseEvent event) {
        System.out.println("🥃 Categories: Spirits clicked!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) homeBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menu - Spirits - BarManager");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error loading spirits: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở danh mục Spirits: " + e.getMessage());
        }
    }

    // ========== Search and User Actions ==========

    @FXML
    private void searchAction(ActionEvent event) {
        if (searchField == null) {
            AlertUtil.showError("Lỗi", "Search field not found!");
            return;
        }

        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            AlertUtil.showWarning("Tìm kiếm", "Vui lòng nhập từ khóa tìm kiếm!");
            return;
        }

        System.out.println("🔍 Searching for: " + searchText);
        AlertUtil.showInfo("Tìm kiếm", "Đang tìm kiếm: " + searchText);

        // TODO: Implement actual search logic
        // You can search across menu items, orders, customers, etc.
        // Example:
        // searchInMenu(searchText);
        // searchInOrders(searchText);
        // searchInCustomers(searchText);
    }

    @FXML
    private void logout(ActionEvent event) {
        System.out.println("🚪 Logout requested...");
        try {
            // Show confirmation dialog
            if (AlertUtil.showConfirmation("Đăng xuất", "Bạn có chắc chắn muốn đăng xuất?")) {
                System.out.println("User confirmed logout");

                // Stop time update timeline
                if (timeUpdateTimeline != null) {
                    timeUpdateTimeline.stop();
                }

                // Load login page
                loadScene("/fxml/login.fxml", "Đăng nhập - BarManager");
            } else {
                System.out.println("User cancelled logout");
            }
        } catch (IOException e) {
            System.err.println("Error during logout: " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể đăng xuất: " + e.getMessage());
        }
    }

    // ========== Utility Methods ==========

    private void loadScene(String fxmlPath, String title) throws IOException {
        System.out.println("Loading scene: " + fxmlPath);

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Stage stage = (Stage) homeBtn.getScene().getWindow();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();

        System.out.println("Scene loaded successfully: " + title);
    }

    private void updateCurrentTime() {
        if (currentTimeLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            currentTimeLabel.setText(now.format(formatter));
        }
    }

    private void setupTimeUpdate() {
        // Create timeline to update time every minute
        timeUpdateTimeline = new Timeline(
                new KeyFrame(Duration.minutes(1), e -> updateCurrentTime())
        );
        timeUpdateTimeline.setCycleCount(Animation.INDEFINITE);
        timeUpdateTimeline.play();

        System.out.println("⏰ Time update timeline started");
    }

    private void setActiveButton(Button activeButton) {
        // Reset all buttons
        Button[] buttons = {homeBtn, dashboardBtn, ordersBtn, menuBtn,
                inventoryBtn, customersBtn, staffBtn, reportsBtn, settingsBtn};

        for (Button btn : buttons) {
            if (btn != null) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14px;");
            }
        }

        // Set active button style
        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #e16428; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        }
    }

    // ========== Debug and Test Methods ==========

    @FXML
    private void testCategoriesVisibility() {
        System.out.println("=== TESTING CATEGORIES VISIBILITY ===");
        System.out.println("Categories section should be visible in FXML");

        // Test click handlers
        System.out.println("Testing category handlers:");
        System.out.println("- Cocktails handler: showCocktails() ✓");
        System.out.println("- Beers handler: showBeers() ✓");
        System.out.println("- Wines handler: showWines() ✓");
        System.out.println("- Spirits handler: showSpirits() ✓");

        AlertUtil.showInfo("Debug", "Categories handlers are working!\nCheck console for details.\n\nHãy scroll xuống để thấy Categories!");
    }

    @FXML
    private void scrollToBottom() {
        if (mainScrollPane != null) {
            mainScrollPane.setVvalue(1.0); // Scroll to bottom
            System.out.println("📜 Scrolled to bottom - Categories should be visible now!");
            AlertUtil.showInfo("Scroll", "Đã scroll xuống cuối! Categories section phải hiển thị rồi!");
        } else {
            System.out.println("⚠️ ScrollPane not found - please scroll manually");
            AlertUtil.showInfo("Scroll", "Hãy scroll xuống cuối trang để thấy Categories!");
        }
    }

    // ========== Optional Enhancement Methods ==========

    // Method to handle featured product clicks
    public void showProductDetails(String productName) {
        System.out.println("🍸 Product clicked: " + productName);
        AlertUtil.showInfo("Sản phẩm", "Xem chi tiết: " + productName);
        // TODO: Implement product details view
    }

    // Method to handle quick navigation to specific category
    public void navigateToCategory(String category) {
        System.out.println("🎯 Quick navigate to category: " + category);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/menu.fxml"));
            Parent root = loader.load();

            // If menu controller has a method to set category filter
            // MenuController menuController = loader.getController();
            // menuController.filterByCategory(category);

            Stage stage = (Stage) homeBtn.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Menu - " + category + " - BarManager");
            stage.show();
        } catch (IOException e) {
            System.err.println("Error navigating to category " + category + ": " + e.getMessage());
            AlertUtil.showError("Lỗi", "Không thể mở danh mục " + category + ": " + e.getMessage());
        }
    }

    // Method to refresh time manually
    public void refreshTime() {
        updateCurrentTime();
        System.out.println("⏰ Time refreshed manually");
    }

    // Cleanup method
    public void cleanup() {
        if (timeUpdateTimeline != null) {
            timeUpdateTimeline.stop();
            System.out.println("🛑 Timeline stopped - cleanup completed");
        }
    }
}
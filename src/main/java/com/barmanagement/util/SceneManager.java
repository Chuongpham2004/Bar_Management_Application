package com.barmanagement.util;

import com.barmanagement.model.Staff;
import com.barmanagement.model.Table;
import com.barmanagement.model.Order;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Scene Manager - Handles navigation between screens
 * Manages the flow: Login → Dashboard → Table → Order → Payment
 */
public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;
    private Map<String, Scene> sceneCache;
    private Map<String, Object> controllerCache;

    // Screen names
    public static final String LOGIN_SCREEN = "Login";
    public static final String DASHBOARD_SCREEN = "Dashboard";
    public static final String TABLE_SCREEN = "TableScreen";
    public static final String ORDER_SCREEN = "OrderScreen";
    public static final String PAYMENT_SCREEN = "PaymentScreen";

    // Screen dimensions
    private static final double LOGIN_WIDTH = 900;
    private static final double LOGIN_HEIGHT = 600;
    private static final double MAIN_WIDTH = 1200;
    private static final double MAIN_HEIGHT = 800;

    private SceneManager() {
        this.sceneCache = new HashMap<>();
        this.controllerCache = new HashMap<>();
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Initialize with primary stage
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
        setupStage();
    }

    /**
     * Setup primary stage properties
     */
    private void setupStage() {
        primaryStage.setTitle("Bar Management System");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.centerOnScreen();
    }

    /**
     * Navigate to specific screen
     */
    public void switchTo(String screenName) {
        switchTo(screenName, null);
    }

    /**
     * Navigate to screen with data
     */
    public void switchTo(String screenName, Object data) {
        try {
            Scene scene = getScene(screenName);

            if (scene != null) {
                // Pass data to controller if needed
                if (data != null) {
                    passDataToController(screenName, data);
                }

                // Set window size based on screen
                setWindowSize(screenName);

                // Switch scene with fade effect
                switchSceneWithTransition(scene);

                // Update title
                updateTitle(screenName);

                System.out.println("✅ Navigated to: " + screenName);
            } else {
                showError("Không thể tải màn hình: " + screenName);
            }

        } catch (Exception e) {
            System.err.println("❌ Navigation error: " + e.getMessage());
            e.printStackTrace();
            showError("Lỗi chuyển màn hình: " + e.getMessage());
        }
    }

    /**
     * Get scene (load if not cached)
     */
    private Scene getScene(String screenName) {
        // Check cache first
        if (sceneCache.containsKey(screenName)) {
            return sceneCache.get(screenName);
        }

        // Load new scene
        try {
            String fxmlPath = "/fxml/" + screenName + ".fxml";
            System.out.println("🔄 Loading FXML: " + fxmlPath);

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Cache controller
            Object controller = loader.getController();
            if (controller != null) {
                controllerCache.put(screenName, controller);
                System.out.println("📝 Controller cached: " + controller.getClass().getSimpleName());
            }

            // Create scene
            Scene scene = new Scene(root);

            // Load CSS if exists
            loadCSS(scene, screenName);

            // Cache scene
            sceneCache.put(screenName, scene);

            return scene;

        } catch (IOException e) {
            System.err.println("❌ Failed to load FXML: " + screenName);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Load CSS for scene
     */
    private void loadCSS(Scene scene, String screenName) {
        try {
            String cssPath = "/css/" + screenName.toLowerCase() + ".css";
            String cssUrl = getClass().getResource(cssPath);

            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
                System.out.println("🎨 CSS loaded: " + cssPath);
            }

            // Load common CSS
            String commonCssPath = "/css/common.css";
            String commonCssUrl = getClass().getResource(commonCssPath);
            if (commonCssUrl != null) {
                scene.getStylesheets().add(commonCssUrl.toExternalForm());
            }

        } catch (Exception e) {
            System.out.println("⚠️ CSS not found for: " + screenName);
        }
    }

    /**
     * Set window size based on screen
     */
    private void setWindowSize(String screenName) {
        switch (screenName) {
            case LOGIN_SCREEN:
                primaryStage.setWidth(LOGIN_WIDTH);
                primaryStage.setHeight(LOGIN_HEIGHT);
                primaryStage.setResizable(false);
                break;
            default:
                primaryStage.setWidth(MAIN_WIDTH);
                primaryStage.setHeight(MAIN_HEIGHT);
                primaryStage.setResizable(true);
                break;
        }
        primaryStage.centerOnScreen();
    }

    /**
     * Switch scene with fade transition
     */
    private void switchSceneWithTransition(Scene newScene) {
        if (primaryStage.getScene() == null) {
            // First time - no transition
            primaryStage.setScene(newScene);
        } else {
            // Fade transition
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), primaryStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                primaryStage.setScene(newScene);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), newScene.getRoot());
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        }
    }

    /**
     * Update window title
     */
    private void updateTitle(String screenName) {
        String title = "Bar Management System";

        switch (screenName) {
            case LOGIN_SCREEN:
                title += " - Đăng nhập";
                break;
            case DASHBOARD_SCREEN:
                title += " - Tổng quan";
                break;
            case TABLE_SCREEN:
                title += " - Chọn bàn";
                break;
            case ORDER_SCREEN:
                title += " - Gọi món";
                break;
            case PAYMENT_SCREEN:
                title += " - Thanh toán";
                break;
        }

        // Add current user info
        Staff currentStaff = SessionManager.getInstance().getCurrentStaff();
        if (currentStaff != null) {
            title += " - " + currentStaff.getFullName();
        }

        primaryStage.setTitle(title);
    }

    /**
     * Pass data to controller
     */
    private void passDataToController(String screenName, Object data) {
        Object controller = controllerCache.get(screenName);

        if (controller == null) {
            return;
        }

        try {
            switch (screenName) {
                case DASHBOARD_SCREEN:
                    if (data instanceof Staff && controller instanceof com.barmanagement.controller.DashboardController) {
                        // Pass staff data to dashboard
                        ((com.barmanagement.controller.DashboardController) controller).setCurrentStaff((Staff) data);
                    }
                    break;

                case TABLE_SCREEN:
                    if (controller instanceof com.barmanagement.controller.TableScreenController) {
                        // Initialize table screen
                        ((com.barmanagement.controller.TableScreenController) controller).initializeData();
                    }
                    break;

                case ORDER_SCREEN:
                    if (data instanceof Table && controller instanceof com.barmanagement.controller.OrderScreenController) {
                        // Pass selected table to order screen
                        ((com.barmanagement.controller.OrderScreenController) controller).setSelectedTable((Table) data);
                    }
                    break;

                case PAYMENT_SCREEN:
                    if (data instanceof Order && controller instanceof com.barmanagement.controller.PaymentScreenController) {
                        // Pass order to payment screen
                        ((com.barmanagement.controller.PaymentScreenController) controller).setOrder((Order) data);
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error passing data to controller: " + e.getMessage());
        }
    }

    /**
     * Navigation methods for specific flows
     */

    // Login successful → Dashboard
    public void loginSuccess(Staff staff) {
        SessionManager.getInstance().login(staff);
        switchTo(DASHBOARD_SCREEN, staff);
    }

    // Dashboard → Table Selection
    public void showTableSelection() {
        switchTo(TABLE_SCREEN);
    }

    // Table selected → Order Screen
    public void showOrderScreen(Table selectedTable) {
        switchTo(ORDER_SCREEN, selectedTable);
    }

    // Order complete → Payment Screen
    public void showPaymentScreen(Order order) {
        switchTo(PAYMENT_SCREEN, order);
    }

    // Payment complete → Dashboard
    public void paymentComplete() {
        switchTo(DASHBOARD_SCREEN);
    }

    // Logout → Login Screen
    public void logout() {
        SessionManager.getInstance().logout();
        clearCache(); // Clear sensitive data
        switchTo(LOGIN_SCREEN);
    }

    // Back to previous screen
    public void goBack() {
        // Simple back navigation - can be enhanced with history stack
        Staff currentStaff = SessionManager.getInstance().getCurrentStaff();
        if (currentStaff != null) {
            switchTo(DASHBOARD_SCREEN);
        } else {
            switchTo(LOGIN_SCREEN);
        }
    }

    /**
     * Get controller instance
     */
    @SuppressWarnings("unchecked")
    public <T> T getController(String screenName, Class<T> controllerClass) {
        Object controller = controllerCache.get(screenName);
        if (controller != null && controllerClass.isInstance(controller)) {
            return (T) controller;
        }
        return null;
    }

    /**
     * Clear cache (for logout)
     */
    public void clearCache() {
        sceneCache.clear();
        controllerCache.clear();
        System.out.println("🧹 Scene cache cleared");
    }

    /**
     * Show error dialog
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show confirmation dialog
     */
    public boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        return alert.showAndWait()
                .filter(response -> response == javafx.scene.control.ButtonType.OK)
                .isPresent();
    }

    /**
     * Get primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
}

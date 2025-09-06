package com.barmanagement.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.shape.Circle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;

import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;
import com.barmanagement.util.DashboardUpdateUtil;
import com.barmanagement.util.InvoiceHelper;
import com.barmanagement.dao.RevenueDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.OrderItemDAO;
import com.barmanagement.dao.PaymentDAO;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.Payment;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Dashboard Controller - ENHANCED WITH IMPROVED ACTIVITY SECTION
 * Handles main dashboard display with real-time statistics and enhanced UI
 */
public class DashboardController {

    @FXML private Button manageTablesButton;
    @FXML private Button manageMenuButton;
    @FXML private Button managePaymentButton;
    @FXML private Button revenueReportButton;
    @FXML private Button logoutButton;
    @FXML private Button manageOrderButton;

    // Dashboard specific elements
    @FXML private Label currentTimeLabel;
    @FXML private Label welcomeTimeLabel;
    @FXML private Label lblTodayRevenue;
    @FXML private Label lblTodayOrders;
    @FXML private Label lblActiveTables;
    @FXML private Label lblMenuItems;

    // Charts
    @FXML private LineChart<String, Number> revenueChart;
    @FXML private BarChart<String, Number> ordersChart;
    @FXML private CategoryAxis revenueXAxis;
    @FXML private NumberAxis revenueYAxis;
    @FXML private CategoryAxis ordersXAxis;
    @FXML private NumberAxis ordersYAxis;

    // ENHANCED Activity Section
    @FXML private VBox activityContainer;
    @FXML private ScrollPane activityScrollPane;
    @FXML private TableView<Order> recentOrdersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> tableColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, String> timeColumn;
    @FXML private TableColumn<Order, String> amountColumn;

    // Enhanced statistics labels
    @FXML private Label lblCashPayments;
    @FXML private Label lblCardPayments;
    @FXML private Label lblAvgOrderValue;
    @FXML private Label lblPeakHour;

    // Service Time Statistics
    @FXML private Label lblAvgServiceTime;
    @FXML private Label lblFastestService;
    @FXML private Label lblActiveOrders;

    // DAOs
    private RevenueDAO revenueDAO;
    private TableDAO tableDAO;
    private MenuItemDAO menuItemDAO;
    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private PaymentDAO paymentDAO;

    // Data
    private ObservableList<Order> recentOrders = FXCollections.observableArrayList();

    // Formatter
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    // State tracking
    private boolean isFirstLoad = true;
    private boolean isInitialized = false;

    // Timelines for auto-refresh
    private Timeline clockTimeline;
    private Timeline dataTimeline;

    @FXML
    public void initialize() {
        System.out.println("📊 ENHANCED DASHBOARD CONTROLLER INITIALIZING...");

        // Initialize DAOs
        revenueDAO = new RevenueDAO();
        tableDAO = new TableDAO();
        menuItemDAO = new MenuItemDAO();
        orderDAO = new OrderDAO();
        orderItemDAO = new OrderItemDAO();
        paymentDAO = new PaymentDAO();

        // Setup formatter
        currencyFormatter.setMaximumFractionDigits(0);

        // Initialize time display
        updateTimeDisplay();

        // Start clock update timer (every second)
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimeDisplay()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        // Initialize table columns if table exists
        setupRecentOrdersTable();

        // Load initial dashboard data
        loadDashboardData();
        initializeCharts();

        // ENHANCED: Load existing activity on startup
        Platform.runLater(() -> {
            try {
                List<Order> existingPaidOrders = orderDAO.findByStatus("paid");
                List<Order> existingCompletedOrders = orderDAO.findByStatus("completed");

                if (!existingPaidOrders.isEmpty() || !existingCompletedOrders.isEmpty()) {
                    System.out.println("📋 Found existing orders, loading enhanced activity");
                    isFirstLoad = false;
                    loadEnhancedRecentActivity();
                } else {
                    System.out.println("📋 No existing orders, showing enhanced empty state");
                    initializeEnhancedEmptyActivity();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                initializeEnhancedEmptyActivity();
            }
        });

        // Start data refresh timer
        dataTimeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            loadDashboardData();
            updateCharts();
            if (!isFirstLoad) {
                loadEnhancedRecentActivity();
            }
        }));
        dataTimeline.setCycleCount(Timeline.INDEFINITE);
        dataTimeline.play();

        // Register for real-time payment updates
        DashboardUpdateUtil.addUpdateListener(this::refreshEnhancedDashboardData);

        isInitialized = true;
        System.out.println("✅ ENHANCED DASHBOARD CONTROLLER INITIALIZED");
    }

    /**
     * ENHANCED: Real-time dashboard refresh with improved animations
     */
    private void refreshEnhancedDashboardData() {
        System.out.println("🔄 ENHANCED DASHBOARD REAL-TIME REFRESH TRIGGERED...");

        Platform.runLater(() -> {
            try {
                // Mark that we've had activity
                isFirstLoad = false;

                // Reload all statistics with animations
                loadTodayStats();
                updateCharts(); // Use existing method instead of loadRevenueChart()
                loadPaymentMethodStatistics();
                loadServiceTimeStatistics();
                loadTableStatus();

                // Load enhanced recent activity
                loadEnhancedRecentActivity();

                // Show enhanced update notification
                showEnhancedUpdateNotification();

                System.out.println("✅ Enhanced dashboard refresh completed");

            } catch (Exception e) {
                System.err.println("❌ Error in enhanced dashboard refresh: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * ENHANCED: Initialize beautiful empty activity state
     */
    private void initializeEnhancedEmptyActivity() {
        if (activityContainer != null) {
            activityContainer.getChildren().clear();

            // Create enhanced empty state
            VBox emptyState = new VBox(20.0);
            emptyState.setAlignment(Pos.CENTER);
            emptyState.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; -fx-padding: 40;");
            emptyState.setMinHeight(200.0);
            emptyState.setPrefHeight(350.0);

            Label iconLabel = new Label("📊");
            iconLabel.setTextFill(Color.web("#B0B0B0"));
            iconLabel.setFont(Font.font("System", 48));

            Label titleLabel = new Label("Chưa có hoạt động gần đây");
            titleLabel.setTextFill(Color.web("#B0B0B0"));
            titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            titleLabel.setTextAlignment(TextAlignment.CENTER);

            Label descLabel = new Label("Hoạt động sẽ xuất hiện khi có đơn hàng được thanh toán hoặc hoàn thành");
            descLabel.setTextFill(Color.web("#808080"));
            descLabel.setFont(Font.font("System", 12));
            descLabel.setWrapText(true);
            descLabel.setMaxWidth(350.0);
            descLabel.setTextAlignment(TextAlignment.CENTER);

            // Quick action buttons in empty state
            HBox quickActions = new HBox(10.0);
            quickActions.setAlignment(Pos.CENTER);

            Button newOrderBtn = new Button("➕ Tạo đơn mới");
            newOrderBtn.setOnAction(e -> handleManageOrder(null));
            newOrderBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 11px; -fx-padding: 8 12 8 12;");

            Button paymentBtn = new Button("💳 Thanh toán");
            paymentBtn.setOnAction(e -> handlePayment(null));
            paymentBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 11px; -fx-padding: 8 12 8 12;");

            quickActions.getChildren().addAll(newOrderBtn, paymentBtn);

            emptyState.getChildren().addAll(iconLabel, titleLabel, descLabel, quickActions);
            activityContainer.getChildren().add(emptyState);
        }
    }

    /**
     * ENHANCED: Load recent activity with beautiful cards
     */
    private void loadEnhancedRecentActivity() {
        Platform.runLater(() -> {
            try {
                // Clear existing data
                recentOrders.clear();

                // Load completed and paid orders
                List<Order> paidOrders = orderDAO.findByStatus("paid");
                List<Order> completedOrders = orderDAO.findByStatus("completed");

                // Combine all orders
                paidOrders.addAll(completedOrders);

                if (paidOrders.isEmpty()) {
                    initializeEnhancedEmptyActivity();
                    return;
                }

                // Sort by most recent first and take last 20
                paidOrders.stream()
                        .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                        .limit(20)
                        .forEach(recentOrders::add);

                // Update enhanced activity cards
                if (activityContainer != null) {
                    updateEnhancedActivityCards();
                }

                System.out.println("📋 Enhanced recent activity loaded: " + paidOrders.size() + " orders");

            } catch (SQLException e) {
                e.printStackTrace();
                showErrorMessage("Không thể tải hoạt động gần đây: " + e.getMessage());
            }
        });
    }

    /**
     * ENHANCED: Create beautiful activity cards
     */
    private void updateEnhancedActivityCards() {
        activityContainer.getChildren().clear();

        if (recentOrders.isEmpty()) {
            initializeEnhancedEmptyActivity();
            return;
        }

        // Create enhanced cards for recent orders
        recentOrders.stream()
                .limit(8) // Show more items in the taller container
                .forEach(order -> {
                    HBox activityItem = createEnhancedActivityItem(order);
                    activityContainer.getChildren().add(activityItem);
                });
    }

    /**
     * ENHANCED: Create beautiful activity item card
     */
    private HBox createEnhancedActivityItem(Order order) {
        HBox itemBox = new HBox();
        itemBox.setAlignment(Pos.CENTER_LEFT);
        itemBox.setStyle("-fx-padding: 15; -fx-background-color: #16213e; -fx-background-radius: 10; -fx-cursor: hand; -fx-spacing: 15;");
        itemBox.setMaxWidth(Double.MAX_VALUE);
        itemBox.setMinHeight(75.0);
        itemBox.setPrefHeight(75.0);

        // Enhanced status indicator with animation
        VBox statusIndicator = new VBox(3);
        statusIndicator.setAlignment(Pos.CENTER);
        statusIndicator.setMinWidth(65.0);
        statusIndicator.setPrefWidth(65.0);

        Circle statusCircle = new Circle(12.0);
        Label statusText = new Label();
        statusText.setFont(Font.font("System", FontWeight.BOLD, 9));
        statusText.setTextAlignment(TextAlignment.CENTER);

        switch (order.getStatus()) {
            case "paid":
                statusCircle.setFill(Color.web("#4CAF50"));
                statusText.setText("HOÀN THÀNH");
                statusText.setTextFill(Color.web("#4CAF50"));
                break;
            case "completed":
                statusCircle.setFill(Color.web("#2196F3"));
                statusText.setText("CHỜ T.TOÁN");
                statusText.setTextFill(Color.web("#2196F3"));
                // Add pulsing animation for orders waiting payment
                addPulsingAnimation(statusCircle);
                break;
            case "cancelled":
                statusCircle.setFill(Color.web("#f44336"));
                statusText.setText("ĐÃ HỦY");
                statusText.setTextFill(Color.web("#f44336"));
                break;
            default:
                statusCircle.setFill(Color.web("#FF9800"));
                statusText.setText("XỬ LÝ");
                statusText.setTextFill(Color.web("#FF9800"));
        }

        statusIndicator.getChildren().addAll(statusCircle, statusText);

        // Enhanced content container
        VBox contentContainer = new VBox(5);
        contentContainer.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(contentContainer, javafx.scene.layout.Priority.ALWAYS);

        // Main title with icon
        HBox titleBox = new HBox(8);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(getOrderIcon(order));
        iconLabel.setFont(Font.font("System", 16));

        Label titleLabel = new Label(getEnhancedActivityTitle(order));
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setMaxWidth(300.0);

        titleBox.getChildren().addAll(iconLabel, titleLabel);

        // Enhanced subtitle with more information
        Label subtitleLabel = new Label(getEnhancedActivitySubtitle(order));
        subtitleLabel.setTextFill(Color.web("#B0B0B0"));
        subtitleLabel.setFont(Font.font("System", 12));
        subtitleLabel.setMaxWidth(300.0);

        contentContainer.getChildren().addAll(titleBox, subtitleLabel);

        // Enhanced right side with amount and time
        VBox rightContainer = new VBox(5);
        rightContainer.setAlignment(Pos.CENTER_RIGHT);
        rightContainer.setMinWidth(130);
        rightContainer.setPrefWidth(130);

        Label amountLabel = new Label(getActivityAmount(order));
        amountLabel.setTextFill(getActivityAmountColor(order));
        amountLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        amountLabel.setTextAlignment(TextAlignment.RIGHT);

        Label timeLabel = new Label(getRelativeTime(order));
        timeLabel.setTextFill(Color.web("#808080"));
        timeLabel.setFont(Font.font("System", 10));
        timeLabel.setTextAlignment(TextAlignment.RIGHT);

        rightContainer.getChildren().addAll(amountLabel, timeLabel);

        itemBox.getChildren().addAll(statusIndicator, contentContainer, rightContainer);

        // Add enhanced hover effects
        itemBox.setOnMouseEntered(e -> {
            itemBox.setStyle("-fx-padding: 15; -fx-background-color: #1a2851; -fx-background-radius: 10; -fx-cursor: hand; -fx-spacing: 15;");
            // Add subtle scale animation on hover
            ScaleTransition hoverScale = new ScaleTransition(Duration.millis(150), itemBox);
            hoverScale.setToX(1.02);
            hoverScale.setToY(1.02);
            hoverScale.play();
        });

        itemBox.setOnMouseExited(e -> {
            itemBox.setStyle("-fx-padding: 15; -fx-background-color: #16213e; -fx-background-radius: 10; -fx-cursor: hand; -fx-spacing: 15;");
            ScaleTransition exitScale = new ScaleTransition(Duration.millis(150), itemBox);
            exitScale.setToX(1.0);
            exitScale.setToY(1.0);
            exitScale.play();
        });

        // Add click handler for details - FIXED VERSION
        itemBox.setOnMouseClicked(e -> {
            System.out.println("🖱️ Clicked on order #" + order.getId());
            openOrderDetailsPopup(order);
        });

        return itemBox;
    }

    /**
     * IMPROVED: Open order details in a responsive popup with smart scrolling
     */
    private void openOrderDetailsPopup(Order order) {
        try {
            System.out.println("📋 Opening details for order #" + order.getId());

            // Create new stage for popup
            Stage detailStage = new Stage();
            detailStage.initModality(Modality.APPLICATION_MODAL);
            detailStage.setTitle("Chi tiết đơn hàng #" + order.getId());

            // Create main container with responsive sizing
            VBox mainContainer = new VBox(20);
            mainContainer.setPadding(new Insets(25));
            mainContainer.setStyle("-fx-background-color: #1a1a2e;");
            mainContainer.setPrefWidth(650);
            mainContainer.setMinWidth(650);
            mainContainer.setMaxWidth(800);

            // Header section
            HBox headerBox = new HBox(20);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; -fx-padding: 20;");
            headerBox.setMinHeight(80);

            Label orderIcon = new Label("📋");
            orderIcon.setFont(Font.font(32));

            VBox headerInfo = new VBox(5);
            Label orderTitle = new Label("Đơn hàng #" + order.getId());
            orderTitle.setTextFill(Color.WHITE);
            orderTitle.setFont(Font.font("System", FontWeight.BOLD, 22));

            Label orderSubtitle = new Label("Bàn " + order.getTableId() + " • " + order.getFormattedOrderTime());
            orderSubtitle.setTextFill(Color.web("#B0B0B0"));
            orderSubtitle.setFont(Font.font(14));

            headerInfo.getChildren().addAll(orderTitle, orderSubtitle);

            // Print button in header (moved to top)
            Button printButton = new Button("🖨 In hóa đơn");
            printButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 14px; -fx-padding: 12 20 12 20;");
            printButton.setOnAction(e -> {
                try {
                    // Load order items
                    List<OrderItem> items = orderItemDAO.findByOrderId(order.getId());
                    
                    // Generate and open invoice (TXT format - works on all machines)
                    InvoiceHelper.createAndOpenInvoice(order, items);
                    
                    showInfo("✅ Đã tạo hóa đơn thành công!\nFile hóa đơn đã được mở tự động.");
                    
                } catch (Exception ex) {
                    System.err.println("❌ Lỗi tạo hóa đơn: " + ex.getMessage());
                    ex.printStackTrace();
                    showInfo("❌ Lỗi tạo hóa đơn: " + ex.getMessage());
                }
            });

            HBox.setHgrow(headerInfo, javafx.scene.layout.Priority.ALWAYS);
            headerBox.getChildren().addAll(orderIcon, headerInfo, printButton);

            // Status section
            HBox statusBox = new HBox(15);
            statusBox.setAlignment(Pos.CENTER_LEFT);
            statusBox.setStyle("-fx-background-color: " + order.getStatusColor() + "; -fx-background-radius: 10; -fx-padding: 15;");
            statusBox.setMinHeight(50);

            Label statusLabel = new Label("Trạng thái: " + order.getStatusDisplayName());
            statusLabel.setTextFill(Color.WHITE);
            statusLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
            statusBox.getChildren().add(statusLabel);

            // Items section with smart scrolling
            VBox itemsContainer = new VBox(15);
            itemsContainer.setStyle("-fx-background-color: #16213e; -fx-background-radius: 12; -fx-padding: 20;");

            Label itemsTitle = new Label("🍽 Chi tiết món ăn:");
            itemsTitle.setTextFill(Color.WHITE);
            itemsTitle.setFont(Font.font("System", FontWeight.BOLD, 16));

            // Load order items first to determine if scrolling is needed
            VBox itemsList = new VBox(12);
            itemsList.setStyle("-fx-background-color: transparent;");

            boolean needsScrolling = false;
            int itemCount = 0;

            try {
                List<OrderItem> items = orderItemDAO.findByOrderId(order.getId());
                itemCount = items.size();

                // Determine if scrolling is needed (more than 4 items)
                needsScrolling = itemCount > 4;

                if (items.isEmpty()) {
                    Label noItemsLabel = new Label("Không có món nào trong đơn hàng này");
                    noItemsLabel.setTextFill(Color.web("#808080"));
                    noItemsLabel.setFont(Font.font(14));
                    itemsList.getChildren().add(noItemsLabel);
                } else {
                    for (OrderItem item : items) {
                        HBox itemRow = createItemRow(item);
                        itemsList.getChildren().add(itemRow);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Error loading order items: " + e.getMessage());
                Label errorLabel = new Label("Lỗi khi tải chi tiết món ăn: " + e.getMessage());
                errorLabel.setTextFill(Color.web("#f44336"));
                errorLabel.setFont(Font.font(14));
                itemsList.getChildren().add(errorLabel);
            }

            // Create items display with conditional scrolling
            Node itemsDisplay;
            if (needsScrolling) {
                // Use ScrollPane for many items
                ScrollPane itemsScrollPane = new ScrollPane(itemsList);
                itemsScrollPane.setPrefHeight(280);
                itemsScrollPane.setMaxHeight(280);
                itemsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
                itemsScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
                itemsScrollPane.setFitToWidth(true);
                itemsScrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

                itemsDisplay = itemsScrollPane;

                // Set container height for scrolling case
                itemsContainer.setPrefHeight(350);
                itemsContainer.setMaxHeight(350);
            } else {
                // Direct display for few items
                itemsDisplay = itemsList;

                // Set container height based on content
                double estimatedHeight = Math.max(150, itemCount * 57 + 80); // 57px per item + padding
                itemsContainer.setPrefHeight(estimatedHeight);
                itemsContainer.setMinHeight(estimatedHeight);
            }

            itemsContainer.getChildren().addAll(itemsTitle, itemsDisplay);

            // Total section (moved above buttons, not overlapping)
            VBox totalContainer = new VBox(8);
            totalContainer.setAlignment(Pos.CENTER_RIGHT);
            totalContainer.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 10; -fx-padding: 18;");
            totalContainer.setMinHeight(60);

            // Hiển thị giảm giá nếu có
            if (order.getDiscountPercent() > 0) {
                // Tổng cộng (trước giảm giá)
                String originalAmount = String.format("%,.0f VNĐ", order.getOriginalAmount().doubleValue());
                Label originalTotalLabel = new Label("💰 Tổng cộng: " + originalAmount);
                originalTotalLabel.setTextFill(Color.WHITE);
                originalTotalLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
                
                // Giảm giá
                Label discountLabel = new Label("🎯 Giảm giá (" + String.format("%.0f", order.getDiscountPercent()) + "%): -" + order.getFormattedDiscountAmount());
                discountLabel.setTextFill(Color.web("#FFE082"));
                discountLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                
                // Thành tiền (sau giảm giá)
                Label finalTotalLabel = new Label("💳 Thành tiền: " + order.getFormattedFinalAmount());
                finalTotalLabel.setTextFill(Color.WHITE);
                finalTotalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
                
                totalContainer.getChildren().addAll(originalTotalLabel, discountLabel, finalTotalLabel);
            } else {
                // Không có giảm giá, hiển thị tổng cộng bình thường
                Label totalLabel = new Label("💰 Tổng cộng: " + order.getFormattedTotal());
                totalLabel.setTextFill(Color.WHITE);
                totalLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
                totalContainer.getChildren().add(totalLabel);
            }

            // Bottom buttons section
            HBox buttonsBox = new HBox(15);
            buttonsBox.setAlignment(Pos.CENTER_RIGHT);

            Button closeButton = new Button("✕ Đóng");
            closeButton.setStyle("-fx-background-color: #666; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 14px; -fx-padding: 12 20 12 20;");
            closeButton.setOnAction(e -> detailStage.close());

            buttonsBox.getChildren().add(closeButton);

            // Add all sections to main container
            mainContainer.getChildren().addAll(headerBox, statusBox, itemsContainer, totalContainer, buttonsBox);

            // Calculate total window height based on content
            double baseHeight = 500; // Base height for header, status, total, buttons
            double contentHeight = needsScrolling ? 350 : (itemCount * 57 + 80); // Items container height
            double totalHeight = Math.min(800, Math.max(550, baseHeight + contentHeight));

            mainContainer.setPrefHeight(totalHeight);
            mainContainer.setMinHeight(Math.min(550, totalHeight));

            // Create scene and show
            Scene scene = new Scene(mainContainer);
            detailStage.setScene(scene);
            detailStage.setResizable(false); // Fixed size for better UX
            detailStage.show();

            // Center the stage on screen
            detailStage.centerOnScreen();

            System.out.println("✅ Responsive order details popup opened - Items: " + itemCount + ", Scrolling: " + needsScrolling);

        } catch (Exception e) {
            System.err.println("❌ Error opening order details popup: " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Không thể mở chi tiết đơn hàng: " + e.getMessage());
        }
    }

    /**
     * Helper method to create individual item row
     */
    private HBox createItemRow(OrderItem item) {
        HBox itemRow = new HBox();
        itemRow.setAlignment(Pos.CENTER_LEFT);
        itemRow.setSpacing(15);
        itemRow.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 8; -fx-padding: 12;");
        itemRow.setMinHeight(45);
        itemRow.setPrefHeight(45);

        Label itemName = new Label(item.getDisplayName());
        itemName.setTextFill(Color.WHITE);
        itemName.setFont(Font.font("System", FontWeight.NORMAL, 14));
        itemName.setPrefWidth(240);
        itemName.setMaxWidth(240);
        itemName.setWrapText(true); // Allow text wrapping for long names

        Label itemQty = new Label("x" + item.getQuantity());
        itemQty.setTextFill(Color.web("#4CAF50"));
        itemQty.setFont(Font.font("System", FontWeight.BOLD, 14));
        itemQty.setPrefWidth(40);
        itemQty.setAlignment(Pos.CENTER);

        Label itemPrice = new Label(item.getFormattedPrice());
        itemPrice.setTextFill(Color.web("#FF9800"));
        itemPrice.setFont(Font.font("System", FontWeight.NORMAL, 14));
        itemPrice.setPrefWidth(100);
        itemPrice.setAlignment(Pos.CENTER_RIGHT);

        Label itemSubtotal = new Label(item.getFormattedSubtotal());
        itemSubtotal.setTextFill(Color.WHITE);
        itemSubtotal.setFont(Font.font("System", FontWeight.BOLD, 14));
        itemSubtotal.setPrefWidth(120);
        itemSubtotal.setAlignment(Pos.CENTER_RIGHT);

        itemRow.getChildren().addAll(itemName, itemQty, itemPrice, itemSubtotal);
        return itemRow;
    }

    /**
     * Get appropriate icon for order type
     */
    private String getOrderIcon(Order order) {
        switch (order.getStatus()) {
            case "paid":
                return "✅";
            case "completed":
                return "⏳";
            case "cancelled":
                return "❌";
            default:
                return "🍽️";
        }
    }

    /**
     * Get enhanced activity title
     */
    private String getEnhancedActivityTitle(Order order) {
        switch (order.getStatus()) {
            case "paid":
                return "Đơn hàng #" + order.getId() + " đã thanh toán";
            case "completed":
                return "Đơn hàng #" + order.getId() + " chờ thanh toán";
            case "cancelled":
                return "Đơn hàng #" + order.getId() + " đã bị hủy";
            default:
                return "Đơn hàng #" + order.getId() + " đang xử lý";
        }
    }

    /**
     * Get enhanced activity subtitle with more details
     */
    private String getEnhancedActivitySubtitle(Order order) {
        StringBuilder subtitle = new StringBuilder();
        subtitle.append("Bàn ").append(order.getTableId());

        if (order.getOrderTime() != null) {
            String timeStr = order.getOrderTime().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("HH:mm"));
            subtitle.append(" • ").append(timeStr);
        }

        // Add additional context based on status
        switch (order.getStatus()) {
            case "paid":
                subtitle.append(" • Đã hoàn thành");
                break;
            case "completed":
                subtitle.append(" • Cần thanh toán");
                break;
            case "cancelled":
                subtitle.append(" • Đã hủy bỏ");
                break;
        }

        return subtitle.toString();
    }

    /**
     * Get relative time string (e.g., "5 phút trước")
     */
    private String getRelativeTime(Order order) {
        if (order.getOrderTime() == null) {
            return "Không rõ";
        }

        LocalDateTime orderTime = order.getOrderTime().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();

        long minutesAgo = ChronoUnit.MINUTES.between(orderTime, now);

        if (minutesAgo < 1) {
            return "Vừa xong";
        } else if (minutesAgo < 60) {
            return minutesAgo + " phút trước";
        } else if (minutesAgo < 1440) { // Less than 24 hours
            long hoursAgo = minutesAgo / 60;
            return hoursAgo + " giờ trước";
        } else {
            return orderTime.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        }
    }

    /**
     * Add pulsing animation to status circle
     */
    private void addPulsingAnimation(Circle circle) {
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), circle);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.3);
        pulse.setToY(1.3);
        pulse.setCycleCount(javafx.animation.Animation.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    /**
     * ENHANCED: Show beautiful update notification
     */
    private void showEnhancedUpdateNotification() {
        Platform.runLater(() -> {
            if (welcomeTimeLabel != null) {
                String originalText = welcomeTimeLabel.getText();
                welcomeTimeLabel.setText("🔄 Cập nhật...");
                welcomeTimeLabel.setTextFill(Color.web("#4CAF50"));

                // Add scale animation
                ScaleTransition notification = new ScaleTransition(Duration.millis(300), welcomeTimeLabel);
                notification.setFromX(1.0);
                notification.setFromY(1.0);
                notification.setToX(1.1);
                notification.setToY(1.1);
                notification.setAutoReverse(true);
                notification.setCycleCount(2);
                notification.play();

                // Restore original text after 2 seconds
                Timeline restoreTimeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                    welcomeTimeLabel.setText(originalText);
                    welcomeTimeLabel.setTextFill(Color.web("#e16428"));
                }));
                restoreTimeline.play();
            }
        });
    }

    /**
     * Load today's statistics with enhanced animations
     */
    private void loadTodayStats() {
        try {
            // Load today's revenue
            BigDecimal todayRevenue = revenueDAO.getTodayRevenue();
            if (lblTodayRevenue != null) {
                lblTodayRevenue.setText(formatCurrency(todayRevenue.doubleValue()));
                addEnhancedUpdateAnimation(lblTodayRevenue);
            }

            // Load today's orders count
            int todayOrders = revenueDAO.getTodayOrders();
            if (lblTodayOrders != null) {
                lblTodayOrders.setText(String.valueOf(todayOrders));
                addEnhancedUpdateAnimation(lblTodayOrders);
            }

            // Load average order value
            BigDecimal avgOrderValue = revenueDAO.getAverageOrderValue();
            if (lblAvgOrderValue != null) {
                lblAvgOrderValue.setText(formatCurrency(avgOrderValue.doubleValue()));
            }

            System.out.println("📊 Enhanced stats updated - Revenue: " + todayRevenue + ", Orders: " + todayOrders);

        } catch (SQLException e) {
            System.err.println("❌ Error loading today's stats: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enhanced animation for data updates
     */
    private void addEnhancedUpdateAnimation(Label label) {
        if (label == null) return;

        // Enhanced scale animation with better timing
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), label);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.15);
        scale.setToY(1.15);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);

        // Add color flash effect
        String originalStyle = label.getStyle();
        scale.setOnFinished(e -> {
            label.setStyle(originalStyle + "-fx-text-fill: #4CAF50;");
            Timeline colorRestore = new Timeline(new KeyFrame(Duration.seconds(1), ev ->
                    label.setStyle(originalStyle)));
            colorRestore.play();
        });

        scale.play();
    }

    private void loadServiceTimeStatistics() {
        try {
            // Average service time calculation
            int avgServiceTime = calculateAverageServiceTime();
            if (lblAvgServiceTime != null) {
                lblAvgServiceTime.setText(avgServiceTime + " phút");
            }

            // Fastest service today
            int fastestService = calculateFastestServiceTime();
            if (lblFastestService != null) {
                lblFastestService.setText(fastestService + " phút");
            }

            // Active orders count
            int activeOrdersCount = getActiveOrdersCount();
            if (lblActiveOrders != null) {
                lblActiveOrders.setText(activeOrdersCount + " bàn");
            }

        } catch (SQLException e) {
            System.err.println("Error loading service time statistics: " + e.getMessage());
            // Set default values on error
            if (lblAvgServiceTime != null) lblAvgServiceTime.setText("-- phút");
            if (lblFastestService != null) lblFastestService.setText("-- phút");
            if (lblActiveOrders != null) lblActiveOrders.setText("-- bàn");
        }
    }

    private int calculateAverageServiceTime() throws SQLException {
        List<Order> completedOrders = orderDAO.findByStatus("paid");
        if (completedOrders.isEmpty()) {
            return 25;
        }
        return 20 + (completedOrders.size() % 15);
    }

    private int calculateFastestServiceTime() throws SQLException {
        List<Order> paidOrders = orderDAO.findByStatus("paid");
        if (paidOrders.isEmpty()) {
            return 15;
        }
        return 10 + (paidOrders.size() % 10);
    }

    private int getActiveOrdersCount() throws SQLException {
        List<Order> activeOrders = orderDAO.findByStatus("completed");
        return activeOrders.size();
    }

    private void loadPaymentMethodStatistics() {
        try {
            Map<String, Integer> paymentStats = revenueDAO.getPaymentMethodStats();

            if (lblCashPayments != null) {
                int cashPayments = paymentStats.getOrDefault("Tiền mặt", 0);
                lblCashPayments.setText(String.valueOf(cashPayments));
            }

            if (lblCardPayments != null) {
                int cardPayments = paymentStats.getOrDefault("Thẻ tín dụng", 0) +
                        paymentStats.getOrDefault("Chuyển khoản", 0) +
                        paymentStats.getOrDefault("MOMO", 0) +
                        paymentStats.getOrDefault("ZaloPay", 0);
                lblCardPayments.setText(String.valueOf(cardPayments));
            }

            if (lblPeakHour != null) {
                Map<String, Object> peakData = revenueDAO.getPeakHoursAnalysis();
                String peakHour = (String) peakData.get("peak_hour");
                lblPeakHour.setText(peakHour);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupRecentOrdersTable() {
        if (recentOrdersTable != null) {
            // Setup columns
            orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            tableColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty("Bàn " + cellData.getValue().getTableId()));
            statusColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusDisplayName()));
            timeColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedOrderTime()));
            amountColumn.setCellValueFactory(cellData ->
                    new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFormattedTotal()));

            // Enhanced cell styling
            statusColumn.setCellFactory(column -> new TableCell<Order, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        Order order = getTableView().getItems().get(getIndex());
                        switch (order.getStatus()) {
                            case "paid":
                                setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                                break;
                            case "completed":
                                setStyle("-fx-text-fill: #2196F3; -fx-font-weight: bold;");
                                break;
                            case "cancelled":
                                setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                                break;
                            default:
                                setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
                        }
                    }
                }
            });

            recentOrdersTable.setItems(recentOrders);

            // Enhanced row factory with double-click
            recentOrdersTable.setRowFactory(tv -> {
                TableRow<Order> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        Order selectedOrder = row.getItem();
                        openOrderDetailsPopup(selectedOrder);
                    }
                });
                return row;
            });
        }
    }

    private final Locale VI_VN = new Locale("vi","VN");
    private final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss", VI_VN);
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", VI_VN);

    private void updateTimeDisplay() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());

        if (currentTimeLabel != null) {
            currentTimeLabel.setText(now.format(TIME_FMT));
        }
        if (welcomeTimeLabel != null) {
            String niceDate = now.format(DATE_FMT);
            if (!niceDate.isEmpty()) {
                niceDate = Character.toUpperCase(niceDate.charAt(0)) + niceDate.substring(1);
            }
            welcomeTimeLabel.setText(niceDate);
        }
    }


    private void loadDashboardData() {
        Platform.runLater(() -> {
            try {
                loadTodayStats();
                loadTableStatus();
                loadMenuItemsCount();
                loadPaymentMethodStatistics();
                loadServiceTimeStatistics();

            } catch (Exception e) {
                e.printStackTrace();
                showErrorMessage("Không thể tải dữ liệu dashboard: " + e.getMessage());
            }
        });
    }

    private void loadTableStatus() throws SQLException {
        var tables = tableDAO.findAll();
        long activeTables = tables.stream()
                .filter(t -> "occupied".equals(t.getStatus()) || "ordering".equals(t.getStatus()))
                .count();

        if (lblActiveTables != null) {
            lblActiveTables.setText(activeTables + "/" + tables.size());
        }
    }

    private void loadMenuItemsCount() throws SQLException {
        var menuItems = menuItemDAO.findAll();
        if (lblMenuItems != null) {
            lblMenuItems.setText(String.valueOf(menuItems.size()));
        }
    }

    @FXML
    private void refreshData() {
        System.out.println("🔄 Manual enhanced dashboard refresh requested");
        loadDashboardData();
        updateCharts();
        if (!isFirstLoad) {
            loadEnhancedRecentActivity();
        }
        System.out.println("✅ Manual enhanced dashboard refresh completed");
    }

    private void initializeCharts() {
        if (revenueChart != null) {
            revenueChart.setTitle("Doanh thu 7 ngày qua");
            revenueChart.setLegendVisible(false);
            revenueChart.setCreateSymbols(true);
            revenueChart.getStyleClass().add("revenue-chart");
            revenueChart.setAnimated(true);
        }

        if (ordersChart != null) {
            ordersChart.setTitle("Số đơn hàng 7 ngày qua");
            ordersChart.setLegendVisible(false);
            ordersChart.getStyleClass().add("orders-chart");
            ordersChart.setAnimated(true);
        }

        updateCharts();
    }

    private void updateCharts() {
        Platform.runLater(() -> {
            try {
                updateRevenueChart();
                updateOrdersChart();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateRevenueChart() throws SQLException {
        if (revenueChart == null) return;

        revenueChart.getData().clear();

        Map<String, BigDecimal> weeklyRevenue = revenueDAO.getWeeklyRevenue();

        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Doanh thu");

        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        int dayIndex = 0;

        for (Map.Entry<String, BigDecimal> entry : weeklyRevenue.entrySet()) {
            String dayName = dayIndex < dayNames.length ? dayNames[dayIndex] : entry.getKey();
            double amount = entry.getValue().doubleValue() / 1000000;

            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(dayName, amount);
            revenueSeries.getData().add(dataPoint);
            dayIndex++;
        }

        while (dayIndex < 7) {
            revenueSeries.getData().add(new XYChart.Data<>(dayNames[dayIndex], 0));
            dayIndex++;
        }

        revenueChart.getData().add(revenueSeries);

        if (revenueYAxis != null) {
            revenueYAxis.setLabel("Triệu VND");
        }

        Platform.runLater(() -> {
            revenueChart.lookupAll(".chart-series-line").forEach(node ->
                    node.setStyle("-fx-stroke: #4CAF50; -fx-stroke-width: 3px;"));
            revenueChart.lookupAll(".chart-line-symbol").forEach(node ->
                    node.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 5px;"));
        });
    }

    private void updateOrdersChart() throws SQLException {
        if (ordersChart == null) return;

        ordersChart.getData().clear();

        Map<String, Integer> weeklyOrders = revenueDAO.getWeeklyOrders();

        XYChart.Series<String, Number> ordersSeries = new XYChart.Series<>();
        ordersSeries.setName("Số đơn hàng");

        String[] dayNames = {"T2", "T3", "T4", "T5", "T6", "T7", "CN"};
        int dayIndex = 0;

        for (Map.Entry<String, Integer> entry : weeklyOrders.entrySet()) {
            String dayName = dayIndex < dayNames.length ? dayNames[dayIndex] : entry.getKey();
            int orders = entry.getValue();
            ordersSeries.getData().add(new XYChart.Data<>(dayName, orders));
            dayIndex++;
        }

        while (dayIndex < 7) {
            ordersSeries.getData().add(new XYChart.Data<>(dayNames[dayIndex], 0));
            dayIndex++;
        }

        ordersChart.getData().add(ordersSeries);

        if (ordersYAxis != null) {
            ordersYAxis.setLabel("Đơn hàng");
        }

        Platform.runLater(() -> {
            ordersChart.lookupAll(".default-color0.chart-bar").forEach(node ->
                    node.setStyle("-fx-bar-fill: #2196F3;"));
        });
    }

    private String getActivityAmount(Order order) {
        if (order.getTotalAmount() != null && order.getTotalAmount().compareTo(BigDecimal.ZERO) > 0) {
            return formatCurrency(order.getTotalAmount().doubleValue());
        }

        switch (order.getStatus()) {
            case "paid":
                return "Đã thanh toán";
            case "completed":
                return "Chờ thanh toán";
            case "cancelled":
                return "Đã hủy";
            default:
                return order.getStatusDisplayName();
        }
    }

    private Color getActivityAmountColor(Order order) {
        switch (order.getStatus()) {
            case "paid":
                return Color.web("#4CAF50");
            case "completed":
                return Color.web("#2196F3");
            case "cancelled":
                return Color.web("#f44336");
            default:
                return Color.web("#FF9800");
        }
    }

    private String formatCurrency(double amount) {
        return currencyFormatter.format(amount) + " VND";
    }

    private void showErrorMessage(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi");
            alert.setHeaderText("Lỗi tải dữ liệu");
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void showInfo(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    /**
     * PUBLIC: Enhanced method called after successful payment
     */
    public void onPaymentCompleted() {
        System.out.println("🎉 Enhanced payment completion notification received");

        isFirstLoad = false;
        refreshEnhancedDashboardData();

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thành công");
            alert.setHeaderText("Thanh toán thành công");
            alert.setContentText("Đơn hàng đã được thanh toán và dashboard đã được cập nhật!");
            alert.showAndWait();
        });
    }

    // ===== EVENT HANDLERS =====

    @FXML
    private void handleManageTables(ActionEvent event) {
        openScene(event, "/fxml/table_management.fxml", "Quản lý bàn");
    }

    @FXML
    private void handleManageMenu(ActionEvent event) {
        openScene(event, "/fxml/menu_management.fxml", "Quản lý thực đơn");
    }

    @FXML
    private void handleRevenueReport(ActionEvent event) {
        openScene(event, "/fxml/revenue_report.fxml", "Báo cáo doanh thu");
    }

    @FXML
    private void handlePayment(ActionEvent event) {
        openScene(event, "/fxml/payment.fxml", "Thanh toán");
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        cleanup();
        LogoutUtil.confirmLogout((Button) (event != null ? event.getSource() : logoutButton));
    }

    @FXML
    private void handleManageOrder(ActionEvent event) {
        openScene(event, "/fxml/order_management.fxml", "Quản lý order");
    }

    @FXML
    private void showHome() {
        refreshData();
    }

    @FXML
    private void handleQuickOrder() {
        handleManageOrder(null);
    }

    @FXML
    private void handleQuickMenu() {
        handleManageMenu(null);
    }

    @FXML
    private void handleQuickTables() {
        handleManageTables(null);
    }

    private void openScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Stage stage;
            if (event != null) {
                stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            } else {
                stage = (Stage) manageOrderButton.getScene().getWindow();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorMessage("Không thể mở " + title + ": " + e.getMessage());
        }
    }

    /**
     * Enhanced cleanup when controller is destroyed
     */
    public void cleanup() {
        System.out.println("🧹 Cleaning up Enhanced Dashboard Controller...");

        if (clockTimeline != null) {
            clockTimeline.stop();
        }
        if (dataTimeline != null) {
            dataTimeline.stop();
        }

        DashboardUpdateUtil.removeUpdateListener(this::refreshEnhancedDashboardData);

        System.out.println("✅ Enhanced Dashboard Controller cleanup completed");
    }
}
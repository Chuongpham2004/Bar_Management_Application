package com.barmanagement.controller;

import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.Table;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class TableController implements Initializable {

    // Sidebar
    @FXML private Button dashboardBtn;
    @FXML private Button tablesBtn;
    @FXML private Button ordersBtn;
    @FXML private Button paymentBtn;
    @FXML private Label staffNameLabel;

    // Filters
    @FXML private ComboBox<String> locationFilterCombo; // demo giữ lại UI (không ràng buộc DB)
    @FXML private ComboBox<String> statusFilterCombo;
    @FXML private Button refreshBtn;

    // Statistics
    @FXML private Label availableCountLabel;
    @FXML private Label occupiedCountLabel;
    @FXML private Label reservedCountLabel;
    @FXML private Label totalRevenueLabel;

    // Table Grids
    @FXML private FlowPane vipTablesFlow;
    @FXML private FlowPane mainFloorTablesFlow;
    @FXML private FlowPane outdoorTablesFlow;
    @FXML private FlowPane barCounterTablesFlow;

    // Controls
    @FXML private ProgressIndicator loadingIndicator;
    @FXML private Button addTableBtn;

    private final TableDAO tableDAO = new TableDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private List<Table> allTables = new ArrayList<>();
    private String currentStatusFilter = "Tất cả";

    // mapping trạng thái -> màu
    private static final Map<String, String> STATUS_COLOR = Map.of(
            "available", "#4CAF50",
            "occupied", "#FF5722",
            "reserved", "#FF9800"
    );

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupFilters();
        refreshTables();
    }

    private void setupFilters() {
        // Location giữ nguyên cho đẹp UI (không filter DB)
        locationFilterCombo.getItems().addAll("Tất cả", "VIP", "Sảnh chính", "Ngoài trời", "Quầy bar");
        locationFilterCombo.setValue("Tất cả");
        locationFilterCombo.setOnAction(e -> refreshTables());

        statusFilterCombo.getItems().addAll("Tất cả", "Trống", "Đang sử dụng", "Đã đặt");
        statusFilterCombo.setValue("Tất cả");
        statusFilterCombo.setOnAction(e -> {
            currentStatusFilter = statusFilterCombo.getValue();
            refreshTables();
        });
    }

    @FXML
    private void refreshTables() {
        setLoadingState(true);

        PauseTransition pause = new PauseTransition(Duration.millis(300));
        pause.setOnFinished(e -> {
            try {
                allTables = tableDAO.findAll(); // lấy từ DB
                renderTables();
                updateStatistics();
            } catch (SQLException ex) {
                showError("Không tải được danh sách bàn: " + ex.getMessage());
            } finally {
                setLoadingState(false);
            }
        });
        pause.play();
    }

    private void renderTables() {
        clearAllTables();

        List<Table> filtered = allTables.stream().filter(t -> {
            if (!"Tất cả".equals(currentStatusFilter)) {
                String vi = switch (t.getStatus()) {
                    case "available" -> "Trống";
                    case "occupied" -> "Đang sử dụng";
                    case "reserved" -> "Đã đặt";
                    default -> t.getStatus();
                };
                if (!Objects.equals(vi, currentStatusFilter)) return false;
            }
            return true;
        }).collect(Collectors.toList());

        // Vì DB chưa có cột khu vực, tạm gán đều vào "Sảnh chính"
        for (Table t : filtered) {
            Button btn = createTableButton(t);
            mainFloorTablesFlow.getChildren().add(btn);
        }
    }

    private Button createTableButton(Table t) {
        String color = STATUS_COLOR.getOrDefault(t.getStatus(), "#9E9E9E");

        Button btn = new Button();
        btn.setPrefSize(130, 110);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white; -fx-background-radius: 12;" +
                        "-fx-font-size: 12px; -fx-font-weight: bold; -fx-alignment: center;"
        );

        String viStatus = switch (t.getStatus()) {
            case "available" -> "Trống";
            case "occupied" -> "Đang sử dụng";
            case "reserved" -> "Đã đặt";
            default -> t.getStatus();
        };

        String content = "Bàn " + t.getTableNumber() +
                "\n👥 " + t.getCapacity() + " chỗ" +
                "\n📍 " + viStatus;

        btn.setText(content);

        DropShadow shadow = new DropShadow();
        btn.setEffect(shadow);

        btn.setOnAction(e -> onTableClick(t));
        return btn;
    }

    private void onTableClick(Table t) {
        List<ButtonType> extra = new ArrayList<>();
        switch (t.getStatus()) {
            case "available" -> extra.add(new ButtonType("🎯 Đặt bàn"));
            case "occupied" -> extra.add(new ButtonType("💳 Thanh toán"));
            case "reserved" -> extra.add(new ButtonType("✅ Check-in"));
        }
        extra.add(new ButtonType("📝 Chỉnh sửa"));

        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Bàn " + t.getTableNumber());
        dlg.setHeaderText("Thông tin bàn");
        dlg.setContentText("Sức chứa: " + t.getCapacity() + "\nTrạng thái: " + t.getStatus());
        dlg.getButtonTypes().setAll(extra);
        Optional<ButtonType> r = dlg.showAndWait();
        if (r.isEmpty()) return;

        String text = r.get().getText();
        try {
            switch (text) {
                case "🎯 Đặt bàn" -> {
                    tableDAO.updateStatus(t.getId(), "reserved");
                    refreshTables();
                }
                case "✅ Check-in" -> {
                    tableDAO.updateStatus(t.getId(), "occupied");
                    refreshTables();
                }
                case "💳 Thanh toán" -> {
                    // tìm order đang mở của bàn (nếu có) và mark paid (DAO mới thêm bên dưới)
                    Integer activeOrderId = orderDAO.findActiveOrderIdByTable(t.getId());
                    if (activeOrderId != null) {
                        orderDAO.updateStatus(activeOrderId, "paid");
                    } else {
                        // nếu không có order, chỉ mở lại bàn
                        tableDAO.updateStatus(t.getId(), "available");
                    }
                    refreshTables();
                    showInfo("Đã thanh toán bàn " + t.getTableNumber());
                }
                case "📝 Chỉnh sửa" -> showEditDialog(t);
            }
        } catch (SQLException ex) {
            showError(ex.getMessage());
        }
    }

    private void showEditDialog(Table t) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(t.getStatus(), "available", "occupied", "reserved");
        dialog.setTitle("Chỉnh sửa trạng thái");
        dialog.setHeaderText("Bàn " + t.getTableNumber());
        dialog.setContentText("Chọn trạng thái mới:");

        dialog.showAndWait().ifPresent(status -> {
            try {
                tableDAO.updateStatus(t.getId(), status);
                refreshTables();
                showInfo("Đã cập nhật trạng thái.");
            } catch (SQLException e) {
                showError(e.getMessage());
            }
        });
    }

    private void updateStatistics() {
        long available = allTables.stream().filter(t -> "available".equals(t.getStatus())).count();
        long occupied  = allTables.stream().filter(t -> "occupied".equals(t.getStatus())).count();
        long reserved  = allTables.stream().filter(t -> "reserved".equals(t.getStatus())).count();

        // Doanh thu tổng: ở đây không có cột doanh thu, demo để 0
        BigDecimal totalRevenue = BigDecimal.ZERO;

        Platform.runLater(() -> {
            availableCountLabel.setText(String.valueOf(available));
            occupiedCountLabel.setText(String.valueOf(occupied));
            reservedCountLabel.setText(String.valueOf(reserved));
            totalRevenueLabel.setText(String.format("%.1fM", totalRevenue.doubleValue() / 1_000_000d));
        });
    }

    private void clearAllTables() {
        vipTablesFlow.getChildren().clear();
        mainFloorTablesFlow.getChildren().clear();
        outdoorTablesFlow.getChildren().clear();
        barCounterTablesFlow.getChildren().clear();
    }

    private void setLoadingState(boolean loading) {
        loadingIndicator.setVisible(loading);
        refreshBtn.setDisable(loading);
        addTableBtn.setDisable(loading);
    }

    // Navigation
    @FXML private void showDashboard() { navigate("/fxml/home.fxml", "Dashboard - BarFlow"); }
    @FXML private void showTables()    { /* đang ở đây */ }
    @FXML private void showOrders()    { navigate("/fxml/orders.fxml", "Gọi món - BarFlow"); }
    @FXML private void showPayment()   { navigate("/fxml/payment.fxml", "Thanh toán - BarFlow"); }

    private void navigate(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) refreshBtn.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            showError("Không thể điều hướng: " + e.getMessage());
        }
    }

    private void showInfo(String m) { new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
    private void showError(String m){ new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
}

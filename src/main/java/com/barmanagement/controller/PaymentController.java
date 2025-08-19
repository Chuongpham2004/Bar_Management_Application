package com.barmanagement.controller;

import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.OrderItemDAO;
import com.barmanagement.dao.PaymentDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class PaymentController {

    @FXML private ComboBox<String> tableComboBox; // Danh sách bàn đang sử dụng (occupied)
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> itemNameCol;
    @FXML private TableColumn<OrderItem, Integer> quantityCol;
    @FXML private TableColumn<OrderItem, Double> priceCol;
    @FXML private TableColumn<OrderItem, Double> totalCol;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;

    private final OrderDAO orderDAO = new OrderDAO();
    private final OrderItemDAO orderItemDAO = new OrderItemDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final TableDAO tableDAO = new TableDAO();

    private Order currentOrder;

    // ==== Điều hướng UI ====
    @FXML private void showHome() { SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel); }
    @FXML private void goBack() { SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel); }
    @FXML private void showDashboard() { SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel); }
    @FXML private void showMenu() { SceneUtil.openScene("/fxml/menu_management.fxml", totalLabel); }
    @FXML private void showOrder() { SceneUtil.openScene("/fxml/order_management.fxml", totalLabel); }
    @FXML private void showTableManagement() { SceneUtil.openScene("/fxml/table_management.fxml", totalLabel); }
    @FXML private void handleLogout() { LogoutUtil.confirmLogout(totalLabel); }

    @FXML
    private void initialize() {
        // Cột bảng
        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalCol.setCellValueFactory(cd ->
                new ReadOnlyObjectWrapper<>(cd.getValue().getPrice() * cd.getValue().getQuantity()));

        // Phương thức thanh toán (hiển thị VN, map sang mã DB khi lưu)
        paymentMethodComboBox.setItems(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản", "MOMO"));
        paymentMethodComboBox.getSelectionModel().selectFirst();

        // Load danh sách bàn đang sử dụng từ DB
        loadOccupiedTables();

        tableComboBox.setOnAction(e -> loadOrderBySelectedTable());

        // Tự chọn bàn đầu tiên nếu có
        if (!tableComboBox.getItems().isEmpty()) {
            tableComboBox.getSelectionModel().select(0);
            loadOrderBySelectedTable();
        }
    }

    /** Lấy danh sách bàn có status='occupied' */
    private void loadOccupiedTables() {
        try {
            var all = tableDAO.findAll();
            var occupied = all.stream()
                    .filter(t -> "occupied".equalsIgnoreCase(t.getStatus()))
                    .map(t -> t.getTableName() + " (#" + t.getId() + ")")
                    .toList();
            tableComboBox.setItems(FXCollections.observableArrayList(occupied));
            currentOrder = null;
            orderTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("0 VND");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Không tải được danh sách bàn đang sử dụng.\n" + e.getMessage());
        }
    }

    /** Parse "Bàn 3 (#3)" -> 3 ; fallback mọi ký tự số */
    private int parseTableIdFromDisplay(String display) {
        var m = Pattern.compile("#(\\d+)").matcher(display);
        if (m.find()) return Integer.parseInt(m.group(1));
        return Integer.parseInt(display.replaceAll("\\D", ""));
    }

    /** Khi chọn bàn, load đơn pending mới nhất + items + tổng (có fallback) */
    private void loadOrderBySelectedTable() {
        String selected = tableComboBox.getValue();
        if (selected == null || selected.isBlank()) {
            orderTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("0 VND");
            currentOrder = null;
            return;
        }
        int tableId = parseTableIdFromDisplay(selected);

        try {
            // 1) Ưu tiên đơn 'pending'
            currentOrder = orderDAO.findPendingByTable(tableId);

            // 2) Fallback: không có 'pending' -> lấy order mới nhất có item
            if (currentOrder == null) {
                currentOrder = orderDAO.findLatestOrderWithItemsByTable(tableId);
                if (currentOrder != null) {
                    showInfo("Không thấy đơn 'pending'. Đang hiển thị đơn gần nhất của bàn này (trạng thái: "
                            + currentOrder.getStatus() + ").");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentOrder = null;
        }

        if (currentOrder == null) {
            orderTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("0 VND");
            showInfo("Bàn này hiện chưa có đơn nào có món.");
            return;
        }

        try {
            ObservableList<OrderItem> orderItems =
                    FXCollections.observableArrayList(orderItemDAO.findByOrderId(currentOrder.getId()));
            orderTable.setItems(orderItems);

            double total = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
            totalLabel.setText(String.format("%,.0f VND", total));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi tải danh sách món!");
        }
    }

    /** Map tên hiển thị -> mã lưu DB */
    private String mapPaymentMethodToCode(String uiValue) {
        if (uiValue == null) return "cash";
        uiValue = uiValue.trim().toLowerCase();
        return switch (uiValue) {
            case "tiền mặt" -> "cash";
            case "chuyển khoản" -> "transfer";
            case "momo" -> "momo";
            default -> "cash";
        };
    }
    


    private String getSelectedPaymentMethod() {
        return paymentMethodComboBox.getValue();
    }

    private double parseTotalAmount() {
        String totalText = totalLabel.getText().replaceAll("[^0-9]", "");
        if (totalText.isEmpty()) return 0;
        return Double.parseDouble(totalText);
    }

    @FXML
    private void onConfirmPayment() {
        if (currentOrder == null) {
            showAlert("Chưa chọn bàn hoặc không có hóa đơn tạm.");
            return;
        }

        double totalAmountDouble = parseTotalAmount();
        BigDecimal totalAmount = BigDecimal.valueOf(totalAmountDouble);
        String methodCode = mapPaymentMethodToCode(getSelectedPaymentMethod());

        // ✅ Gọi DAO: nó sẽ ưu tiên SP CompleteOrder, nếu không có thì chạy manual (đồng bộ với schema).
        try {
            orderDAO.complete(currentOrder.getId());
        } catch (SQLException ex) {
            ex.printStackTrace();
            showAlert("Thanh toán thất bại. Vui lòng thử lại.");
            return;
        }


        showInfo("Thanh toán thành công. Bàn đã được giải phóng!");
        loadOccupiedTables(); // refresh list (bàn vừa thanh toán biến mất)
    }

    @FXML
    private void onCancel() {
        tableComboBox.getSelectionModel().clearSelection();
        orderTable.setItems(FXCollections.observableArrayList());
        totalLabel.setText("0 VND");
        paymentMethodComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void exportMenu() {
        showInfo("Chức năng xuất menu sẽ được phát triển trong phiên bản tới!");
    }

    @FXML
    private void importMenu() {
        showInfo("Chức năng nhập menu sẽ được phát triển trong phiên bản tới!");
    }

    // ==== Helpers UI ====
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}

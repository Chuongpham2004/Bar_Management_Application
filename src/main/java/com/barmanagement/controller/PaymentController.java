package com.barmanagement.controller;

import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.OrderItemDAO;
import com.barmanagement.dao.PaymentDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.MenuItem;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.Payment;
import java.sql.Timestamp;
import java.sql.SQLException;
import com.barmanagement.util.SceneUtil;



import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;

public class PaymentController {

    @FXML
    private ComboBox<String> tableComboBox; // Danh sách bàn
    @FXML private TableView<OrderItem> orderTable;
    @FXML private TableColumn<OrderItem, String> itemNameCol;
    @FXML private TableColumn<OrderItem, Integer> quantityCol;
    @FXML private TableColumn<OrderItem, Double> priceCol;
    @FXML private TableColumn<OrderItem, Double> totalCol;
    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodComboBox;
    //
    @FXML
    private void showHome() {
        SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
    }
    @FXML
    private void goBack() {
        SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
    }

    @FXML
    private void showDashboard() {
        SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
    }

    @FXML
    private void showMenu() {
        SceneUtil.openScene("/fxml/menu_management.fxml", totalLabel);
    }

    @FXML
    private void showOrder() {
        SceneUtil.openScene("/fxml/order_management.fxml", totalLabel);
    }

    @FXML
    private void showTableManagement() {
        SceneUtil.openScene("/fxml/table_management.fxml", totalLabel);
    }

    @FXML
    private void handleLogout() {
        SceneUtil.openScene("/fxml/login.fxml", totalLabel);
    }



    private OrderDAO orderDAO = new OrderDAO();
    private OrderItemDAO orderItemDAO = new OrderItemDAO();
    private PaymentDAO paymentDAO = new PaymentDAO();
    private TableDAO tableDAO = new TableDAO();

    private Order currentOrder;

    @FXML
    private void initialize() {
        tableComboBox.setItems(FXCollections.observableArrayList("Bàn 1", "Bàn 2", "Bàn 3"));
        tableComboBox.setOnAction(e -> loadOrderBySelectedTable());

        paymentMethodComboBox.setItems(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản", "MOMO"));
        paymentMethodComboBox.getSelectionModel().selectFirst(); // chọn mặc định

        itemNameCol.setCellValueFactory(new PropertyValueFactory<>("menuItemName"));
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        totalCol.setCellValueFactory(cellData -> new ReadOnlyObjectWrapper<>(
                cellData.getValue().getPrice() * cellData.getValue().getQuantity()
        ));

        if (!tableComboBox.getItems().isEmpty()) {
            tableComboBox.getSelectionModel().select(0);
            loadOrderBySelectedTable();
        }
    }
    private void loadOrderBySelectedTable() {
        String selectedTableName = tableComboBox.getValue();
        int tableId = parseTableIdFromName(selectedTableName);

        try {
            currentOrder = orderDAO.findPendingByTable(tableId);
        } catch (Exception e) {
            e.printStackTrace();
            currentOrder = null;
        }

        if (currentOrder == null) {
            orderTable.setItems(FXCollections.observableArrayList());
            totalLabel.setText("0 VND");
            return;
        }

        try {
            ObservableList<OrderItem> orderItems = FXCollections.observableArrayList(
                    orderItemDAO.findByOrderId(currentOrder.getId())
            );
            orderTable.setItems(orderItems);

            // Tính tổng tiền
            double total = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
            totalLabel.setText(String.format("%.0f VND", total));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi khi tải danh sách món!");
        }
    }

    private int parseTableIdFromName(String tableName) {
        // Ví dụ bàn "Bàn 3" => return 3
        return Integer.parseInt(tableName.replaceAll("[^0-9]", ""));
    }

    @FXML
    private void onConfirmPayment() {
        if (currentOrder == null) {
            showAlert("Chưa chọn bàn hoặc không có hóa đơn tạm.");
            return;
        }

        String method = getSelectedPaymentMethod();
        double totalAmount = parseTotalAmount();

        Payment payment = new Payment();
        payment.setOrderId(currentOrder.getId());
        payment.setTotalAmount(totalAmount);
        payment.setPaymentMethod(method);
        payment.setPaymentTime(new Timestamp(System.currentTimeMillis()));

        boolean success = paymentDAO.insertPayment(payment);
        if (success) {
            try {
                tableDAO.updateStatus(currentOrder.getTableId(), "free"); // cập nhật bàn trống
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Lỗi khi cập nhật trạng thái bàn.");
                return;
            }
            showAlert("Thanh toán thành công!");
            loadOrderBySelectedTable(); // reload dữ liệu
        } else {
            showAlert("Thanh toán thất bại. Vui lòng thử lại.");
        }
    }

    private double parseTotalAmount() {
        String totalText = totalLabel.getText().replaceAll("[^0-9]", "");
        if (totalText.isEmpty()) return 0;
        return Double.parseDouble(totalText);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private String getSelectedPaymentMethod() {
        String method = paymentMethodComboBox.getValue();
        return (method != null && !method.isEmpty()) ? method : "Tiền mặt"; // fallback nếu người dùng chưa chọn
    }
    @FXML
    private void onCancel() {
        // ví dụ: reset giao diện, đóng form, hoặc làm gì đó
        tableComboBox.getSelectionModel().clearSelection();
        orderTable.setItems(FXCollections.observableArrayList());
        totalLabel.setText("0 VND");
        paymentMethodComboBox.getSelectionModel().selectFirst();
    }
    @FXML
    private void exportMenu() {
        // TODO: Implement export functionality
        showInfo("Chức năng xuất menu sẽ được phát triển trong phiên bản tới!");
    }

    @FXML
    private void importMenu() {
        // TODO: Implement import functionality
        showInfo("Chức năng nhập menu sẽ được phát triển trong phiên bản tới!");
    }
    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

}

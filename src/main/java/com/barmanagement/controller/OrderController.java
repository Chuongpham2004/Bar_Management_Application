package com.barmanagement.controller;

import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.MenuItem;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.Table;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import com.barmanagement.util.SceneUtil;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.Scene;


import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderController {

    @FXML private ComboBox<Table> cbTable;

    // Bảng menu
    @FXML private TableView<MenuItem> tblMenu;
    @FXML private TableColumn<MenuItem, String> colMName, colMCat;
    @FXML private TableColumn<MenuItem, Number> colMPrice;

    @FXML private Spinner<Integer> spQty;

    // Bảng item trong order
    @FXML private TableView<OrderItem> tblItems;
    @FXML private TableColumn<OrderItem, String> colIName;
    @FXML private TableColumn<OrderItem, Number> colIQty, colIPrice, colISubtotal;

    @FXML private Label lblOrderId, lblTotal;

    private final TableDAO tableDAO = new TableDAO();
    private final MenuItemDAO menuDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private final ObservableList<MenuItem> menuData = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> itemData = FXCollections.observableArrayList();
    private Map<Integer, MenuItem> menuMap = new HashMap<>();

    private Order current;


    @FXML
    public void initialize() {
        spQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        // ===== Menu columns =====
        colMName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colMCat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
        // price là double
        colMPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleDoubleProperty(c.getValue().getPrice()));
        tblMenu.setItems(menuData);

        // ===== Order items columns =====
        colIName.setCellValueFactory(c -> {
            MenuItem m = menuMap.get(c.getValue().getMenuItemId());
            return new javafx.beans.property.SimpleStringProperty(m != null ? m.getName() : "?");
        });

        colIQty.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity())
        );

        // Giá đơn vị (double) -> Number
        colIPrice.setCellValueFactory(c ->
                new javafx.beans.property.SimpleDoubleProperty(getUnitPrice(c.getValue()))
        );

        // Thành tiền = price * qty (double)
        colISubtotal.setCellValueFactory(c -> {
            double p = getUnitPrice(c.getValue());
            double sub = p * c.getValue().getQuantity();
            return new javafx.beans.property.SimpleDoubleProperty(sub);
        });

        tblItems.setItems(itemData);

        loadTables();
        loadMenu();
    }

    /** Lấy đơn giá cho 1 item: ưu tiên lấy từ OrderItem; nếu 0 thì tra theo menuMap */
    private double getUnitPrice(OrderItem it) {
        double p = it.getPrice(); // OrderItem.price là double, mặc định 0
        if (p > 0) return p;
        MenuItem m = menuMap.get(it.getMenuItemId());
        return (m != null) ? m.getPrice() : 0.0;
    }

    private void loadTables() {
        try {
            cbTable.setItems(FXCollections.observableArrayList(tableDAO.findAll()));
            cbTable.getSelectionModel().selectedItemProperty()
                    .addListener((o, a, b) -> loadOrCreatePending(b));
        } catch (SQLException e) { err(e); }
    }

    private void loadMenu() {
        try {
            menuData.setAll(menuDAO.findAll());
            menuMap = menuData.stream().collect(Collectors.toMap(MenuItem::getId, m -> m));
        } catch (Exception e) { err(e); }
    }

    private void loadOrCreatePending(Table t) {
        if (t == null) return;
        try {
            current = orderDAO.findPendingByTable(t.getId());
            if (current == null) {
                lblOrderId.setText("(chưa có)");
                itemData.clear();
                lblTotal.setText("0");
            } else {
                lblOrderId.setText("#" + current.getId());
                reloadItems();
            }
        } catch (SQLException e) { err(e); }
    }

    private void reloadItems() {
        try {
            itemData.setAll(orderDAO.findItems(current.getId())); // DAO JOIN menu_items để gắn unit price
            BigDecimal total = orderDAO.calcTotal(current.getId()); // SUM DECIMAL -> BigDecimal
            lblTotal.setText(total.toPlainString());
        } catch (SQLException e) { err(e); }
    }

    // ===== Actions =====
    @FXML
    public void newOrder() {
        Table t = cbTable.getSelectionModel().getSelectedItem();
        if (t == null) return;
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
            }
        } catch (SQLException e) { err(e); }
    }
    @FXML
    private void goBack() {
        // dùng tblItems (hay cbTable/tblMenu đều được)
        SceneUtil.openScene("/fxml/dashboard.fxml", tblItems);
    }

    @FXML
    public void addItem() {
        if (current == null) { info("Hãy tạo/chọn order đang mở."); return; }
        MenuItem m = tblMenu.getSelectionModel().getSelectedItem();
        if (m == null) return;
        try {
            orderDAO.addItem(current.getId(), m.getId(), spQty.getValue()); // nếu đã có -> tăng SL
            reloadItems();
        } catch (SQLException e) { err(e); }
    }

    @FXML
    public void removeItem() {
        OrderItem it = tblItems.getSelectionModel().getSelectedItem();
        if (it == null) return;
        try {
            orderDAO.removeItem(it.getId());
            reloadItems();
        } catch (SQLException e) { err(e); }
    }

    @FXML
    public void completeOrder() {
        if (current == null) return;
        try {
            orderDAO.complete(current.getId(), "pending"); // Hoặc trạng thái phù hợp
            current = null;
            afterComplete();
        } catch (SQLException e) {
            err(e);
        }
    }


    @FXML public void reload() { afterComplete(); }

    private void afterComplete() {
        itemData.clear();
        lblOrderId.setText("(chưa có)");
        lblTotal.setText("0");
        loadTables();
    }

    private void info(String m) { new Alert(Alert.AlertType.INFORMATION, m).showAndWait(); }
    private void err(Exception e) { new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); e.printStackTrace(); }
// / Thêm nút thanh toán
    // Nếu đã có order đang mở thì chuyển sang scene thanh toán
    @FXML
    private Button paymentButton; // nút thanh toán

    @FXML
    public void openPayment() {
        if (current == null) {
            info("Chưa có order để thanh toán.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/payment.fxml"));
            Scene scene = new Scene(loader.load());
            PaymentController pc = loader.getController();
            pc.setOrderId(current.getId());
            pc.setTotalLabelText(lblTotal.getText());  // gọi method thay vì truy cập trực tiếp biến private


            Stage stage = (Stage) paymentButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Thanh toán");
            stage.show();
        } catch (Exception e) {
            err(e);
        }
    }
}

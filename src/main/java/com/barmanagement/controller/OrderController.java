package com.barmanagement.controller;

import com.barmanagement.dao.MenuItemDAO;
import com.barmanagement.dao.OrderDAO;
import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.MenuItem;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.Table;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class OrderController {

    @FXML private ChoiceBox<Table> cbTable;
    @FXML private ChoiceBox<MenuItem> cbMenuItem;
    @FXML private TextField txtQty;

    @FXML private TableView<OrderItem> tblItems;
    @FXML private TableColumn<OrderItem, String> colItem;
    @FXML private TableColumn<OrderItem, Number> colQty;
    @FXML private TableColumn<OrderItem, BigDecimal> colPrice;
    @FXML private TableColumn<OrderItem, BigDecimal> colTotal;

    @FXML private Label lblOrderId;
    @FXML private Label lblTotal;
    @FXML private Button btnCreateOrder;
    @FXML private Button btnAddItem;
    @FXML private Button btnRemoveItem;
    @FXML private Button btnMarkPaid;

    private final TableDAO tableDAO = new TableDAO();
    private final MenuItemDAO menuDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private final ObservableList<OrderItem> data = FXCollections.observableArrayList();
    private final Map<Integer, MenuItem> menuCache = new HashMap<>();
    private Order current;

    @FXML
    public void initialize() {
        try {
            cbTable.setItems(FXCollections.observableArrayList(tableDAO.findAll()));
            var menus = menuDAO.findAllActive();
            cbMenuItem.setItems(FXCollections.observableArrayList(menus));
            menus.forEach(m -> menuCache.put(m.getId(), m));
        } catch (SQLException e) {
            err(e.getMessage());
        }

        txtQty.setText("1");

        // Hiển thị tên món từ cache theo menu_item_id
        colItem.setCellValueFactory(c ->
                new SimpleStringProperty(menuCache.getOrDefault(c.getValue().getMenuItemId(), new MenuItem()).getName()));
        colQty.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantity()));
        colPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrice()));
        colTotal.setCellValueFactory(c ->
                new SimpleObjectProperty<>(c.getValue().getPrice().multiply(BigDecimal.valueOf(c.getValue().getQuantity()))));

        tblItems.setItems(data);

        updateUI(false);
    }

    private void updateUI(boolean hasOrder) {
        btnAddItem.setDisable(!hasOrder);
        btnRemoveItem.setDisable(!hasOrder);
        btnMarkPaid.setDisable(!hasOrder);
    }

    @FXML
    private void onCreateOrder() {
        Table t = cbTable.getValue();
        if (t == null) { err("Chọn bàn trước."); return; }
        try {
            // tạo order trống (total=0) bằng createOrderWithItems(items rỗng)
            Order o = new Order(t.getId(), /*staffId*/ 1);
            int id = orderDAO.createOrderWithItems(o);
            current = orderDAO.findById(id);
            lblOrderId.setText(String.valueOf(id));
            data.setAll(current.getItems());
            lblTotal.setText(current.getTotalAmount().toPlainString());
            updateUI(true);
        } catch (SQLException e) {
            err(e.getMessage());
        }
    }

    @FXML
    private void onAddItem() {
        if (current == null) { err("Chưa có order."); return; }
        MenuItem m = cbMenuItem.getValue();
        if (m == null) { err("Chọn món."); return; }

        int qty;
        try { qty = Integer.parseInt(txtQty.getText().trim()); }
        catch (NumberFormatException ex) { err("Số lượng không hợp lệ."); return; }
        if (qty <= 0) { err("Số lượng phải > 0."); return; }

        try {
            orderDAO.addItem(current.getId(), m.getId(), qty, m.getPrice());
            reloadOrder();
        } catch (SQLException e) {
            err(e.getMessage());
        }
    }

    @FXML
    private void onRemoveItem() {
        OrderItem sel = tblItems.getSelectionModel().getSelectedItem();
        if (sel == null) { err("Chọn dòng cần xoá."); return; }
        try {
            orderDAO.removeItem(sel.getId());
            reloadOrder();
        } catch (SQLException e) {
            err(e.getMessage());
        }
    }

    @FXML
    private void onMarkPaid() {
        if (current == null) return;
        try {
            orderDAO.updateStatus(current.getId(), "paid"); // tự giải phóng bàn trong DAO
            info("Đã thanh toán Order #" + current.getId());
            current = null;
            data.clear();
            lblOrderId.setText("-");
            lblTotal.setText("0.00");
            updateUI(false);
        } catch (SQLException e) {
            err(e.getMessage());
        }
    }

    private void reloadOrder() throws SQLException {
        current = orderDAO.findById(current.getId());
        data.setAll(current.getItems());
        lblTotal.setText(current.getTotalAmount().toPlainString());
    }

    private void err(String m){ new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait(); }
    private void info(String m){ new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait(); }
}

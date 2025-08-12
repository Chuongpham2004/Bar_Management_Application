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
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.sql.SQLException;

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
    private final MenuItemDAO menuItemDAO = new MenuItemDAO();
    private final OrderDAO orderDAO = new OrderDAO();

    private final ObservableList<OrderItem> data = FXCollections.observableArrayList();
    private Order current;

    @FXML
    public void initialize() {
        try {
            cbTable.setItems(FXCollections.observableArrayList(tableDAO.findAll()));
            cbMenuItem.setItems(FXCollections.observableArrayList(menuItemDAO.findActive()));
        } catch (SQLException e) {
            showErr(e.getMessage());
        }

        txtQty.setText("1");

        colItem.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getMenuItem().getName()));
        colQty.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getQuantity()));
        colPrice.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getPrice()));
        colTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getLineTotal()));

        tblItems.setItems(data);

        updateUIState(false);
    }

    private void updateUIState(boolean hasOrder) {
        btnAddItem.setDisable(!hasOrder);
        btnRemoveItem.setDisable(!hasOrder);
        btnMarkPaid.setDisable(!hasOrder);
    }

    @FXML
    private void onCreateOrder() {
        Table t = cbTable.getValue();
        if (t == null) { showErr("Chọn bàn trước."); return; }
        try {
            // giả định staffId = 1 (admin) -> bạn có thể truyền từ màn login
            current = orderDAO.createNewOrder(t.getId(), 1);
            lblOrderId.setText(String.valueOf(current.getId()));
            data.setAll(current.getItems());
            recomputeAndShowTotal();
            // chuyển trạng thái bàn -> occupied
            tableDAO.updateStatus(t.getId(), "occupied");
            updateUIState(true);
        } catch (SQLException e) {
            showErr(e.getMessage());
        }
    }

    @FXML
    private void onAddItem() {
        if (current == null) { showErr("Chưa có order."); return; }
        MenuItem m = cbMenuItem.getValue();
        if (m == null) { showErr("Chọn món."); return; }
        int qty;
        try { qty = Integer.parseInt(txtQty.getText().trim()); }
        catch (NumberFormatException ex) { showErr("Số lượng không hợp lệ."); return; }

        try {
            orderDAO.addItem(current.getId(), m.getId(), qty, m.getPrice());
            reloadOrder();
        } catch (SQLException e) {
            showErr(e.getMessage());
        }
    }

    @FXML
    private void onRemoveItem() {
        OrderItem sel = tblItems.getSelectionModel().getSelectedItem();
        if (sel == null) { showErr("Chọn dòng cần xóa."); return; }
        try {
            orderDAO.removeItem(sel.getId());
            reloadOrder();
        } catch (SQLException e) {
            showErr(e.getMessage());
        }
    }

    @FXML
    private void onMarkPaid() {
        if (current == null) return;
        try {
            orderDAO.markPaid(current.getId());
            // bàn về available lại
            tableDAO.updateStatus(current.getTableId(), "available");
            updateUIState(false);
            showInfo("Đã thanh toán. Order #" + current.getId());
            current = null;
            lblOrderId.setText("-");
            data.clear();
            lblTotal.setText("0.00");
        } catch (SQLException e) {
            showErr(e.getMessage());
        }
    }

    private void reloadOrder() throws SQLException {
        current = orderDAO.findById(current.getId());
        data.setAll(current.getItems());
        recomputeAndShowTotal();
    }

    private void recomputeAndShowTotal() {
        current.recomputeTotal();
        lblTotal.setText(current.getTotalAmount().toPlainString());
    }

    private void showErr(String m) {
        new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait();
    }

    private void showInfo(String m) {
        new Alert(Alert.AlertType.INFORMATION, m, ButtonType.OK).showAndWait();
    }
}

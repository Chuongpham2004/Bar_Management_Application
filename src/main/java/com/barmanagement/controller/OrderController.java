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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderController {

    @FXML private ComboBox<Table> cbTable;
    @FXML private Label selectedTableLabel;
    @FXML private GridPane tableGrid;

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
    private int selectedTableId = -1;

    @FXML
    public void initialize() {
        spQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 99, 1));

        // ===== Menu columns =====
        colMName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        colMCat.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCategory()));
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

        colIPrice.setCellValueFactory(c ->
                new javafx.beans.property.SimpleDoubleProperty(getUnitPrice(c.getValue()))
        );

        colISubtotal.setCellValueFactory(c -> {
            double p = getUnitPrice(c.getValue());
            double sub = p * c.getValue().getQuantity();
            return new javafx.beans.property.SimpleDoubleProperty(sub);
        });

        tblItems.setItems(itemData);

        loadTables();
        loadMenu();
    }

    /** Lấy đơn giá cho 1 item */
    private double getUnitPrice(OrderItem it) {
        double p = it.getPrice();
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
            itemData.setAll(orderDAO.findItems(current.getId()));
            BigDecimal total = orderDAO.calcTotal(current.getId());
            lblTotal.setText(total.toPlainString());
        } catch (SQLException e) { err(e); }
    }

    // ===== Table Selection =====
    @FXML
    private void selectTable(javafx.scene.input.MouseEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        String tableIdStr = (String) source.getUserData();

        if (tableIdStr != null) {
            try {
                int tableId = Integer.parseInt(tableIdStr);
                selectTableById(tableId);
            } catch (NumberFormatException e) {
                info("Lỗi chọn bàn: " + tableIdStr);
            }
        }
    }

    private void selectTableById(int tableId) {
        selectedTableId = tableId;

        // Update label
        if (selectedTableLabel != null) {
            selectedTableLabel.setText("Đã chọn bàn " + tableId);
        }

        // Find and select in ComboBox
        for (Table table : cbTable.getItems()) {
            if (table.getId() == tableId) {
                cbTable.getSelectionModel().select(table);
                break;
            }
        }

        // Update visual selection
        updateTableSelection(tableId);

        info("Đã chọn bàn " + tableId);
    }

    private void updateTableSelection(int tableId) {
        // Simple visual update - change selected table style
        if (tableGrid != null) {
            for (javafx.scene.Node node : tableGrid.getChildren()) {
                if (node instanceof VBox) {
                    String userData = (String) node.getUserData();
                    if (userData != null && userData.equals(String.valueOf(tableId))) {
                        // Selected table - orange color
                        node.setStyle("-fx-background-color: #e16428; -fx-background-radius: 15; -fx-cursor: hand;");
                    } else {
                        // Reset other tables to default colors
                        resetTableStyle((VBox) node, userData);
                    }
                }
            }
        }
    }

    private void resetTableStyle(VBox tableVBox, String tableIdStr) {
        // Default colors based on table type
        if (tableIdStr != null) {
            try {
                int tableId = Integer.parseInt(tableIdStr);
                String color;

                // VIP tables (6, 12) - purple
                if (tableId == 6 || tableId == 12) {
                    color = "#9C27B0";
                }
                // Occupied tables (3, 8) - red
                else if (tableId == 3 || tableId == 8) {
                    color = "#f44336";
                }
                // Reserved tables (5, 11) - orange
                else if (tableId == 5 || tableId == 11) {
                    color = "#FF9800";
                }
                // Available tables - green
                else {
                    color = "#4CAF50";
                }

                tableVBox.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 15; -fx-cursor: hand;");
            } catch (NumberFormatException e) {
                // Default green
                tableVBox.setStyle("-fx-background-color: #4CAF50; -fx-background-radius: 15; -fx-cursor: hand;");
            }
        }
    }

    // ===== Actions =====
    @FXML
    public void newOrder() {
        Table t = cbTable.getSelectionModel().getSelectedItem();
        if (t == null) {
            info("Vui lòng chọn bàn trước!");
            return;
        }
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
                info("Đã tạo order mới #" + id);
            }
        } catch (SQLException e) { err(e); }
    }

    @FXML
    private void goBack() {
        SceneUtil.openScene("/fxml/dashboard.fxml", tblItems);
    }

    @FXML
    private void showPayment() {
        SceneUtil.openScene("/fxml/payment.fxml", tblItems);
    }

    @FXML
    private void showMenu() {
        SceneUtil.openScene("/fxml/menu_management.fxml", tblItems);
    }

    @FXML
    private void showAllOrders() {
        info("Chức năng xem tất cả orders sẽ được phát triển trong phiên bản tới!");
    }

    @FXML
    private void showReports() {
        SceneUtil.openScene("/fxml/revenue_report.fxml", tblItems);
    }

    @FXML
    private void showTableManagement() {
        SceneUtil.openScene("/fxml/table_management.fxml", tblItems);
    }

    @FXML
    public void addItem() {
        if (current == null) {
            info("Hãy tạo order trước khi thêm món!");
            return;
        }
        MenuItem m = tblMenu.getSelectionModel().getSelectedItem();
        if (m == null) {
            info("Vui lòng chọn món từ menu!");
            return;
        }
        try {
            orderDAO.addItem(current.getId(), m.getId(), spQty.getValue());
            reloadItems();
            info("Đã thêm " + m.getName() + " x" + spQty.getValue());
        } catch (SQLException e) { err(e); }
    }

    @FXML
    public void removeItem() {
        OrderItem it = tblItems.getSelectionModel().getSelectedItem();
        if (it == null) {
            info("Vui lòng chọn món cần xóa!");
            return;
        }
        try {
            orderDAO.removeItem(it.getId());
            reloadItems();
            info("Đã xóa món khỏi order!");
        } catch (SQLException e) { err(e); }
    }

    @FXML
    public void completeOrder() {
        if (current == null) {
            info("Không có order nào để hoàn thành!");
            return;
        }
        try {
            orderDAO.complete(current.getId());
            current = null;
            afterComplete();
            info("Đã hoàn thành order và giải phóng bàn!");
        } catch (SQLException e) { err(e); }
    }

    @FXML
    public void reload() {
        afterComplete();
        info("Đã làm mới dữ liệu!");
    }

    private void afterComplete() {
        itemData.clear();
        lblOrderId.setText("(chưa có)");
        lblTotal.setText("0");
        selectedTableId = -1;
        if (selectedTableLabel != null) {
            selectedTableLabel.setText("(Chưa chọn bàn)");
        }
        loadTables();
    }

    private void info(String m) {
        new Alert(Alert.AlertType.INFORMATION, m).showAndWait();
    }

    private void err(Exception e) {
        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        e.printStackTrace();
    }
}
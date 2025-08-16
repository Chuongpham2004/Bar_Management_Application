package com.barmanagement.controller;

import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.Table;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.barmanagement.util.SceneUtil;
import com.barmanagement.util.LogoutUtil;

import java.sql.SQLException;

public class TableManagementController {

    @FXML
    private TableView<Table> tableView;
    @FXML
    private TableColumn<Table, Number> colId;
    @FXML
    private TableColumn<Table, String> colName;
    @FXML
    private TableColumn<Table, String> colStatus;

    @FXML
    private TextField txtName;
    @FXML
    private ComboBox<String> cbStatus;
    @FXML
    private Button btnAdd, btnUpdate, btnDelete, btnEmpty, btnOccupied, btnReserved, btnRefresh;

    // Statistics labels
    @FXML
    private Label lblTotalTables, lblEmptyTables, lblOccupiedTables, lblReservedTables;

    private final TableDAO dao = new TableDAO();
    private final ObservableList<Table> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("empty", "occupied", "reserved");
        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTableName()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        tableView.setItems(data);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, a, b) -> fillForm(b));
        refresh();
    }

    private void fillForm(Table t) {
        if (t == null) return;
        txtName.setText(t.getTableName());
        cbStatus.getSelectionModel().select(t.getStatus());
    }

    @FXML
    private void goBack() {
        SceneUtil.openScene("/fxml/dashboard.fxml", tableView);
    }

    // Navigation methods for sidebar
    @FXML
    private void showPayment() {
        SceneUtil.openScene("/fxml/payment.fxml", tableView);
    }

    @FXML
    private void showMenu() {
        SceneUtil.openScene("/fxml/menu_management.fxml", tableView);
    }

    @FXML
    private void showOrder() {
        SceneUtil.openScene("/fxml/order_management.fxml", tableView);
    }

    // Thêm method xử lý đăng xuất
    @FXML
    private void handleLogout() {
        LogoutUtil.confirmLogout(tableView);
    }

    // Quick actions methods
    @FXML
    private void exportTables() {
        // TODO: Implement export functionality
        showInfo("Chức năng xuất danh sách bàn sẽ được phát triển trong phiên bản tới!");
    }

    @FXML
    public void add(ActionEvent e) {
        try {
            Table t = new Table();
            t.setTableName(txtName.getText().trim());
            t.setStatus(cbStatus.getValue() == null ? "empty" : cbStatus.getValue());
            int id = dao.insert(t);
            t.setId(id);
            data.add(t);
            clearForm();
            updateStatistics();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    @FXML
    public void update(ActionEvent e) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            sel.setTableName(txtName.getText().trim());
            sel.setStatus(cbStatus.getValue());
            dao.update(sel);
            tableView.refresh();
            updateStatistics();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    @FXML
    public void delete(ActionEvent e) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            dao.delete(sel.getId());
            data.remove(sel);
            updateStatistics();
        } catch (Exception ex) {
            showErr(ex);
        }
    }

    @FXML
    public void setEmpty() {
        setStatus("empty");
    }

    @FXML
    public void setOccupied() {
        setStatus("occupied");
    }

    @FXML
    public void setReserved() {
        setStatus("reserved");
    }

    private void setStatus(String st) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            dao.updateStatus(sel.getId(), st);
            sel.setStatus(st);
            tableView.refresh();
            updateStatistics();
        } catch (SQLException e) {
            showErr(e);
        }
    }

    @FXML
    public void refresh() {
        data.clear();
        try {
            data.addAll(dao.findAll());
            updateStatistics();
        } catch (Exception e) {
            showErr(e);
        }
    }

    private void updateStatistics() {
        if (lblTotalTables != null) {
            int total = data.size();
            int empty = (int) data.stream().filter(t -> "empty".equals(t.getStatus())).count();
            int occupied = (int) data.stream().filter(t -> "occupied".equals(t.getStatus())).count();
            int reserved = (int) data.stream().filter(t -> "reserved".equals(t.getStatus())).count();

            lblTotalTables.setText(String.valueOf(total));
            lblEmptyTables.setText(String.valueOf(empty));
            lblOccupiedTables.setText(String.valueOf(occupied));
            lblReservedTables.setText(String.valueOf(reserved));
        }
    }

    private void clearForm() {
        txtName.clear();
        cbStatus.getSelectionModel().clearSelection();
    }

    private void showErr(Exception e) {
        new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        e.printStackTrace();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }
}

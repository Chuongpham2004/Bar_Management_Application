package com.barmanagement.controller;

import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.Table;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import com.barmanagement.util.SceneUtil;

import java.sql.SQLException;

public class TableManagementController {

    @FXML private TableView<Table> tableView;
    @FXML private TableColumn<Table, Number> colId;
    @FXML private TableColumn<Table, String> colName;
    @FXML private TableColumn<Table, String> colStatus;

    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbStatus;
    @FXML private Button btnAdd, btnUpdate, btnDelete, btnEmpty, btnOccupied, btnReserved, btnRefresh;

    private final TableDAO dao = new TableDAO();
    private final ObservableList<Table> data = FXCollections.observableArrayList();


    @FXML
    public void initialize() {
        cbStatus.getItems().addAll("empty","occupied","reserved");
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
        // dùng bất kỳ Node trong scene hiện tại; ở đây dùng tableView
        SceneUtil.openScene("/fxml/dashboard.fxml", tableView);
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
        } catch (Exception ex) { showErr(ex); }
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
        } catch (Exception ex) { showErr(ex); }
    }

    @FXML
    public void delete(ActionEvent e) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try { dao.delete(sel.getId()); data.remove(sel); }
        catch (Exception ex) { showErr(ex); }
    }

    @FXML public void setEmpty()    { setStatus("empty"); }
    @FXML public void setOccupied() { setStatus("occupied"); }
    @FXML public void setReserved() { setStatus("reserved"); }

    private void setStatus(String st) {
        Table sel = tableView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try { dao.updateStatus(sel.getId(), st); sel.setStatus(st); tableView.refresh(); }
        catch (SQLException e) { showErr(e); }
    }

    @FXML public void refresh() {
        data.clear();
        try { data.addAll(dao.findAll()); }
        catch (Exception e) { showErr(e); }
    }

    private void clearForm() { txtName.clear(); cbStatus.getSelectionModel().clearSelection(); }
    private void showErr(Exception e){ new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait(); e.printStackTrace(); }
}

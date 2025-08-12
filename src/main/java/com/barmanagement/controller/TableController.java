package com.barmanagement.controller;

import com.barmanagement.dao.TableDAO;
import com.barmanagement.model.Table;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.sql.SQLException;

public class TableController {

    @FXML private TableView<Table> tblTables;
    @FXML private TableColumn<Table, Number> colId;
    @FXML private TableColumn<Table, Number> colNumber;
    @FXML private TableColumn<Table, Number> colCapacity;
    @FXML private TableColumn<Table, String> colStatus;

    @FXML private TextField txtNumber;
    @FXML private TextField txtCapacity;
    @FXML private ChoiceBox<String> cbStatus;
    @FXML private Button btnSave;
    @FXML private Button btnDelete;

    private final TableDAO tableDAO = new TableDAO();
    private final ObservableList<Table> data = FXCollections.observableArrayList();
    private Table selected;

    @FXML
    public void initialize() {
        cbStatus.setItems(FXCollections.observableArrayList("available","occupied","reserved"));

        colId.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()));
        colNumber.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getTableNumber()));
        colCapacity.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCapacity()));
        colStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));

        tblTables.setItems(data);
        tblTables.setOnMouseClicked(this::onRowClick);

        refresh();
    }

    private void refresh() {
        try {
            data.setAll(tableDAO.findAll());
            clearForm();
        } catch (SQLException e) {
            showErr(e.getMessage());
        }
    }

    private void clearForm() {
        selected = null;
        txtNumber.clear();
        txtCapacity.clear();
        cbStatus.setValue("available");
        tblTables.getSelectionModel().clearSelection();
    }

    private void onRowClick(MouseEvent e) {
        Table t = tblTables.getSelectionModel().getSelectedItem();
        if (t != null) {
            selected = t;
            txtNumber.setText(String.valueOf(t.getTableNumber()));
            txtCapacity.setText(String.valueOf(t.getCapacity()));
            cbStatus.setValue(t.getStatus());
        }
    }

    @FXML
    private void onSave() {
        try {
            int number = Integer.parseInt(txtNumber.getText().trim());
            int capacity = Integer.parseInt(txtCapacity.getText().trim());
            String status = cbStatus.getValue();

            if (selected == null) {
                Table t = new Table(0, number, capacity, status);
                tableDAO.insert(t);
            } else {
                selected.setTableNumber(number);
                selected.setCapacity(capacity);
                selected.setStatus(status);
                tableDAO.update(selected);
            }
            refresh();
        } catch (Exception ex) {
            showErr(ex.getMessage());
        }
    }

    @FXML
    private void onDelete() {
        if (selected == null) { showErr("Chưa chọn bàn."); return; }
        try {
            tableDAO.delete(selected.getId());
            refresh();
        } catch (SQLException e) {
            showErr(e.getMessage());
        }
    }

    private void showErr(String m) {
        new Alert(Alert.AlertType.ERROR, m, ButtonType.OK).showAndWait();
    }
}

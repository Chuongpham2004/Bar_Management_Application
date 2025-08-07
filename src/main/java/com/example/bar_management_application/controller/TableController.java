package com.example.bar_management_application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TableController {

    @FXML
    private TableView<TableData> tableTableView;
    @FXML
    private TableColumn<TableData, String> colTableId;
    @FXML
    private TableColumn<TableData, String> colTableName;
    @FXML
    private TableColumn<TableData, String> colStatus;
    @FXML
    private Button btnAddTable, btnEditTable, btnDeleteTable;

    private final ObservableList<TableData> tableList = FXCollections.observableArrayList();

    public static class TableData {
        public String tableId;
        public String tableName;
        public String status;

        public TableData(String tableId, String tableName, String status) {
            this.tableId = tableId;
            this.tableName = tableName;
            this.status = status;
        }

        public String getTableId() { return tableId; }
        public String getTableName() { return tableName; }
        public String getStatus() { return status; }
    }

    @FXML
    public void initialize() {
        colTableId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTableId()));
        colTableName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTableName()));
        colStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        // Dummy data
        tableList.addAll(
                new TableData("T1", "Table 1", "Available"),
                new TableData("T2", "Table 2", "Occupied"),
                new TableData("T3", "Table 3", "Reserved")
        );

        tableTableView.setItems(tableList);
    }

    @FXML
    private void handleAddTable() {
        System.out.println("Add Table clicked");
    }

    @FXML
    private void handleEditTable() {
        System.out.println("Edit Table clicked");
    }

    @FXML
    private void handleDeleteTable() {
        System.out.println("Delete Table clicked");
    }
}


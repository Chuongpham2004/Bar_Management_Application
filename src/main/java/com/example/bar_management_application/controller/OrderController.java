package com.example.bar_management_application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class OrderController {

    @FXML
    private TableView<OrderData> orderTableView;
    @FXML
    private TableColumn<OrderData, String> colOrderId, colOrderTable, colOrderTotal, colOrderStatus;

    private final ObservableList<OrderData> orderList = FXCollections.observableArrayList();

    public static class OrderData {
        String id, table, total, status;

        public OrderData(String id, String table, String total, String status) {
            this.id = id;
            this.table = table;
            this.total = total;
            this.status = status;
        }

        public String getId() { return id; }
        public String getTable() { return table; }
        public String getTotal() { return total; }
        public String getStatus() { return status; }
    }

    @FXML
    public void initialize() {
        colOrderId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getId()));
        colOrderTable.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTable()));
        colOrderTotal.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTotal()));
        colOrderStatus.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        orderList.addAll(
                new OrderData("O1", "T1", "$50", "Completed"),
                new OrderData("O2", "T2", "$75", "Pending")
        );

        orderTableView.setItems(orderList);
    }
}


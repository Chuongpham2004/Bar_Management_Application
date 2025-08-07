package com.example.bar_management_application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PaymentController {

    @FXML
    private TableView<PaymentData> paymentTableView;
    @FXML
    private TableColumn<PaymentData, String> colPaymentId, colPaymentOrderId, colPaymentAmount, colPaymentMethod;

    private final ObservableList<PaymentData> paymentList = FXCollections.observableArrayList();

    public static class PaymentData {
        String id, orderId, amount, method;
        public PaymentData(String id, String orderId, String amount, String method) {
            this.id = id; this.orderId = orderId; this.amount = amount; this.method = method;
        }
        public String getId() { return id; }
        public String getOrderId() { return orderId; }
        public String getAmount() { return amount; }
        public String getMethod() { return method; }
    }

    @FXML
    public void initialize() {
        colPaymentId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getId()));
        colPaymentOrderId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrderId()));
        colPaymentAmount.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAmount()));
        colPaymentMethod.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMethod()));

        paymentList.addAll(
                new PaymentData("P1", "O1", "$50", "Cash"),
                new PaymentData("P2", "O2", "$75", "Credit Card")
        );

        paymentTableView.setItems(paymentList);
    }
}

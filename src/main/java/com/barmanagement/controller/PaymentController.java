package com.barmanagement.controller;

import com.barmanagement.dao.OrderDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PaymentController {

    @FXML
    private Label totalLabel;

    @FXML
    private ComboBox<String> paymentMethodCombo;

    @FXML
    private Button confirmButton;

    @FXML
    private Button backButton;

    @FXML
    private TextField paidAmountField;

    @FXML
    private Label changeLabel;  // hiển thị tiền thối

    @FXML
    private void initialize() {
        paymentMethodCombo.getItems().addAll("Tiền mặt", "Thẻ", "Chuyển khoản");
        paidAmountField.textProperty().addListener((obs, oldV, newV) -> {
            calculateChange();
        });

    }
    public void setTotalLabelText(String text) {
        totalLabel.setText(text);
    }

    private void calculateChange() {
        try {
            double total = Double.parseDouble(totalLabel.getText());
            double paid = Double.parseDouble(paidAmountField.getText());
            double change = paid - total;
            if (change < 0) {
                changeLabel.setText("Tiền chưa đủ");
                confirmButton.setDisable(true);
            } else {
                changeLabel.setText(String.format("Tiền thối: %.2f", change));
                confirmButton.setDisable(false);
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("");
            confirmButton.setDisable(true);
        }
    }

    private int orderId;
    private OrderDAO orderDAO = new OrderDAO();

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    @FXML
    private void handleConfirmPayment(ActionEvent event) {
        try {
            double total = Double.parseDouble(totalLabel.getText());
            double paid = Double.parseDouble(paidAmountField.getText());
            if (paid < total) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Số tiền thanh toán chưa đủ!");
                alert.showAndWait();
                return;
            }

            String method = paymentMethodCombo.getSelectionModel().getSelectedItem();
            if (method == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Hãy chọn phương thức thanh toán.");
                alert.showAndWait();
                return;
            }

            // Map "Tiền mặt" -> "cash", "Thẻ" -> "card", "Chuyển khoản" -> "transfer"
            String methodCode;
            switch (method) {
                case "Tiền mặt": methodCode = "cash"; break;
                case "Thẻ": methodCode = "card"; break;
                case "Chuyển khoản": methodCode = "transfer"; break;
                default: methodCode = "other"; break;
            }

            orderDAO.complete(orderId, methodCode);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thanh toán");
            alert.setHeaderText(null);
            alert.setContentText("Thanh toán thành công!");
            alert.showAndWait();

            openScene("/fxml/dashboard.fxml", "Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Lỗi trong quá trình thanh toán!");
            alert.showAndWait();
        }
    }


    @FXML
    private void handleBack(ActionEvent event) {
        openScene("/fxml/order_management.fxml", "Order");
    }

    private void openScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

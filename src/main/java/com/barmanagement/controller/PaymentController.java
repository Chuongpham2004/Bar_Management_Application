package com.barmanagement.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.barmanagement.util.SceneUtil;

public class PaymentController {

    @FXML private Label totalLabel;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private Button confirmButton;
    @FXML private Button backButton;

    // New UI elements for enhanced payment interface
    @FXML private Label orderInfoLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label vatLabel;
    @FXML private Label discountLabel;
    @FXML private TextField customerNameField;
    @FXML private TextField customerPhoneField;
    @FXML private TextField discountField;
    @FXML private TextField taxField;
    @FXML private TextArea notesArea;
    @FXML private RadioButton cashRadio;
    @FXML private RadioButton cardRadio;
    @FXML private RadioButton transferRadio;

    @FXML
    private void initialize() {
        paymentMethodCombo.getItems().addAll("Tiền mặt", "Thẻ", "Chuyển khoản");

        // Setup radio button group
        ToggleGroup paymentGroup = new ToggleGroup();
        if (cashRadio != null) cashRadio.setToggleGroup(paymentGroup);
        if (cardRadio != null) cardRadio.setToggleGroup(paymentGroup);
        if (transferRadio != null) transferRadio.setToggleGroup(paymentGroup);

        // Set default selection
        if (cashRadio != null) cashRadio.setSelected(true);

        // Initialize sample data
        updatePaymentSummary();
    }

    // Navigation methods for sidebar
    @FXML
    private void showHome() {
        SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
    }

    @FXML
    private void showDashboard() {
        SceneUtil.openScene("/fxml/dashboard.fxml", totalLabel);
    }

    @FXML
    private void showMenu() {
        SceneUtil.openScene("/fxml/menu_management.fxml", totalLabel);
    }

    @FXML
    private void showOrder() {
        SceneUtil.openScene("/fxml/order_management.fxml", totalLabel);
    }

    @FXML
    private void showTableManagement() {
        SceneUtil.openScene("/fxml/table_management.fxml", totalLabel);
    }

    @FXML
    private void handleLogout() {
        SceneUtil.openScene("/fxml/login.fxml", totalLabel);
    }

    // Payment method selection
    @FXML
    private void selectCashPayment() {
        if (cashRadio != null) cashRadio.setSelected(true);
        paymentMethodCombo.getSelectionModel().select("Tiền mặt");
    }

    @FXML
    private void selectCardPayment() {
        if (cardRadio != null) cardRadio.setSelected(true);
        paymentMethodCombo.getSelectionModel().select("Thẻ");
    }

    @FXML
    private void selectTransferPayment() {
        if (transferRadio != null) transferRadio.setSelected(true);
        paymentMethodCombo.getSelectionModel().select("Chuyển khoản");
    }

    @FXML
    private void handleConfirmPayment(ActionEvent event) {
        String paymentMethod = getSelectedPaymentMethod();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thanh toán");
        alert.setHeaderText(null);
        alert.setContentText("Thanh toán thành công bằng " + paymentMethod + "!");
        alert.showAndWait();

        // Return to dashboard after successful payment
        showDashboard();
    }

    @FXML
    private void handleBack(ActionEvent event) {
        SceneUtil.openScene("/fxml/order_management.fxml", backButton);
    }

    @FXML
    private void printReceipt() {
        // TODO: Implement print receipt functionality
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("In hóa đơn");
        alert.setHeaderText(null);
        alert.setContentText("Chức năng in hóa đơn sẽ được phát triển trong phiên bản tới!");
        alert.showAndWait();
    }

    private String getSelectedPaymentMethod() {
        if (cashRadio != null && cashRadio.isSelected()) return "Tiền mặt";
        if (cardRadio != null && cardRadio.isSelected()) return "Thẻ";
        if (transferRadio != null && transferRadio.isSelected()) return "Chuyển khoản";
        return paymentMethodCombo.getValue() != null ? paymentMethodCombo.getValue() : "Tiền mặt";
    }

    private void updatePaymentSummary() {
        // Sample calculation - replace with actual order data
        double subtotal = 227273.0;
        double vatRate = 0.10;
        double discount = 0.0;

        if (discountField != null && !discountField.getText().isEmpty()) {
            try {
                double discountPercent = Double.parseDouble(discountField.getText()) / 100.0;
                discount = subtotal * discountPercent;
            } catch (NumberFormatException e) {
                discount = 0.0;
            }
        }

        double vat = (subtotal - discount) * vatRate;
        double total = subtotal - discount + vat;

        if (subtotalLabel != null) subtotalLabel.setText(String.format("%.0f VNĐ", subtotal));
        if (vatLabel != null) vatLabel.setText(String.format("%.0f VNĐ", vat));
        if (discountLabel != null) discountLabel.setText(String.format("%.0f VNĐ", discount));
        if (totalLabel != null) totalLabel.setText(String.format("%.0f VNĐ", total));
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
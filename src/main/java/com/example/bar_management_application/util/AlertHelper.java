package com.example.bar_management_application.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.List;

public class AlertHelper {

    // Basic alert types
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setupAlert(alert, "Information", title, message);
        alert.showAndWait();
    }

    public static void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setupAlert(alert, "Success", title, message);
        // Add success styling
        alert.getDialogPane().getStyleClass().add("success-alert");
        alert.showAndWait();
    }

    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        setupAlert(alert, "Warning", title, message);
        alert.showAndWait();
    }

    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setupAlert(alert, "Error", title, message);
        alert.showAndWait();
    }

    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupAlert(alert, "Confirmation", title, message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public static boolean showYesNoConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupAlert(alert, "Confirmation", title, message);

        // Add custom Yes/No buttons
        alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.YES;
    }

    public static Optional<ButtonType> showThreeOptionDialog(String title, String message,
                                                             String option1, String option2, String option3) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        setupAlert(alert, "Choose Option", title, message);

        ButtonType button1 = new ButtonType(option1);
        ButtonType button2 = new ButtonType(option2);
        ButtonType button3 = new ButtonType(option3);

        alert.getButtonTypes().setAll(button1, button2, button3, ButtonType.CANCEL);

        return alert.showAndWait();
    }

    // Exception handling alerts
    public static void showException(String title, String message, Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setupAlert(alert, "Exception", title, message);

        // Create expandable Exception details
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    public static void showDetailedError(String title, String message, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setupAlert(alert, "Error Details", title, message);

        TextArea textArea = new TextArea(details);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    // Input dialogs
    public static Optional<String> showInputDialog(String title, String headerText, String contentText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        setupDialogIcon(dialog);

        return dialog.showAndWait();
    }

    public static Optional<String> showInputDialog(String title, String headerText,
                                                   String contentText, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        setupDialogIcon(dialog);

        return dialog.showAndWait();
    }

    public static Optional<String> showPasswordDialog(String title, String headerText, String contentText) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);

        // Make it a password field
        dialog.getEditor().setStyle("-fx-text-inner-color: transparent;");
        setupDialogIcon(dialog);

        return dialog.showAndWait();
    }

    // Choice dialogs
    public static <T> Optional<T> showChoiceDialog(String title, String headerText,
                                                   String contentText, List<T> choices, T defaultChoice) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        setupDialogIcon(dialog);

        return dialog.showAndWait();
    }

    public static <T> Optional<T> showChoiceDialog(String title, String headerText,
                                                   String contentText, List<T> choices) {
        if (choices.isEmpty()) return Optional.empty();

        ChoiceDialog<T> dialog = new ChoiceDialog<>(choices.get(0), choices);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        setupDialogIcon(dialog);

        return dialog.showAndWait();
    }

    // Validation alerts
    public static void showValidationError(String fieldName, String errorMessage) {
        showError("Validation Error",
                String.format("Invalid %s:\n%s", fieldName, errorMessage));
    }

    public static void showRequiredFieldError(String fieldName) {
        showError("Required Field",
                String.format("%s is required and cannot be empty.", fieldName));
    }

    public static void showFormatError(String fieldName, String expectedFormat) {
        showError("Format Error",
                String.format("%s is not in the correct format.\nExpected format: %s",
                        fieldName, expectedFormat));
    }

    // Business logic alerts
    public static void showOperationSuccess(String operation) {
        showSuccess("Operation Successful",
                String.format("%s completed successfully!", operation));
    }

    public static void showOperationFailed(String operation, String reason) {
        showError("Operation Failed",
                String.format("%s failed:\n%s", operation, reason));
    }

    public static boolean showDeleteConfirmation(String itemType, String itemName) {
        return showConfirmation("Confirm Delete",
                String.format("Are you sure you want to delete %s '%s'?\n\nThis action cannot be undone.",
                        itemType, itemName));
    }

    public static boolean showSaveConfirmation(String itemType) {
        return showConfirmation("Save Changes",
                String.format("Do you want to save changes to %s?", itemType));
    }

    public static boolean showDiscardChanges() {
        return showYesNoConfirmation("Discard Changes",
                "You have unsaved changes. Do you want to discard them?");
    }

    // Loading and progress alerts
    public static Alert showLoadingAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Loading");
        alert.setHeaderText(title);
        alert.setContentText(message + "\n\nPlease wait...");

        // Remove buttons to make it non-closable by user
        alert.getButtonTypes().clear();
        setupDialogIcon(alert);

        return alert; // Return alert so caller can close it when done
    }

    // Network/Connection alerts
    public static void showConnectionError() {
        showError("Connection Error",
                "Unable to connect to the database.\nPlease check your connection and try again.");
    }

    public static void showTimeoutError() {
        showError("Timeout Error",
                "The operation timed out.\nPlease try again later.");
    }

    public static void showDatabaseError(String operation) {
        showError("Database Error",
                String.format("A database error occurred while %s.\nPlease contact support if this continues.",
                        operation));
    }

    // Permission alerts
    public static void showAccessDenied(String resource) {
        showError("Access Denied",
                String.format("You do not have permission to access %s.", resource));
    }

    public static void showInsufficientPermissions(String action) {
        showError("Insufficient Permissions",
                String.format("You do not have sufficient permissions to %s.", action));
    }

    // Custom alerts for Bar Management System
    public static void showTableUnavailable(int tableNumber) {
        showWarning("Table Unavailable",
                String.format("Table %d is currently occupied or reserved.\nPlease select a different table.",
                        tableNumber));
    }

    public static void showOrderCompleted(String orderNumber) {
        showSuccess("Order Completed",
                String.format("Order %s has been completed successfully!", orderNumber));
    }

    public static void showPaymentProcessed(String paymentNumber, double amount) {
        showSuccess("Payment Processed",
                String.format("Payment %s for $%.2f has been processed successfully!",
                        paymentNumber, amount));
    }

    public static boolean showCloseApplicationConfirmation() {
        return showYesNoConfirmation("Exit Application",
                "Are you sure you want to exit the Bar Management System?");
    }

    // Helper methods
    private static void setupAlert(Alert alert, String windowTitle, String headerText, String contentText) {
        alert.setTitle(windowTitle);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        // Set resizable for longer messages
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(400, 200);

        setupDialogIcon(alert);
    }

    private static void setupDialogIcon(Alert alert) {
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("/images/logo.png"));
        } catch (Exception e) {
            // Ignore if icon can't be loaded
        }
    }

    private static void setupDialogIcon(TextInputDialog dialog) {
        try {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("/images/logo.png"));
        } catch (Exception e) {
            // Ignore if icon can't be loaded
        }
    }

    private static <T> void setupDialogIcon(ChoiceDialog<T> dialog) {
        try {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image("/images/logo.png"));
        } catch (Exception e) {
            // Ignore if icon can't be loaded
        }
    }

    // Utility method to show alerts in a non-blocking way
    public static void showNonBlockingInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        setupAlert(alert, "Information", title, message);
        alert.show(); // Non-blocking
    }

    public static void showNonBlockingError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        setupAlert(alert, "Error", title, message);
        alert.show(); // Non-blocking
    }
}
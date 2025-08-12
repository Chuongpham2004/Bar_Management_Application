package com.barmanagement.util;

import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;
import javafx.geometry.Insets;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

/**
 * Alert and Dialog Utilities - Helper methods for showing alerts and dialogs
 */
public class AlertUtil {

    // Default window title
    private static final String DEFAULT_TITLE = "Bar Management System";

    /**
     * Show information alert
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        showInfo("Thông báo", message);
    }

    /**
     * Show warning alert
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showWarning(String message) {
        showWarning("Cảnh báo", message);
    }

    /**
     * Show error alert
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        styleAlert(alert);
        alert.showAndWait();
    }

    public static void showError(String message) {
        showError("Lỗi", message);
    }

    /**
     * Show error with exception details
     */
    public static void showError(String title, String message, Exception ex) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText("Chi tiết lỗi: " + ex.getMessage());

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("Stack trace:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);

        styleAlert(alert);
        alert.showAndWait();
    }

    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Custom button text
        ButtonType yesButton = new ButtonType("Có", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("Không", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    public static boolean showConfirmation(String message) {
        return showConfirmation("Xác nhận", message);
    }

    /**
     * Show confirmation with custom buttons
     */
    public static boolean showConfirmation(String title, String message, String yesText, String noText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType(yesText, ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(noText, ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yesButton, noButton);

        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesButton;
    }

    /**
     * Show three-option dialog
     */
    public static enum TripleChoice { YES, NO, CANCEL }

    public static TripleChoice showTripleChoice(String title, String message,
                                                String yesText, String noText, String cancelText) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        ButtonType yesButton = new ButtonType(yesText, ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType(noText, ButtonBar.ButtonData.NO);
        ButtonType cancelButton = new ButtonType(cancelText, ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

        styleAlert(alert);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == yesButton) return TripleChoice.YES;
            if (result.get() == noButton) return TripleChoice.NO;
        }
        return TripleChoice.CANCEL;
    }

    /**
     * Show input dialog
     */
    public static Optional<String> showInputDialog(String title, String message, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        styleDialog(dialog);
        return dialog.showAndWait();
    }

    public static Optional<String> showInputDialog(String title, String message) {
        return showInputDialog(title, message, "");
    }

    /**
     * Show password input dialog
     */
    public static Optional<String> showPasswordDialog(String title, String message) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);

        ButtonType loginButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        PasswordField password = new PasswordField();
        password.setPromptText("Mật khẩu");

        grid.add(new Label(message), 0, 0);
        grid.add(password, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Focus on password field
        password.requestFocus();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return password.getText();
            }
            return null;
        });

        styleDialog(dialog);
        return dialog.showAndWait();
    }

    /**
     * Show login dialog
     */
    public static Optional<Pair<String, String>> showLoginDialog(String title) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText("Vui lòng đăng nhập");

        ButtonType loginButtonType = new ButtonType("Đăng nhập", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField username = new TextField();
        username.setPromptText("Tên đăng nhập");
        PasswordField password = new PasswordField();
        password.setPromptText("Mật khẩu");

        grid.add(new Label("Tên đăng nhập:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Mật khẩu:"), 0, 1);
        grid.add(password, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // Focus on username field
        username.requestFocus();

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(username.getText(), password.getText());
            }
            return null;
        });

        styleDialog(dialog);
        return dialog.showAndWait();
    }

    /**
     * Show progress dialog (non-blocking)
     */
    public static Alert showProgress(String title, String message) {
        Alert alert = new Alert(AlertType.NONE);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Add progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        grid.add(new Label(message), 0, 0);
        grid.add(progressIndicator, 0, 1);

        alert.getDialogPane().setContent(grid);
        alert.getButtonTypes().clear(); // Remove all buttons

        styleAlert(alert);
        alert.show(); // Non-blocking

        return alert;
    }

    /**
     * Show choice dialog
     */
    public static <T> Optional<T> showChoiceDialog(String title, String message, T defaultChoice, T... choices) {
        ChoiceDialog<T> dialog = new ChoiceDialog<>(defaultChoice, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);

        styleDialog(dialog);
        return dialog.showAndWait();
    }

    /**
     * Show custom dialog with multiple inputs
     */
    public static class MultiInputDialog extends Dialog<ButtonType> {
        private GridPane grid;
        private int currentRow = 0;

        public MultiInputDialog(String title, String headerText) {
            setTitle(title);
            setHeaderText(headerText);

            grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            getDialogPane().setContent(grid);
            getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            styleDialog(this);
        }

        public TextField addTextField(String label, String defaultValue) {
            TextField textField = new TextField(defaultValue);
            textField.setPrefWidth(200);

            grid.add(new Label(label + ":"), 0, currentRow);
            grid.add(textField, 1, currentRow);
            currentRow++;

            return textField;
        }

        public PasswordField addPasswordField(String label) {
            PasswordField passwordField = new PasswordField();
            passwordField.setPrefWidth(200);

            grid.add(new Label(label + ":"), 0, currentRow);
            grid.add(passwordField, 1, currentRow);
            currentRow++;

            return passwordField;
        }

        public ComboBox<String> addComboBox(String label, String... items) {
            ComboBox<String> comboBox = new ComboBox<>();
            comboBox.getItems().addAll(items);
            comboBox.setPrefWidth(200);

            grid.add(new Label(label + ":"), 0, currentRow);
            grid.add(comboBox, 1, currentRow);
            currentRow++;

            return comboBox;
        }

        public CheckBox addCheckBox(String label, boolean defaultValue) {
            CheckBox checkBox = new CheckBox();
            checkBox.setSelected(defaultValue);

            grid.add(new Label(label + ":"), 0, currentRow);
            grid.add(checkBox, 1, currentRow);
            currentRow++;

            return checkBox;
        }
    }

    /**
     * Show success notification
     */
    public static void showSuccess(String message) {
        showInfo("Thành công", "✅ " + message);
    }

    /**
     * Show notification with auto-dismiss
     */
    public static void showNotification(String message, int durationSeconds) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Remove default buttons
        alert.getButtonTypes().clear();

        styleAlert(alert);
        alert.show();

        // Auto-close after duration
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(
                        javafx.util.Duration.seconds(durationSeconds),
                        e -> alert.close()
                )
        );
        timeline.play();
    }

    /**
     * Show loading dialog with cancel option
     */
    public static class LoadingDialog {
        private Alert alert;
        private boolean cancelled = false;

        public LoadingDialog(String title, String message) {
            alert = new Alert(AlertType.NONE);
            alert.setTitle(title);
            alert.setHeaderText(null);

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setPrefSize(50, 50);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            grid.add(new Label(message), 0, 0);
            grid.add(progressIndicator, 0, 1);

            alert.getDialogPane().setContent(grid);

            ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().add(cancelButton);

            alert.setOnCloseRequest(e -> cancelled = true);

            styleAlert(alert);
        }

        public void show() {
            alert.show();
        }

        public void close() {
            alert.close();
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void updateMessage(String message) {
            // Update the message in the dialog
            GridPane grid = (GridPane) alert.getDialogPane().getContent();
            Label messageLabel = (Label) grid.getChildren().get(0);
            messageLabel.setText(message);
        }
    }

    /**
     * Style alert dialogs
     */
    private static void styleAlert(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                AlertUtil.class.getResource("/css/alert-styles.css").toExternalForm()
        );

        // Set icon based on alert type
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        try {
            switch (alert.getAlertType()) {
                case INFORMATION:
                    stage.getIcons().add(new javafx.scene.image.Image("/images/info-icon.png"));
                    break;
                case WARNING:
                    stage.getIcons().add(new javafx.scene.image.Image("/images/warning-icon.png"));
                    break;
                case ERROR:
                    stage.getIcons().add(new javafx.scene.image.Image("/images/error-icon.png"));
                    break;
                case CONFIRMATION:
                    stage.getIcons().add(new javafx.scene.image.Image("/images/question-icon.png"));
                    break;
            }
        } catch (Exception e) {
            // Icons not found, use default
        }
    }

    /**
     * Style regular dialogs
     */
    private static void styleDialog(Dialog<?> dialog) {
        try {
            dialog.getDialogPane().getStylesheets().add(
                    AlertUtil.class.getResource("/css/alert-styles.css").toExternalForm()
            );
        } catch (Exception e) {
            // CSS not found, use default styling
        }
    }

    /**
     * Set parent window for modal dialogs
     */
    public static void setParentWindow(Alert alert, Window parent) {
        if (parent != null) {
            alert.initOwner(parent);
        }
    }

    public static void setParentWindow(Dialog<?> dialog, Window parent) {
        if (parent != null) {
            dialog.initOwner(parent);
        }
    }

    /**
     * Quick method for validation error alerts
     */
    public static void showValidationError(String fieldName, String errorMessage) {
        showError("Lỗi nhập liệu",
                "Lỗi tại trường \"" + fieldName + "\":\n" + errorMessage);
    }

    /**
     * Quick method for operation result alerts
     */
    public static void showOperationResult(boolean success, String operation, String details) {
        if (success) {
            showSuccess(operation + " thành công!" +
                    (details != null ? "\n" + details : ""));
        } else {
            showError("Lỗi " + operation.toLowerCase(),
                    operation + " thất bại!" +
                            (details != null ? "\n" + details : ""));
        }
    }

    /**
     * Show about dialog
     */
    public static void showAbout() {
        showInfo("Về chúng tôi",
                "Bar Management System v1.0\n\n" +
                        "Hệ thống quản lý quầy bar hiện đại\n" +
                        "Phát triển bằng JavaFX\n\n" +
                        "© 2024 - All rights reserved");
    }
}

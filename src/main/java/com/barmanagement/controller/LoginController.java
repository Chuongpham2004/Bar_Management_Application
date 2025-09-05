package com.barmanagement.controller;

import com.barmanagement.dao.JDBCConnect;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button closeButton;
    
    @FXML
    private Button closeButton2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        usernameField.requestFocus();
        setupKeyEvents();
        setupButtonEffects();
        // Ẩn error label ban đầu
        errorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        // Disable login button và thay đổi text
        loginButton.setDisable(true);
        loginButton.setText("Đang đăng nhập...");

        // Ẩn error label khi bắt đầu đăng nhập
        errorLabel.setVisible(false);

        // Thực hiện đăng nhập trong background thread
        new Thread(() -> {
            try {
                if (checkLogin(username, password)) {
                    // Đăng nhập thành công
                    javafx.application.Platform.runLater(() -> {
                        try {
                            openDashboard();
                        } catch (IOException e) {
                            showError("Không thể mở màn hình chính.");
                            resetLoginButton();
                            e.printStackTrace();
                        }
                    });
                } else {
                    // Đăng nhập thất bại
                    javafx.application.Platform.runLater(() -> {
                        showError("Tên đăng nhập hoặc mật khẩu không đúng.");
                        resetLoginButton();
                        passwordField.clear();
                        passwordField.requestFocus();
                    });
                }
            } catch (Exception e) {
                // Lỗi kết nối database
                javafx.application.Platform.runLater(() -> {
                    showError("Lỗi kết nối cơ sở dữ liệu.");
                    resetLoginButton();
                    e.printStackTrace();
                });
            }
        }).start();
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quên mật khẩu");
        alert.setHeaderText(null);
        alert.setContentText("Vui lòng liên hệ quản trị viên để lấy lại mật khẩu.");
        alert.showAndWait();
    }

    @FXML
    private void handleCloseApp(ActionEvent event) {
        // Hiển thị dialog xác nhận trước khi thoát
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thoát");
        alert.setHeaderText(null);
        alert.setContentText("Bạn có chắc chắn muốn thoát ứng dụng?");

        // Tùy chỉnh các button
        ButtonType exitButton = new ButtonType("Thoát", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Hủy", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(exitButton, cancelButton);

        // Hiển thị dialog và xử lý response
        alert.showAndWait().ifPresent(response -> {
            if (response == exitButton) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    private boolean checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                // Verify password với hash đã lưu
                return PasswordUtils.verifyPassword(password, storedHash);
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Database connection error", e);
        }
    }

    // Method này không cần thiết nữa vì đã có PasswordUtils
    // Giữ lại để tương thích với code cũ nếu cần
    @Deprecated
    private String hashPassword(String password) {
        return PasswordUtils.hashPasswordSimple(password);
    }

    private void openDashboard() throws IOException {
        try {
            // Load dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
            Parent root = loader.load();

            // Tạo scene mới
            Scene scene = new Scene(root);

            // Lấy stage hiện tại
            Stage stage = (Stage) loginButton.getScene().getWindow();

            // Set scene mới và cấu hình stage
            stage.setScene(scene);
            stage.setTitle("Bar Management System - Dashboard");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            throw e; // Re-throw để handle ở caller
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void resetLoginButton() {
        loginButton.setDisable(false);
        loginButton.setText("Đăng nhập");
    }

    private void setupKeyEvents() {
        // Enter key và ESC key cho username field
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                handleCloseApp(null);
            }
        });

        // Enter key và ESC key cho password field
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin(null);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                handleCloseApp(null);
            }
        });
    }

    private void setupButtonEffects() {
        // Hiệu ứng hover cho login button
        loginButton.setOnMouseEntered(e -> {
            if (!loginButton.isDisabled()) {
                loginButton.setStyle("-fx-background-color: #2ea043; -fx-text-fill: #f0f6fc; -fx-padding: 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: transparent;");
            }
        });

        loginButton.setOnMouseExited(e -> {
            if (!loginButton.isDisabled()) {
                loginButton.setStyle("-fx-background-color: #238636; -fx-text-fill: #f0f6fc; -fx-padding: 12; -fx-background-radius: 12; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: transparent;");
            }
        });

        // Hiệu ứng hover cho close button
        closeButton.setOnMouseEntered(e -> {
            closeButton.setStyle("-fx-background-color: #ff3742; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 20px; -fx-cursor: hand; -fx-border-color: #ff2f3a; -fx-border-width: 2; -fx-border-radius: 20; -fx-scale-x: 1.1; -fx-scale-y: 1.1;");
        });

        closeButton.setOnMouseExited(e -> {
            closeButton.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; -fx-background-radius: 20; -fx-font-weight: bold; -fx-font-size: 20px; -fx-cursor: hand; -fx-border-color: #ff3742; -fx-border-width: 2; -fx-border-radius: 20; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
        });
        
        // Hiệu ứng pressed cho close button
        closeButton.setOnMousePressed(e -> {
            closeButton.setStyle("-fx-background-color: #ff2f3a; -fx-text-fill: white; -fx-background-radius: 25; -fx-font-weight: bold; -fx-font-size: 24px; -fx-cursor: hand; -fx-border-color: #ff1e2a; -fx-border-width: 3; -fx-border-radius: 25; -fx-scale-x: 0.95; -fx-scale-y: 0.95;");
        });
        
        // Hiệu ứng cho close button 2 (nếu có)
        if (closeButton2 != null) {
            closeButton2.setOnMouseEntered(e -> {
                closeButton2.setStyle("-fx-background-color: #ff3742; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #ff2f3a; -fx-border-width: 2; -fx-border-radius: 10; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
            });

            closeButton2.setOnMouseExited(e -> {
                closeButton2.setStyle("-fx-background-color: #ff4757; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand; -fx-border-color: #ff3742; -fx-border-width: 2; -fx-border-radius: 10; -fx-scale-x: 1.0; -fx-scale-y: 1.0;");
            });
        }
    }
}
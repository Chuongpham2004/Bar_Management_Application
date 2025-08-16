package com.barmanagement.controller;

import com.barmanagement.dao.JDBCConnect;
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
        // Enter key trong username field sẽ focus vào password
        usernameField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passwordField.requestFocus();
            }
        });

        // Enter key trong password field sẽ thực hiện login
        passwordField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                handleLogin(null);
            }
        });
    }

    private void setupButtonEffects() {
        // Hiệu ứng hover cho login button
        loginButton.setOnMouseEntered(e -> {
            if (!loginButton.isDisabled()) {
                loginButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
        });

        loginButton.setOnMouseExited(e -> {
            if (!loginButton.isDisabled()) {
                loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 12; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
        });
    }
}
package com.barmanagement.controller;

import com.barmanagement.dao.JDBCConnect;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ResourceBundle;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    public void initialize(URL location, ResourceBundle resources) {
        usernameField.requestFocus();
        setupKeyEvents();
        setupButtonEffects();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập đầy đủ thông tin.");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Đang đăng nhập...");

        new Thread(() -> {
            try {
                System.out.println("Checking login for user: " + username);
                System.out.println("Input password: " + password); // Log password nhập vào
                if (checkLogin(username, password)) {
                    System.out.println("Login successful, opening dashboard");
                    javafx.application.Platform.runLater(() -> {
                        try {
                            openDashboard();
                        } catch (IOException e) {
                            System.err.println("Failed to open dashboard: " + e.getMessage());
                            e.printStackTrace();
                            showError("Không thể mở màn hình chính.");
                            resetLoginButton();
                        }
                    });
                } else {
                    System.out.println("Login failed: Invalid credentials");
                    javafx.application.Platform.runLater(() -> {
                        showError("Tên đăng nhập hoặc mật khẩu không đúng.");
                        resetLoginButton();
                        passwordField.clear();
                        passwordField.requestFocus();
                    });
                }
            } catch (Exception e) {
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    showError("Lỗi kết nối cơ sở dữ liệu.");
                    resetLoginButton();
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
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            // Chọn một trong hai tùy chọn dưới đây:
            // Tùy chọn 1: Nếu mật khẩu trong DB là plaintext
            stmt.setString(2, password); // So sánh trực tiếp
            System.out.println("Querying with username: " + username + ", password: " + password);

            // Tùy chọn 2: Nếu mật khẩu trong DB là hash MD5
            // String hashedPassword = hashPassword(password);
            // stmt.setString(2, hashedPassword);
            // System.out.println("Querying with username: " + username + ", hashed password: " + hashedPassword);

            ResultSet rs = stmt.executeQuery();
            boolean result = rs.next();
            System.out.println("Login check result: " + result);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            showError("Lỗi kết nối cơ sở dữ liệu.");
            return false;
        }
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] array = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback nếu MD5 không hoạt động
        }
    }

    private void openDashboard() throws IOException {
        URL fxmlLocation = getClass().getResource("/fxml/dashboard.fxml");
        if (fxmlLocation == null) {
            throw new IOException("Không tìm thấy file dashboard.fxml tại /fxml/dashboard.fxml");
        }
        try {
            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Bar Management System - Dashboard");
            stage.centerOnScreen();
            stage.show();
            System.out.println("Dashboard loaded successfully");
        } catch (Exception e) {
            System.err.println("Failed to open dashboard: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Lỗi khi tải dashboard: " + e.getMessage());
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
                loginButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: #3620cf; -fx-padding: 12; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
        });

        loginButton.setOnMouseExited(e -> {
            if (!loginButton.isDisabled()) {
                loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: #3620cf; -fx-padding: 12; -fx-background-radius: 5; -fx-font-weight: bold; -fx-font-size: 14px;");
            }
        });
    }

    public static void main(String[] args) {
        // Không cần thiết trong controller, để trống
    }
}
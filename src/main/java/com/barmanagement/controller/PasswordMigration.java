package com.barmanagement.controller;

import com.barmanagement.dao.JDBCConnect;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordMigration {
    public static void migrateAllPasswords() {
        String selectSql = "SELECT id, password FROM users";
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            ResultSet rs = selectStmt.executeQuery();
            int count = 0;

            while (rs.next()) {
                int id = rs.getInt("id");
                String plainPassword = rs.getString("password");

                // Kiểm tra xem password đã được hash chưa
                if (!isPasswordHashed(plainPassword)) {
                    String hashedPassword = PasswordUtils.hashPassword(plainPassword);

                    updateStmt.setString(1, hashedPassword);
                    updateStmt.setInt(2, id);
                    updateStmt.executeUpdate();

                    count++;
                    System.out.println("Updated password for user ID: " + id);
                }
            }

            System.out.println("Migration completed. Updated " + count + " passwords.");

        } catch (SQLException e) {
            System.err.println("Error during password migration:");
            e.printStackTrace();
        }
    }

    /**
     * Kiểm tra xem password đã được hash chưa
     * Password đã hash sẽ có length > 50 và chứa Base64 characters
     */
    private static boolean isPasswordHashed(String password) {
        if (password == null || password.length() < 50) {
            return false;
        }

        // Kiểm tra xem có phải Base64 encoded không
        try {
            java.util.Base64.getDecoder().decode(password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Method để tạo user mới với password đã hash
     */
    public static boolean createUserWithHashedPassword(String username, String plainPassword,
                                                       String fullName, String role) {
        String sql = "INSERT INTO users (username, password, full_name, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, PasswordUtils.hashPassword(plainPassword));
            stmt.setString(3, fullName);
            stmt.setString(4, role);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Main method để chạy migration (chỉ chạy một lần)
     */
    public static void main(String[] args) {
        System.out.println("Starting password migration...");
        migrateAllPasswords();

        // Test tạo user mới
        System.out.println("\nCreating test users...");
        createUserWithHashedPassword("admin", "admin123", "Administrator", "admin");
        createUserWithHashedPassword("user1", "password123", "User One", "user");

        System.out.println("Migration and test user creation completed!");
    }
}
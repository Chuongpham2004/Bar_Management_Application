package com.barmanagement.util;

import com.barmanagement.config.JDBCConfig;
import com.barmanagement.controller.PasswordUtils;

import java.sql.*;
import java.util.Scanner;

/**
 * Tool to automatically migrate all plain text passwords to hashed passwords
 * Run this ONCE to convert existing plain text passwords to hashed format
 */
public class PasswordMigrationTool {

    public static void main(String[] args) {
        System.out.println("=== PASSWORD MIGRATION TOOL ===");
        System.out.println("This tool will hash all plain text passwords in the database.");
        System.out.println("WARNING: This operation cannot be undone!");

        // Confirm before proceeding
        Scanner scanner = new Scanner(System.in);
        System.out.print("Do you want to continue? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes") && !confirm.equals("y")) {
            System.out.println("Operation cancelled.");
            return;
        }

        try {
            migratePasswords();
            System.out.println("✅ Password migration completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error during migration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Main migration logic
     */
    private static void migratePasswords() throws SQLException {
        Connection conn = null;
        try {
            // Connect to database
            conn = DriverManager.getConnection(
                    JDBCConfig.CONNECTION_URL,
                    JDBCConfig.USERNAME,
                    JDBCConfig.PASSWORD
            );

            System.out.println("🔌 Connected to database successfully");

            // Step 1: Check if passwords are already hashed
            if (arePasswordsAlreadyHashed(conn)) {
                System.out.println("ℹ️ Passwords appear to already be hashed. Migration not needed.");
                return;
            }

            // Step 2: Get all users with plain text passwords
            String selectSQL = "SELECT id, username, password FROM users WHERE password IS NOT NULL AND password != ''";

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL);
                 ResultSet rs = selectStmt.executeQuery()) {

                int totalUsers = 0;
                int successCount = 0;
                int errorCount = 0;

                System.out.println("🔄 Starting password migration...");

                // Step 3: Process each user
                while (rs.next()) {
                    totalUsers++;
                    int userId = rs.getInt("id");
                    String username = rs.getString("username");
                    String plainPassword = rs.getString("password");

                    try {
                        // Hash the plain text password
                        String hashedPassword = PasswordUtils.hashPassword(plainPassword);

                        if (hashedPassword != null) {
                            // Update the password in database
                            updateUserPassword(conn, userId, hashedPassword);
                            successCount++;
                            System.out.println("✅ User '" + username + "' (ID: " + userId + ") - Password hashed successfully");
                        } else {
                            errorCount++;
                            System.err.println("❌ User '" + username + "' (ID: " + userId + ") - Failed to hash password");
                        }

                    } catch (Exception e) {
                        errorCount++;
                        System.err.println("❌ User '" + username + "' (ID: " + userId + ") - Error: " + e.getMessage());
                    }
                }

                // Step 4: Show results
                System.out.println("\n=== MIGRATION RESULTS ===");
                System.out.println("Total users processed: " + totalUsers);
                System.out.println("Successfully migrated: " + successCount);
                System.out.println("Errors: " + errorCount);

                if (errorCount > 0) {
                    System.out.println("⚠️ Some users had errors. Please check the logs above.");
                }
            }

        } finally {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("🔌 Database connection closed");
            }
        }
    }

    /**
     * Check if passwords are already hashed by examining their format
     */
    private static boolean arePasswordsAlreadyHashed(Connection conn) throws SQLException {
        String sql = "SELECT password FROM users WHERE password IS NOT NULL AND password != '' LIMIT 5";

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String password = rs.getString("password");

                // Check if password looks like Base64 encoded hash
                // Hashed passwords should be much longer (typically 60+ characters)
                if (password.length() > 50 && isValidBase64(password)) {
                    return true; // At least one password is already hashed
                }
            }
        }

        return false; // Passwords appear to be plain text
    }

    /**
     * Update a user's password in the database
     */
    private static void updateUserPassword(Connection conn, int userId, String hashedPassword) throws SQLException {
        String updateSQL = "UPDATE users SET password = ? WHERE id = ?";

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
            updateStmt.setString(1, hashedPassword);
            updateStmt.setInt(2, userId);

            int rowsAffected = updateStmt.executeUpdate();

            if (rowsAffected != 1) {
                throw new SQLException("Expected to update 1 row, but updated " + rowsAffected + " rows");
            }
        }
    }

    /**
     * Simple check to see if a string could be valid Base64
     */
    private static boolean isValidBase64(String str) {
        try {
            java.util.Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Test method to verify a specific user's password after migration
     */
    public static void testUserPassword(String username, String plainPassword) {
        try (Connection conn = DriverManager.getConnection(
                JDBCConfig.CONNECTION_URL,
                JDBCConfig.USERNAME,
                JDBCConfig.PASSWORD)) {

            String sql = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String storedHash = rs.getString("password");
                        boolean isValid = PasswordUtils.verifyPassword(plainPassword, storedHash);

                        System.out.println("Testing user: " + username);
                        System.out.println("Password verification: " + (isValid ? "✅ SUCCESS" : "❌ FAILED"));
                        System.out.println("Stored hash length: " + storedHash.length());
                    } else {
                        System.out.println("User '" + username + "' not found");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error testing password: " + e.getMessage());
        }
    }
}

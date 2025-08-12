package com.barmanagement.util;

import com.barmanagement.config.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Database Utilities - Common database operations and helpers
 */
public class DatabaseUtil {

    /**
     * Execute a simple SELECT query and return results as List of Maps
     */
    public static List<Map<String, Object>> executeQuery(String sql, Object... params) {
        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnLabel(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Query execution error: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    /**
     * Execute INSERT, UPDATE, DELETE queries
     */
    public static int executeUpdate(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            return stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("❌ Update execution error: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Execute INSERT and return generated key
     */
    public static Long executeInsertWithGeneratedKey(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Insert with key error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Check if a record exists
     */
    public static boolean recordExists(String tableName, String whereClause, Object... params) {
        String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE " + whereClause;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Record existence check error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get single value from database
     */
    public static Object getSingleValue(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameters
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getObject(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Single value query error: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get count of records
     */
    public static int getRecordCount(String tableName, String whereClause, Object... params) {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        Object result = getSingleValue(sql, params);
        return result != null ? ((Number) result).intValue() : 0;
    }

    /**
     * Transaction helper - Execute multiple statements in transaction
     */
    public static boolean executeTransaction(List<TransactionStatement> statements) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            for (TransactionStatement statement : statements) {
                try (PreparedStatement stmt = conn.prepareStatement(statement.getSql())) {
                    Object[] params = statement.getParams();
                    for (int i = 0; i < params.length; i++) {
                        stmt.setObject(i + 1, params[i]);
                    }
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            System.out.println("✅ Transaction completed successfully");
            return true;

        } catch (SQLException e) {
            System.err.println("❌ Transaction failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("🔄 Transaction rolled back");
                } catch (SQLException rollbackEx) {
                    System.err.println("❌ Rollback failed: " + rollbackEx.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("⚠️ Error resetting auto-commit: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Convert Timestamp to LocalDateTime safely
     */
    public static LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    /**
     * Convert LocalDateTime to Timestamp safely
     */
    public static Timestamp localDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? Timestamp.valueOf(dateTime) : null;
    }

    /**
     * Create dynamic WHERE clause from Map of conditions
     */
    public static String buildWhereClause(Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return "";
        }

        StringBuilder whereClause = new StringBuilder();
        boolean first = true;

        for (String key : conditions.keySet()) {
            if (!first) {
                whereClause.append(" AND ");
            }
            whereClause.append(key).append(" = ?");
            first = false;
        }

        return whereClause.toString();
    }

    /**
     * Get parameters array from Map of conditions
     */
    public static Object[] getParametersFromMap(Map<String, Object> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return new Object[0];
        }
        return conditions.values().toArray();
    }

    /**
     * Check database connection health
     */
    public static boolean isDatabaseHealthy() {
        try {
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            return dbConn.testConnection();
        } catch (Exception e) {
            System.err.println("❌ Database health check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get database statistics
     */
    public static Map<String, Object> getDatabaseStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Get table counts
            stats.put("staff_count", getRecordCount("staff", "status = TRUE"));
            stats.put("tables_count", getRecordCount("tables", null));
            stats.put("menu_items_count", getRecordCount("menu_item", "status = TRUE"));
            stats.put("today_orders_count", getRecordCount("orders", "DATE(created_at) = CURDATE()"));

            // Get today's revenue
            Object todayRevenue = getSingleValue(
                    "SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE DATE(created_at) = CURDATE() AND status = 'paid'"
            );
            stats.put("today_revenue", todayRevenue != null ? todayRevenue : 0);

            // Get active tables
            stats.put("active_tables_count", getRecordCount("tables", "status = 'occupied'"));

        } catch (Exception e) {
            System.err.println("❌ Error getting database stats: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Escape SQL LIKE pattern
     */
    public static String escapeLikePattern(String pattern) {
        if (pattern == null) return null;
        return pattern.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /**
     * Build LIKE search condition
     */
    public static String buildLikeSearch(String columnName, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return "1=1"; // Always true condition
        }

        String escapedTerm = escapeLikePattern(searchTerm.trim());
        return columnName + " LIKE '%" + escapedTerm + "%'";
    }

    /**
     * Inner class for transaction statements
     */
    public static class TransactionStatement {
        private String sql;
        private Object[] params;

        public TransactionStatement(String sql, Object... params) {
            this.sql = sql;
            this.params = params;
        }

        public String getSql() {
            return sql;
        }

        public Object[] getParams() {
            return params;
        }
    }
}

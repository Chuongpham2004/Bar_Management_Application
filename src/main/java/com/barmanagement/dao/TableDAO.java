package com.barmanagement.dao;

import com.barmanagement.config.DatabaseConnection;
import com.barmanagement.model.Table;
import com.barmanagement.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Table Data Access Object
 * Handles all database operations related to tables
 */
public class TableDAO {

    private DatabaseConnection dbConnection;

    public TableDAO() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    /**
     * Get all tables
     */
    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables ORDER BY table_number";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Table table = mapResultSetToTable(rs);
                tables.add(table);
            }

            System.out.println("📋 Loaded " + tables.size() + " tables from database");

        } catch (SQLException e) {
            System.err.println("❌ Error getting all tables: " + e.getMessage());
            e.printStackTrace();
        }

        return tables;
    }

    /**
     * Get table by ID
     */
    public Table getTableById(int id) {
        String sql = "SELECT * FROM tables WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTable(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting table by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get table by table number
     */
    public Table getTableByNumber(int tableNumber) {
        String sql = "SELECT * FROM tables WHERE table_number = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTable(rs);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting table by number: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get tables by status
     */
    public List<Table> getTablesByStatus(String status) {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE status = ? ORDER BY table_number";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tables.add(mapResultSetToTable(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting tables by status: " + e.getMessage());
        }

        return tables;
    }

    /**
     * Get available tables
     */
    public List<Table> getAvailableTables() {
        return getTablesByStatus("available");
    }

    /**
     * Get occupied tables
     */
    public List<Table> getOccupiedTables() {
        return getTablesByStatus("occupied");
    }

    /**
     * Add new table
     */
    public boolean addTable(Table table) {
        String sql = "INSERT INTO tables (table_number, capacity, status, location, position_x, position_y, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, parseTableNumber(table.getTableNumber()));
            stmt.setInt(2, table.getCapacity());
            stmt.setString(3, table.getStatus());
            stmt.setString(4, table.getLocation());
            stmt.setDouble(5, table.getPositionX());
            stmt.setDouble(6, table.getPositionY());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        table.setId(generatedKeys.getInt(1));
                    }
                }
                System.out.println("✅ Table added successfully: " + table.getTableNumber());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error adding table: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update table information
     */
    public boolean updateTable(Table table) {
        String sql = "UPDATE tables SET table_number = ?, capacity = ?, status = ?, location = ?, " +
                "position_x = ?, position_y = ?, occupied_since = ?, reservation_time = ?, " +
                "reserved_by = ?, current_order_id = ?, current_bill = ?, guest_count = ?, " +
                "notes = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, parseTableNumber(table.getTableNumber()));
            stmt.setInt(2, table.getCapacity());
            stmt.setString(3, table.getStatus());
            stmt.setString(4, table.getLocation());
            stmt.setDouble(5, table.getPositionX());
            stmt.setDouble(6, table.getPositionY());
            stmt.setTimestamp(7, table.getOccupiedSince() != null ?
                    Timestamp.valueOf(table.getOccupiedSince()) : null);
            stmt.setTimestamp(8, table.getReservationTime() != null ?
                    Timestamp.valueOf(table.getReservationTime()) : null);
            stmt.setString(9, table.getReservedBy());
            stmt.setInt(10, table.getCurrentOrderId());
            stmt.setDouble(11, table.getCurrentBill());
            stmt.setInt(12, table.getGuestCount());
            stmt.setString(13, table.getNotes());
            stmt.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(15, table.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Table updated successfully: " + table.getTableNumber());
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating table: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Update table status only
     */
    public boolean updateTableStatus(int tableId, String status) {
        String sql = "UPDATE tables SET status = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, tableId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Table status updated: ID " + tableId + " → " + status);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating table status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Occupy table
     */
    public boolean occupyTable(int tableId, int guestCount, String notes) {
        String sql = "UPDATE tables SET status = 'occupied', occupied_since = ?, " +
                "guest_count = ?, notes = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();
            stmt.setTimestamp(1, Timestamp.valueOf(now));
            stmt.setInt(2, guestCount);
            stmt.setString(3, notes);
            stmt.setTimestamp(4, Timestamp.valueOf(now));
            stmt.setInt(5, tableId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Table occupied: ID " + tableId + " with " + guestCount + " guests");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error occupying table: " + e.getMessage());
        }

        return false;
    }

    /**
     * Reserve table
     */
    public boolean reserveTable(int tableId, String reservedBy, LocalDateTime reservationTime, String notes) {
        String sql = "UPDATE tables SET status = 'reserved', reserved_by = ?, " +
                "reservation_time = ?, notes = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, reservedBy);
            stmt.setTimestamp(2, Timestamp.valueOf(reservationTime));
            stmt.setString(3, notes);
            stmt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(5, tableId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Table reserved: ID " + tableId + " by " + reservedBy);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error reserving table: " + e.getMessage());
        }

        return false;
    }

    /**
     * Free table (make available)
     */
    public boolean freeTable(int tableId) {
        String sql = "UPDATE tables SET status = 'available', occupied_since = NULL, " +
                "reservation_time = NULL, reserved_by = NULL, current_order_id = 0, " +
                "current_bill = 0, guest_count = 0, notes = NULL, updated_at = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, tableId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Table freed: ID " + tableId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error freeing table: " + e.getMessage());
        }

        return false;
    }

    /**
     * Delete table (soft delete by setting active = false)
     */
    public boolean deleteTable(int tableId) {
        String sql = "UPDATE tables SET is_active = false, updated_at = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(2, tableId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Table deleted (soft): ID " + tableId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting table: " + e.getMessage());
        }

        return false;
    }

    /**
     * Check if table number exists
     */
    public boolean tableNumberExists(int tableNumber) {
        String sql = "SELECT COUNT(*) FROM tables WHERE table_number = ? AND is_active = true";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableNumber);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error checking table number existence: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get table statistics
     */
    public TableStatistics getTableStatistics() {
        TableStatistics stats = new TableStatistics();

        String sql = "SELECT status, COUNT(*) as count FROM tables WHERE is_active = true GROUP BY status";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");

                switch (status) {
                    case "available":
                        stats.availableCount = count;
                        break;
                    case "occupied":
                        stats.occupiedCount = count;
                        break;
                    case "reserved":
                        stats.reservedCount = count;
                        break;
                    case "cleaning":
                        stats.cleaningCount = count;
                        break;
                }
            }

            stats.totalCount = stats.availableCount + stats.occupiedCount +
                    stats.reservedCount + stats.cleaningCount;

        } catch (SQLException e) {
            System.err.println("❌ Error getting table statistics: " + e.getMessage());
        }

        return stats;
    }

    /**
     * Get tables that have been occupied for too long
     */
    public List<Table> getOverdueTables(int hoursThreshold) {
        List<Table> overdueTables = new ArrayList<>();
        String sql = "SELECT * FROM tables WHERE status = 'occupied' AND " +
                "occupied_since < DATE_SUB(NOW(), INTERVAL ? HOUR)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, hoursThreshold);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    overdueTables.add(mapResultSetToTable(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error getting overdue tables: " + e.getMessage());
        }

        return overdueTables;
    }

    /**
     * Update table's current order and bill
     */
    public boolean updateTableOrder(int tableId, int orderId, double billAmount) {
        String sql = "UPDATE tables SET current_order_id = ?, current_bill = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            stmt.setDouble(2, billAmount);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(4, tableId);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("✅ Table order updated: ID " + tableId + ", Order: " + orderId);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating table order: " + e.getMessage());
        }

        return false;
    }

    /**
     * Map ResultSet to Table object
     */
    private Table mapResultSetToTable(ResultSet rs) throws SQLException {
        Table table = new Table();

        table.setId(rs.getInt("id"));
        table.setTableNumber(String.valueOf(rs.getInt("table_number")));
        table.setCapacity(rs.getInt("capacity"));
        table.setStatus(rs.getString("status"));
        table.setPositionX(rs.getDouble("position_x"));
        table.setPositionY(rs.getDouble("position_y"));
        table.setCurrentOrderId(rs.getInt("current_order_id"));
        table.setCurrentBill(rs.getDouble("current_bill"));
        table.setGuestCount(rs.getInt("guest_count"));
        table.setNotes(rs.getString("notes"));
        table.setActive(rs.getBoolean("is_active"));

        // Handle optional fields
        String location = rs.getString("location");
        if (location != null) {
            table.setLocation(location);
        }

        String reservedBy = rs.getString("reserved_by");
        if (reservedBy != null) {
            table.setReservedBy(reservedBy);
        }

        // Handle timestamps
        Timestamp occupiedSince = rs.getTimestamp("occupied_since");
        if (occupiedSince != null) {
            table.setOccupiedSince(occupiedSince.toLocalDateTime());
        }

        Timestamp reservationTime = rs.getTimestamp("reservation_time");
        if (reservationTime != null) {
            table.setReservationTime(reservationTime.toLocalDateTime());
        }

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            table.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            table.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return table;
    }

    /**
     * Parse table number from string (handle formats like "B01", "A5", "12")
     */
    private int parseTableNumber(String tableNumber) {
        if (tableNumber == null || tableNumber.trim().isEmpty()) {
            return 0;
        }

        // Remove non-numeric characters and parse
        String numericPart = tableNumber.replaceAll("[^0-9]", "");
        if (numericPart.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(numericPart);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Table Statistics inner class
     */
    public static class TableStatistics {
        public int totalCount = 0;
        public int availableCount = 0;
        public int occupiedCount = 0;
        public int reservedCount = 0;
        public int cleaningCount = 0;

        public double getOccupancyRate() {
            if (totalCount == 0) return 0.0;
            return (double) occupiedCount / totalCount * 100.0;
        }

        public double getAvailabilityRate() {
            if (totalCount == 0) return 0.0;
            return (double) availableCount / totalCount * 100.0;
        }

        @Override
        public String toString() {
            return String.format(
                    "Tables: Total=%d, Available=%d, Occupied=%d, Reserved=%d, Cleaning=%d (%.1f%% occupancy)",
                    totalCount, availableCount, occupiedCount, reservedCount, cleaningCount, getOccupancyRate()
            );
        }
    }
}
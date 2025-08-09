package com.barmanagement.dao;

import com.barmanagement.model.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    private Connection connection;

    public TableDAO(Connection connection) {
        this.connection = connection;
    }

    // Lấy toàn bộ bàn
    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT id, name, status FROM tables";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Table table = new Table(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("status")
                );
                tables.add(table);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tables;
    }

    // Cập nhật trạng thái bàn
    public boolean updateTableStatus(int tableId, String newStatus) {
        String sql = "UPDATE tables SET status = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, tableId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Tìm bàn theo ID
    public Table getTableById(int tableId) {
        String sql = "SELECT id, name, status FROM tables WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Table(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}


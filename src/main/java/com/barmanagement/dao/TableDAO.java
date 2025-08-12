package com.barmanagement.dao;

import com.barmanagement.model.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {
    
    public List<Table> getAllTables() {
        List<Table> tables = new ArrayList<>();
        String sql = "SELECT * FROM tables ORDER BY table_number";
        
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Table table = new Table(
                    rs.getInt("id"),
                    rs.getString("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status")
                );
                table.setTotalAmount(rs.getDouble("total_amount"));
                tables.add(table);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return tables;
    }
    
    public Table getTableById(int id) {
        String sql = "SELECT * FROM tables WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Table table = new Table(
                    rs.getInt("id"),
                    rs.getString("table_number"),
                    rs.getInt("capacity"),
                    rs.getString("status")
                );
                table.setTotalAmount(rs.getDouble("total_amount"));
                return table;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean updateTableStatus(int tableId, String status) {
        String sql = "UPDATE tables SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, tableId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean updateTableTotalAmount(int tableId, double totalAmount) {
        String sql = "UPDATE tables SET total_amount = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, totalAmount);
            pstmt.setInt(2, tableId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
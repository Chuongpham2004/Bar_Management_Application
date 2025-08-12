package com.barmanagement.dao;

import com.barmanagement.config.DatabaseConnection;
import com.barmanagement.model.Table;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    public List<Table> findAll() throws SQLException {
        String sql = "SELECT id, table_number, capacity, status FROM tables ORDER BY table_number";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Table> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    public Table findById(int id) throws SQLException {
        String sql = "SELECT id, table_number, capacity, status FROM tables WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Table insert(Table t) throws SQLException {
        String sql = "INSERT INTO tables (table_number, capacity, status) VALUES (?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getTableNumber());
            ps.setInt(2, t.getCapacity());
            ps.setString(3, t.getStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.setId(keys.getInt(1));
            }
            return t;
        }
    }

    public boolean update(Table t) throws SQLException {
        String sql = "UPDATE tables SET table_number=?, capacity=?, status=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, t.getTableNumber());
            ps.setInt(2, t.getCapacity());
            ps.setString(3, t.getStatus());
            ps.setInt(4, t.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE tables SET status=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM tables WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private Table map(ResultSet rs) throws SQLException {
        return new Table(
                rs.getInt("id"),
                rs.getInt("table_number"),
                rs.getInt("capacity"),
                rs.getString("status")
        );
    }
}

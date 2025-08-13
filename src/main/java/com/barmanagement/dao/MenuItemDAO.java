package com.barmanagement.dao;

import com.barmanagement.config.DatabaseConnection;
import com.barmanagement.model.MenuItem;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {

    private MenuItem map(ResultSet rs) throws SQLException {
        MenuItem m = new MenuItem(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getBigDecimal("price"),
                rs.getBoolean("status"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
        return m;
    }

    public List<MenuItem> findAllActive() throws SQLException {
        String sql = "SELECT * FROM menu_item WHERE status = TRUE ORDER BY category, name";
        List<MenuItem> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public MenuItem findById(int id) throws SQLException {
        String sql = "SELECT * FROM menu_item WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public int create(MenuItem m) throws SQLException {
        String sql = "INSERT INTO menu_item(name, category, price, status) VALUES(?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCategory());
            ps.setBigDecimal(3, m.getPrice());
            ps.setBoolean(4, m.isStatus());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return 0;
    }

    public boolean update(MenuItem m) throws SQLException {
        String sql = "UPDATE menu_item SET name=?, category=?, price=?, status=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setString(2, m.getCategory());
            ps.setBigDecimal(3, m.getPrice());
            ps.setBoolean(4, m.isStatus());
            ps.setInt(5, m.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM menu_item WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }
}

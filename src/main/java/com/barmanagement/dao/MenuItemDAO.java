package com.barmanagement.dao;

import com.barmanagement.model.MenuItem;

import java.sql.*;
import java.util.*;

public class MenuItemDAO {

    public List<MenuItem> findAll() throws SQLException {
        String sql = "SELECT id, name, price, category FROM menu_items ORDER BY id";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<MenuItem> list = new ArrayList<>();
            while (rs.next()) {
                MenuItem m = new MenuItem();
                m.setId(rs.getInt("id"));
                m.setName(rs.getString("name"));
                // DECIMAL -> double
                m.setPrice(rs.getBigDecimal("price").doubleValue());
                m.setCategory(rs.getString("category"));
                list.add(m);
            }
            return list;
        }
    }

    public int insert(MenuItem m) throws SQLException {
        String sql = "INSERT INTO menu_items(name, price, category) VALUES(?,?,?)";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, m.getName());
            // double -> DECIMAL
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(m.getPrice()));
            ps.setString(3, m.getCategory());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        }
    }

    public void update(MenuItem m) throws SQLException {
        String sql = "UPDATE menu_items SET name=?, price=?, category=? WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getName());
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(m.getPrice()));
            ps.setString(3, m.getCategory());
            ps.setInt(4, m.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM menu_items WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public MenuItem findById(int id) throws SQLException {
        String sql = "SELECT id, name, price, category FROM menu_items WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                MenuItem m = new MenuItem();
                m.setId(rs.getInt("id"));
                m.setName(rs.getString("name"));
                m.setPrice(rs.getBigDecimal("price").doubleValue());
                m.setCategory(rs.getString("category"));
                return m;
            }
        }
    }
}

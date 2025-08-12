package com.barmanagement.dao;

import com.barmanagement.config.DatabaseConnection;
import com.barmanagement.model.MenuItem;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public Order createNewOrder(int tableId, int staffId) throws SQLException {
        String sql = "INSERT INTO orders (table_id, staff_id, total_amount, status) VALUES (?, ?, 0, 'pending')";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.setInt(2, staffId);
            ps.executeUpdate();
            int orderId = 0;
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) orderId = keys.getInt(1);
            }
            return findById(orderId);
        }
    }

    public Order findById(int id) throws SQLException {
        String sql = "SELECT id, table_id, staff_id, total_amount, status, created_at FROM orders WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Order o = new Order(
                        rs.getInt("id"),
                        rs.getInt("table_id"),
                        rs.getInt("staff_id"),
                        rs.getBigDecimal("total_amount"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                );
                o.setItems(getItems(id));
                return o;
            }
        }
    }

    public List<Order> findActiveByTable(int tableId) throws SQLException {
        String sql = "SELECT id FROM orders WHERE table_id=? AND status IN ('pending','served') ORDER BY created_at DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) list.add(findById(rs.getInt(1)));
                return list;
            }
        }
    }

    public void addItem(int orderId, int menuItemId, int quantity, java.math.BigDecimal unitPrice) throws SQLException {
        String sql = "INSERT INTO order_item (order_id, menu_item_id, quantity, price) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, menuItemId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.executeUpdate();
        }
        recomputeTotal(orderId);
    }

    public void removeItem(int orderItemId) throws SQLException {
        int orderId = getOrderIdByOrderItem(orderItemId);
        String sql = "DELETE FROM order_item WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            ps.executeUpdate();
        }
        if (orderId > 0) recomputeTotal(orderId);
    }

    public List<OrderItem> getItems(int orderId) throws SQLException {
        String sql = """
                SELECT oi.id, oi.order_id, oi.menu_item_id, oi.quantity, oi.price,
                       mi.name, mi.category
                FROM order_item oi
                JOIN menu_item mi ON mi.id = oi.menu_item_id
                WHERE oi.order_id=?
                ORDER BY oi.id
                """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> list = new ArrayList<>();
                while (rs.next()) {
                    MenuItem m = new MenuItem(
                            rs.getInt("menu_item_id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getBigDecimal("price"),
                            true
                    );
                    list.add(new OrderItem(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            m,
                            rs.getInt("quantity"),
                            rs.getBigDecimal("price")
                    ));
                }
                return list;
            }
        }
    }

    public void updateStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public void markPaid(int orderId) throws SQLException {
        updateStatus(orderId, "paid");
    }

    private void recomputeTotal(int orderId) throws SQLException {
        String sql = "UPDATE orders o SET total_amount = " +
                "(SELECT COALESCE(SUM(quantity * price),0) FROM order_item WHERE order_id=o.id) " +
                "WHERE o.id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    private int getOrderIdByOrderItem(int orderItemId) throws SQLException {
        String sql = "SELECT order_id FROM order_item WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }
}

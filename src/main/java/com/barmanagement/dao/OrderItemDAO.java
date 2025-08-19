package com.barmanagement.dao;

import com.barmanagement.model.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    /** Lấy danh sách item của một order + dùng đơn giá đã chốt (oi.price) */
    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        String sql = """
            SELECT oi.id,
                   oi.order_id,
                   oi.menu_item_id,
                   oi.quantity,
                   oi.price AS unit_price,     -- dùng giá đã chốt trong order_items
                   mi.name AS menu_item_name
            FROM order_items oi
            JOIN menu_items mi ON mi.id = oi.menu_item_id
            WHERE oi.order_id = ?
            ORDER BY oi.id
        """;
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> list = new ArrayList<>();
                while (rs.next()) {
                    OrderItem it = new OrderItem();
                    it.setId(rs.getInt("id"));
                    it.setOrderId(rs.getInt("order_id"));
                    it.setMenuItemId(rs.getInt("menu_item_id"));
                    it.setQuantity(rs.getInt("quantity"));
                    // DECIMAL -> double (OrderItem.price là double)
                    BigDecimal unitPrice = rs.getBigDecimal("unit_price");
                    it.setPrice(unitPrice != null ? unitPrice.doubleValue() : 0d);
                    it.setMenuItemName(rs.getString("menu_item_name"));
                    list.add(it);
                }
                return list;
            }
        }
    }

    /** Thêm mới; nếu đã có (orderId, menuItemId) thì tăng số lượng
     *  Lưu ý: INSERT kèm price (chụp từ menu_items tại thời điểm thêm) */
    public void addOrIncrement(int orderId, int menuItemId, int qty) throws SQLException {
        String up  = "UPDATE order_items SET quantity = quantity + ? WHERE order_id=? AND menu_item_id=?";
        String ins = """
            INSERT INTO order_items(order_id, menu_item_id, quantity, price)
            SELECT ?, ?, ?, mi.price
              FROM menu_items mi
             WHERE mi.id = ?
        """;
        try (Connection c = JDBCConnect.getJDBCConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement psUp = c.prepareStatement(up)) {
                psUp.setInt(1, qty);
                psUp.setInt(2, orderId);
                psUp.setInt(3, menuItemId);
                int n = psUp.executeUpdate();
                if (n == 0) {
                    try (PreparedStatement psIns = c.prepareStatement(ins)) {
                        psIns.setInt(1, orderId);
                        psIns.setInt(2, menuItemId);
                        psIns.setInt(3, qty);
                        psIns.setInt(4, menuItemId);
                        psIns.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    public void updateQuantity(int orderItemId, int qty) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE order_items SET quantity=? WHERE id=?")) {
            ps.setInt(1, qty);
            ps.setInt(2, orderItemId);
            ps.executeUpdate();
        }
    }

    public void delete(int orderItemId) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM order_items WHERE id=?")) {
            ps.setInt(1, orderItemId);
            ps.executeUpdate();
        }
    }

    public void clearByOrder(int orderId) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM order_items WHERE order_id=?")) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    /** Tính tổng tiền (SL * giá chốt trong order_items) của order */
    public BigDecimal calculateTotal(int orderId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(oi.quantity * oi.price), 0) AS total
            FROM order_items oi
            WHERE oi.order_id = ?
        """;
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal("total");
            }
        }
    }
}

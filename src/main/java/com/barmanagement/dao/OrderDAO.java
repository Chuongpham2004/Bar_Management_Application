package com.barmanagement.dao;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;

import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class OrderDAO {

    public Integer createEmptyOrder(int tableId) throws SQLException {
        String sql = "INSERT INTO orders(table_id, status) VALUES(?, 'pending')";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : null; }
        }
    }

    public Order findPendingByTable(int tableId) throws SQLException {
        String sql = "SELECT id, table_id, status, order_time FROM orders WHERE table_id=? AND status='pending' ORDER BY id DESC LIMIT 1";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setTableId(rs.getInt("table_id"));
                o.setStatus(rs.getString("status"));
                o.setOrderTime(rs.getTimestamp("order_time"));
                return o;
            }
        }
    }

    public List<OrderItem> findItems(int orderId) throws SQLException {
        String sql = """
        SELECT oi.id, oi.order_id, oi.menu_item_id, oi.quantity,
               mi.price AS unit_price
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
                    // DECIMAL -> double
                    it.setPrice(rs.getBigDecimal("unit_price").doubleValue());
                    list.add(it);
                }
                return list;
            }
        }
    }

    public void addItem(int orderId, int menuItemId, int qty) throws SQLException {
        String up  = "UPDATE order_items SET quantity = quantity + ? WHERE order_id=? AND menu_item_id=?";
        String ins = "INSERT INTO order_items(order_id, menu_item_id, quantity) VALUES(?,?,?)";
        try (Connection c = JDBCConnect.getJDBCConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement psUp = c.prepareStatement(up)) {
                psUp.setInt(1, qty); psUp.setInt(2, orderId); psUp.setInt(3, menuItemId);
                int n = psUp.executeUpdate();
                if (n == 0) {
                    try (PreparedStatement psIns = c.prepareStatement(ins)) {
                        psIns.setInt(1, orderId); psIns.setInt(2, menuItemId); psIns.setInt(3, qty);
                        psIns.executeUpdate();
                    }
                }
                c.commit();
            } catch (Exception e) { c.rollback(); throw e; }
            finally { c.setAutoCommit(true); }
        }
    }

    public void updateItemQty(int orderItemId, int qty) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE order_items SET quantity=? WHERE id=?")) {
            ps.setInt(1, qty);
            ps.setInt(2, orderItemId);
            ps.executeUpdate();
        }
    }

    public void removeItem(int orderItemId) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM order_items WHERE id=?")) {
            ps.setInt(1, orderItemId);
            ps.executeUpdate();
        }
    }

    public java.math.BigDecimal calcTotal(int orderId) throws SQLException {
        String sql = """
      SELECT COALESCE(SUM(oi.quantity * mi.price),0) AS total
      FROM order_items oi JOIN menu_items mi ON mi.id = oi.menu_item_id
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

//    public void complete(int orderId) throws SQLException {
//        try (Connection c = JDBCConnect.getJDBCConnection()) {
//            c.setAutoCommit(false);
//            try {
//                BigDecimal total = calcTotal(orderId);
//
//                try (PreparedStatement p1 = c.prepareStatement("UPDATE orders SET status='completed' WHERE id=?")) {
//                    p1.setInt(1, orderId);
//                    p1.executeUpdate();
//                }
//                try (PreparedStatement p2 = c.prepareStatement(
//                        "INSERT INTO payments(order_id, total_amount, payment_method) VALUES(?, ?, 'cash')")) {
//                    p2.setInt(1, orderId);
//                    p2.setBigDecimal(2, total);
//                    p2.executeUpdate();
//                }
//                // giải phóng bàn
//                try (PreparedStatement p3 = c.prepareStatement(
//                        "UPDATE tables t JOIN orders o ON o.table_id=t.id SET t.status='empty' WHERE o.id=?")) {
//                    p3.setInt(1, orderId);
//                    p3.executeUpdate();
//                }
//                c.commit();
//            } catch (Exception e) { c.rollback(); throw e; }
//            finally { c.setAutoCommit(true); }
//        }
//    }
public void complete(int orderId, String paymentMethod) throws SQLException {
    try (Connection c = JDBCConnect.getJDBCConnection()) {
        c.setAutoCommit(false);
        try {
            BigDecimal total = calcTotal(orderId);

            try (PreparedStatement p1 = c.prepareStatement("UPDATE orders SET status='completed' WHERE id=?")) {
                p1.setInt(1, orderId);
                p1.executeUpdate();
            }
            try (PreparedStatement p2 = c.prepareStatement(
                    "INSERT INTO payments(order_id, total_amount, payment_method) VALUES(?, ?, ?)")) {
                p2.setInt(1, orderId);
                p2.setBigDecimal(2, total);
                p2.setString(3, paymentMethod);
                p2.executeUpdate();
            }
            // Giải phóng bàn
            try (PreparedStatement p3 = c.prepareStatement(
                    "UPDATE tables t JOIN orders o ON o.table_id=t.id SET t.status='empty' WHERE o.id=?")) {
                p3.setInt(1, orderId);
                p3.executeUpdate();
            }
            c.commit();
        } catch (Exception e) { c.rollback(); throw e; }
        finally { c.setAutoCommit(true); }
    }
}

}

package com.barmanagement.dao;

import com.barmanagement.model.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO {

    /** Lấy danh sách item của một order + kèm đơn giá từ menu_items */
    public List<OrderItem> findByOrderId(int orderId) throws SQLException {
        String sql = """
            SELECT oi.id,
                   oi.order_id,
                   oi.menu_item_id,
                   oi.quantity,
                   mi.price AS unit_price,
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
                    it.setPrice(rs.getBigDecimal("unit_price").doubleValue());
                    it.setMenuItemName(rs.getString("menu_item_name")); // set thêm

                    // THÊM MỚI: Calculate và set subtotal
                    it.setSubtotal(it.getPrice() * it.getQuantity());

                    list.add(it);
                }
                return list;
            }
        }
    }

    /** Thêm mới; nếu đã có (orderId, menuItemId) thì tăng số lượng */
    public void addOrIncrement(int orderId, int menuItemId, int qty) throws SQLException {
        String up  = "UPDATE order_items SET quantity = quantity + ? WHERE order_id=? AND menu_item_id=?";
        String ins = "INSERT INTO order_items(order_id, menu_item_id, quantity) VALUES(?,?,?)";
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

    /** Tính tổng tiền (SL * đơn giá menu) của order */
    public BigDecimal calculateTotal(int orderId) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(oi.quantity * mi.price), 0) AS total
            FROM order_items oi
            JOIN menu_items mi ON mi.id = oi.menu_item_id
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

    // THÊM MỚI: Các methods compatibility với PaymentController

    /**
     * Insert order item mới (cho compatibility)
     */
    public boolean insertOrderItem(OrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, item.getOrderId());
            ps.setInt(2, item.getMenuItemId());
            ps.setInt(3, item.getQuantity());

            int result = ps.executeUpdate();

            if (result > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }

        return false;
    }

    /**
     * Update order item (cho compatibility)
     */
    public boolean updateOrderItem(OrderItem item) throws SQLException {
        String sql = "UPDATE order_items SET quantity = ? WHERE id = ?";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, item.getQuantity());
            ps.setInt(2, item.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Delete order item (alias cho delete method)
     */
    public boolean deleteOrderItem(int orderItemId) throws SQLException {
        delete(orderItemId);
        return true; // delete() throws exception nếu fail
    }

    /**
     * Find by ID (cho compatibility)
     */
    public OrderItem findById(int id) throws SQLException {
        String sql = """
            SELECT oi.id,
                   oi.order_id,
                   oi.menu_item_id,
                   oi.quantity,
                   mi.price AS unit_price,
                   mi.name AS menu_item_name,
                   mi.description,
                   mi.category
            FROM order_items oi
            JOIN menu_items mi ON mi.id = oi.menu_item_id
            WHERE oi.id = ?
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setMenuItemId(rs.getInt("menu_item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("unit_price").doubleValue());
                    item.setMenuItemName(rs.getString("menu_item_name"));
                    item.setDescription(rs.getString("description"));
                    item.setCategory(rs.getString("category"));

                    // Calculate subtotal
                    item.setSubtotal(item.getPrice() * item.getQuantity());

                    return item;
                }
            }
        }

        return null;
    }

    /**
     * Get total items by order ID
     */
    public int getTotalItemsByOrderId(int orderId) throws SQLException {
        String sql = "SELECT SUM(quantity) as total FROM order_items WHERE order_id = ?";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        }

        return 0;
    }

    /**
     * Find by order and menu item
     */
    public OrderItem findByOrderAndMenuItem(int orderId, int menuItemId) throws SQLException {
        String sql = """
            SELECT oi.id,
                   oi.order_id,
                   oi.menu_item_id,
                   oi.quantity,
                   mi.price AS unit_price,
                   mi.name AS menu_item_name
            FROM order_items oi
            JOIN menu_items mi ON mi.id = oi.menu_item_id
            WHERE oi.order_id = ? AND oi.menu_item_id = ?
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, menuItemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setMenuItemId(rs.getInt("menu_item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("unit_price").doubleValue());
                    item.setMenuItemName(rs.getString("menu_item_name"));

                    // Calculate subtotal
                    item.setSubtotal(item.getPrice() * item.getQuantity());

                    return item;
                }
            }
        }

        return null;
    }

    /**
     * Delete all by order ID (alias cho clearByOrder)
     */
    public boolean deleteAllByOrderId(int orderId) throws SQLException {
        clearByOrder(orderId);
        return true; // clearByOrder() throws exception nếu fail
    }
}
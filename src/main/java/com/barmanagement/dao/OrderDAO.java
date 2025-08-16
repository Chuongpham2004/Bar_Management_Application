package com.barmanagement.dao;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;

import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class OrderDAO {

    /**
     * Tạo order trống mới
     */
    public Integer createEmptyOrder(int tableId) throws SQLException {
        String sql = "INSERT INTO orders(table_id, status, order_time) VALUES(?, 'pending', CURRENT_TIMESTAMP)";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : null;
            }
        }
    }

    /**
     * Tìm order đang pending theo table
     */
    public Order findPendingByTable(int tableId) throws SQLException {
        String sql = """
            SELECT id, table_id, status, order_time 
            FROM orders 
            WHERE table_id=? AND status='pending' 
            ORDER BY id DESC LIMIT 1
        """;

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

    /**
     * Lấy danh sách items của order với thông tin menu đầy đủ
     */
    public List<OrderItem> findItems(int orderId) throws SQLException {
        String sql = """
            SELECT oi.id, oi.order_id, oi.menu_item_id, oi.quantity,
                   mi.price AS unit_price, mi.name AS menu_item_name,
                   mi.image_path, mi.category, mi.description
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
                    BigDecimal price = rs.getBigDecimal("unit_price");
                    it.setPrice(price != null ? price.doubleValue() : 0.0);

                    // Thông tin menu item
                    it.setMenuItemName(rs.getString("menu_item_name"));

                    list.add(it);
                }
                return list;
            }
        }
    }

    /**
     * Lấy order items với thông tin chi tiết (cho báo cáo)
     */
    public List<OrderItemDetail> findItemsWithDetails(int orderId) throws SQLException {
        String sql = """
            SELECT oi.id, oi.order_id, oi.menu_item_id, oi.quantity,
                   mi.price AS unit_price, mi.name AS menu_item_name,
                   mi.image_path, mi.category, mi.description,
                   (oi.quantity * mi.price) AS subtotal
            FROM order_items oi
            JOIN menu_items mi ON mi.id = oi.menu_item_id
            WHERE oi.order_id = ?
            ORDER BY oi.id
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItemDetail> list = new ArrayList<>();
                while (rs.next()) {
                    OrderItemDetail detail = new OrderItemDetail();
                    detail.setId(rs.getInt("id"));
                    detail.setOrderId(rs.getInt("order_id"));
                    detail.setMenuItemId(rs.getInt("menu_item_id"));
                    detail.setQuantity(rs.getInt("quantity"));

                    BigDecimal price = rs.getBigDecimal("unit_price");
                    detail.setUnitPrice(price != null ? price.doubleValue() : 0.0);

                    detail.setMenuItemName(rs.getString("menu_item_name"));
                    detail.setImagePath(rs.getString("image_path"));
                    detail.setCategory(rs.getString("category"));
                    detail.setDescription(rs.getString("description"));

                    BigDecimal subtotal = rs.getBigDecimal("subtotal");
                    detail.setSubtotal(subtotal != null ? subtotal.doubleValue() : 0.0);

                    list.add(detail);
                }
                return list;
            }
        }
    }

    /**
     * Thêm item vào order (hoặc tăng quantity nếu đã có)
     */
    public void addItem(int orderId, int menuItemId, int qty) throws SQLException {
        String checkSql = "SELECT id, quantity FROM order_items WHERE order_id=? AND menu_item_id=?";
        String updateSql = "UPDATE order_items SET quantity = quantity + ? WHERE id=?";
        String insertSql = "INSERT INTO order_items(order_id, menu_item_id, quantity) VALUES(?,?,?)";

        try (Connection c = JDBCConnect.getJDBCConnection()) {
            c.setAutoCommit(false);
            try {
                // Kiểm tra item đã tồn tại chưa
                try (PreparedStatement checkPs = c.prepareStatement(checkSql)) {
                    checkPs.setInt(1, orderId);
                    checkPs.setInt(2, menuItemId);
                    try (ResultSet rs = checkPs.executeQuery()) {
                        if (rs.next()) {
                            // Item đã tồn tại, cập nhật quantity
                            int existingId = rs.getInt("id");
                            try (PreparedStatement updatePs = c.prepareStatement(updateSql)) {
                                updatePs.setInt(1, qty);
                                updatePs.setInt(2, existingId);
                                updatePs.executeUpdate();
                            }
                        } else {
                            // Item chưa tồn tại, insert mới
                            try (PreparedStatement insertPs = c.prepareStatement(insertSql)) {
                                insertPs.setInt(1, orderId);
                                insertPs.setInt(2, menuItemId);
                                insertPs.setInt(3, qty);
                                insertPs.executeUpdate();
                            }
                        }
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Cập nhật quantity của order item
     */
    public void updateItemQty(int orderItemId, int qty) throws SQLException {
        if (qty <= 0) {
            removeItem(orderItemId);
            return;
        }

        String sql = "UPDATE order_items SET quantity=? WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, orderItemId);
            ps.executeUpdate();
        }
    }

    /**
     * Xóa item khỏi order
     */
    public void removeItem(int orderItemId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            ps.executeUpdate();
        }
    }

    /**
     * Xóa tất cả items của order
     */
    public void clearAllItems(int orderId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE order_id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    /**
     * Tính tổng tiền order
     */
    public BigDecimal calcTotal(int orderId) throws SQLException {
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
                BigDecimal total = rs.getBigDecimal("total");
                return total != null ? total : BigDecimal.ZERO;
            }
        }
    }

    /**
     * Hoàn thành order
     */
    public void complete(int orderId) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal total = calcTotal(orderId);

                // Cập nhật status order
                try (PreparedStatement p1 = c.prepareStatement(
                        "UPDATE orders SET status='completed', completed_time=CURRENT_TIMESTAMP WHERE id=?")) {
                    p1.setInt(1, orderId);
                    p1.executeUpdate();
                }

                // Tạo payment record
                try (PreparedStatement p2 = c.prepareStatement(
                        "INSERT INTO payments(order_id, total_amount, payment_method, payment_time) VALUES(?, ?, 'cash', CURRENT_TIMESTAMP)")) {
                    p2.setInt(1, orderId);
                    p2.setBigDecimal(2, total);
                    p2.executeUpdate();
                }

                // Giải phóng bàn
                try (PreparedStatement p3 = c.prepareStatement(
                        "UPDATE tables t JOIN orders o ON o.table_id=t.id SET t.status='empty' WHERE o.id=?")) {
                    p3.setInt(1, orderId);
                    p3.executeUpdate();
                }

                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Hủy order
     */
    public void cancel(int orderId) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection()) {
            c.setAutoCommit(false);
            try {
                // Cập nhật status order
                try (PreparedStatement p1 = c.prepareStatement(
                        "UPDATE orders SET status='cancelled' WHERE id=?")) {
                    p1.setInt(1, orderId);
                    p1.executeUpdate();
                }

                // Giải phóng bàn
                try (PreparedStatement p2 = c.prepareStatement(
                        "UPDATE tables t JOIN orders o ON o.table_id=t.id SET t.status='empty' WHERE o.id=?")) {
                    p2.setInt(1, orderId);
                    p2.executeUpdate();
                }

                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Lấy tất cả orders theo status
     */
    public List<Order> findByStatus(String status) throws SQLException {
        String sql = """
            SELECT o.id, o.table_id, o.status, o.order_time,
                   t.table_name, COALESCE(SUM(oi.quantity * mi.price), 0) AS total
            FROM orders o
            JOIN tables t ON t.id = o.table_id
            LEFT JOIN order_items oi ON oi.order_id = o.id
            LEFT JOIN menu_items mi ON mi.id = oi.menu_item_id
            WHERE o.status = ?
            GROUP BY o.id, o.table_id, o.status, o.order_time, t.table_name
            ORDER BY o.order_time DESC
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setTableId(rs.getInt("table_id"));
                    order.setStatus(rs.getString("status"));
                    order.setOrderTime(rs.getTimestamp("order_time"));
                    // Có thể thêm total và table_name vào Order model nếu cần
                    orders.add(order);
                }
                return orders;
            }
        }
    }

    /**
     * Lấy order theo ID
     */
    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT id, table_id, status, order_time FROM orders WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setTableId(rs.getInt("table_id"));
                order.setStatus(rs.getString("status"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                return order;
            }
        }
    }

    /**
     * Đếm số items trong order
     */
    public int countItems(int orderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) as total_items FROM order_items WHERE order_id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("total_items");
            }
        }
    }

    // Inner class để chứa thông tin chi tiết order item
    public static class OrderItemDetail {
        private int id;
        private int orderId;
        private int menuItemId;
        private int quantity;
        private double unitPrice;
        private double subtotal;
        private String menuItemName;
        private String imagePath;
        private String category;
        private String description;

        // Getters and setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }

        public int getMenuItemId() { return menuItemId; }
        public void setMenuItemId(int menuItemId) { this.menuItemId = menuItemId; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

        public double getSubtotal() { return subtotal; }
        public void setSubtotal(double subtotal) { this.subtotal = subtotal; }

        public String getMenuItemName() { return menuItemName; }
        public void setMenuItemName(String menuItemName) { this.menuItemName = menuItemName; }

        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }
}
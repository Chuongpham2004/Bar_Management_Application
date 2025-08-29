package com.barmanagement.dao;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.Payment;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO class for Order operations
 * FIXED: All payment processing and status management
 */
public class OrderDAO {

    /**
     * Find order by ID
     */
    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT id, table_id, order_time, completed_time, status, total_amount, notes, created_by " +
                "FROM orders WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractOrderFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Process payment for an order - COMPLETELY FIXED VERSION
     */
    public void processPayment(int orderId, String paymentMethod, int userId) throws SQLException {
        Connection conn = null;
        try {
            conn = JDBCConnect.getJDBCConnection();
            conn.setAutoCommit(false);

            System.out.println("=== PROCESSING PAYMENT FOR ORDER #" + orderId + " ===");

            // Get order details first
            Order order = findById(orderId);
            if (order == null) {
                throw new SQLException("Order not found: " + orderId);
            }

            // Calculate total amount
            BigDecimal totalAmount = calcTotal(orderId);
            System.out.println("Order total amount: " + totalAmount);

            if (totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new SQLException("Invalid order amount: " + totalAmount);
            }

            // 1. Update order status to paid
            String updateOrderSql = "UPDATE orders SET status = 'paid', completed_time = NOW(), total_amount = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
                ps.setBigDecimal(1, totalAmount);
                ps.setInt(2, orderId);
                int updated = ps.executeUpdate();
                System.out.println("Updated order status: " + updated + " rows");
            }

            // 2. Create payment record - FIXED: using correct column name
            String insertPaymentSql = "INSERT INTO payments (order_id, total_amount, payment_method, payment_time, processed_by) VALUES (?, ?, ?, NOW(), ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql)) {
                ps.setInt(1, orderId);
                ps.setBigDecimal(2, totalAmount);
                ps.setString(3, paymentMethod);
                ps.setInt(4, userId);
                int inserted = ps.executeUpdate();
                System.out.println("Created payment record: " + inserted + " rows");
            }

            // 3. Update table status to empty - FIXED: using correct status name
            String updateTableSql = "UPDATE tables SET status = 'empty' WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateTableSql)) {
                ps.setInt(1, order.getTableId());
                int updated = ps.executeUpdate();
                System.out.println("Updated table status: " + updated + " rows");
            }

            // 4. Update revenue for today - FIXED: proper revenue update
            String updateRevenueSql = "INSERT INTO revenue (date, total_amount, total_orders) VALUES (CURDATE(), ?, 1) " +
                    "ON DUPLICATE KEY UPDATE total_amount = total_amount + VALUES(total_amount), total_orders = total_orders + 1";
            try (PreparedStatement ps = conn.prepareStatement(updateRevenueSql)) {
                ps.setBigDecimal(1, totalAmount);
                int updated = ps.executeUpdate();
                System.out.println("Updated revenue: " + updated + " rows");
            }

            conn.commit();
            System.out.println("=== PAYMENT PROCESSED SUCCESSFULLY ===");
            System.out.println("Order #" + orderId + " paid: " + totalAmount + " VND via " + paymentMethod);

        } catch (SQLException e) {
            System.err.println("Error processing payment: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("Transaction rolled back");
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Error closing connection: " + closeEx.getMessage());
                }
            }
        }
    }

    /**
     * Extract order from ResultSet
     */
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getInt("id"));
        order.setTableId(rs.getInt("table_id"));
        order.setOrderTime(rs.getTimestamp("order_time"));
        order.setCompletedTime(rs.getTimestamp("completed_time"));
        order.setStatus(rs.getString("status"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setNotes(rs.getString("notes"));
        order.setCreatedBy(rs.getInt("created_by"));
        return order;
    }

    /**
     * Extract orders from ResultSet
     */
    private List<Order> extractOrdersFromResultSet(ResultSet rs) throws SQLException {
        List<Order> orders = new ArrayList<>();
        while (rs.next()) {
            orders.add(extractOrderFromResultSet(rs));
        }
        return orders;
    }

    /**
     * FIXED: Get completed but not paid orders - Only today's real orders
     */
    public List<Order> findCompletedNotPaidOrders() throws SQLException {
        String sql = "SELECT DISTINCT o.id, o.table_id, o.order_time, o.completed_time, o.status, o.total_amount, o.notes, o.created_by " +
                "FROM orders o " +
                "INNER JOIN order_items oi ON o.id = oi.order_id " +
                "LEFT JOIN payments p ON o.id = p.order_id " +
                "WHERE o.status = 'completed' " +
                "AND DATE(o.order_time) = CURDATE() " +
                "AND o.total_amount > 0 " +
                "AND p.order_id IS NULL " + // NOT YET PAID
                "ORDER BY o.order_time DESC";

        System.out.println("Finding completed unpaid orders for today...");

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Order> completedOrders = extractOrdersFromResultSet(rs);
            System.out.println("Found " + completedOrders.size() + " completed unpaid orders for today");

            // Debug log
            for (Order order : completedOrders) {
                System.out.println("Unpaid Order: #" + order.getId() +
                        " - Table " + order.getTableId() +
                        " - Amount: " + order.getTotalAmount() +
                        " - Time: " + order.getFormattedOrderTime());
            }

            return completedOrders;
        }
    }

    /**
     * Get orders by status with filtering
     */
    public List<Order> findByStatus(String status) throws SQLException {
        if ("completed".equals(status)) {
            return findCompletedNotPaidOrders();
        }

        String sql = "SELECT id, table_id, order_time, completed_time, status, total_amount, notes, created_by " +
                "FROM orders WHERE status = ? ORDER BY order_time DESC";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return extractOrdersFromResultSet(rs);
            }
        }
    }

    /**
     * Clean up old completed orders that were not paid
     */
    public void cleanupOldCompletedOrders() throws SQLException {
        String sql = "UPDATE orders SET status = 'cancelled' " +
                "WHERE status = 'completed' " +
                "AND DATE(order_time) < CURDATE() " +
                "AND id NOT IN (SELECT DISTINCT order_id FROM payments)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int cleaned = ps.executeUpdate();
            if (cleaned > 0) {
                System.out.println("Cleaned up " + cleaned + " old completed orders");
            }
        }
    }

    /**
     * Check if order has actual items
     */
    public boolean hasActualItems(int orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM order_items WHERE order_id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Get completed orders with details (unpaid only)
     */
    public List<Order> findCompletedOrdersWithDetails() throws SQLException {
        String sql = "SELECT o.id, o.table_id, o.order_time, o.completed_time, o.status, o.total_amount, o.notes, o.created_by, " +
                "COUNT(oi.id) as item_count, SUM(oi.quantity) as total_quantity " +
                "FROM orders o " +
                "LEFT JOIN order_items oi ON o.id = oi.order_id " +
                "LEFT JOIN payments p ON o.id = p.order_id " +
                "WHERE o.status = 'completed' " +
                "AND DATE(o.order_time) = CURDATE() " +
                "AND p.order_id IS NULL " + // NOT YET PAID
                "GROUP BY o.id, o.table_id, o.order_time, o.completed_time, o.status, o.total_amount, o.notes, o.created_by " +
                "HAVING item_count > 0 " +
                "ORDER BY o.order_time DESC";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                System.out.println("Unpaid completed order: #" + order.getId() +
                        " - " + rs.getInt("item_count") + " items, " +
                        rs.getInt("total_quantity") + " total quantity");
                orders.add(order);
            }

            return orders;
        }
    }

    /**
     * Find pending order by table ID
     */
    public Order findPendingByTable(int tableId) throws SQLException {
        String sql = "SELECT id, table_id, order_time, completed_time, status, total_amount, notes, created_by " +
                "FROM orders WHERE table_id = ? AND status IN ('pending', 'ordering', 'completed') " +
                "ORDER BY order_time DESC LIMIT 1";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractOrderFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Create empty order for table
     */
    public Integer createEmptyOrder(int tableId) throws SQLException {
        String sql = "INSERT INTO orders (table_id, order_time, status, total_amount, created_by) VALUES (?, NOW(), 'pending', 0, 1)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tableId);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        System.out.println("Created empty order #" + orderId + " for table " + tableId);
                        return orderId;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Add item to order
     */
    public void addItem(int orderId, int menuItemId, int quantity) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price) " +
                "SELECT ?, ?, ?, price FROM menu_items WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, menuItemId);
            ps.setInt(3, quantity);
            ps.setInt(4, menuItemId);
            ps.executeUpdate();

            System.out.println("Added item to order: OrderID=" + orderId + ", MenuItemID=" + menuItemId + ", Qty=" + quantity);
        }
    }

    /**
     * Remove item from order
     */
    public void removeItem(int orderItemId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderItemId);
            int deleted = ps.executeUpdate();
            System.out.println("Removed order item #" + orderItemId + ": " + deleted + " rows deleted");
        }
    }

    /**
     * Find order items by order ID
     */
    public List<OrderItem> findItems(int orderId) throws SQLException {
        String sql = "SELECT oi.id, oi.order_id, oi.menu_item_id, oi.quantity, oi.price, " +
                "mi.name as menu_item_name, mi.category as menu_item_category " +
                "FROM order_items oi " +
                "LEFT JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setMenuItemId(rs.getInt("menu_item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));
                    item.setMenuItemName(rs.getString("menu_item_name"));
                    item.setMenuItemCategory(rs.getString("menu_item_category"));
                    items.add(item);
                }
                return items;
            }
        }
    }

    /**
     * Calculate total amount for order
     */
    public BigDecimal calcTotal(int orderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * price), 0) as total FROM order_items WHERE order_id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Mark order as completed
     */
    public void markCompleted(int orderId) throws SQLException {
        // First calculate and update total amount
        BigDecimal totalAmount = calcTotal(orderId);

        String sql = "UPDATE orders SET status = 'completed', completed_time = NOW(), total_amount = ? WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, totalAmount);
            ps.setInt(2, orderId);
            int updated = ps.executeUpdate();

            System.out.println("Marked order #" + orderId + " as completed with amount: " + totalAmount + " (" + updated + " rows)");
        }
    }
}
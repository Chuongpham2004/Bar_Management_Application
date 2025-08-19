package com.barmanagement.dao;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Tạo order mới rỗng
    public Integer createEmptyOrder(int tableId) throws SQLException {
        String sql = "INSERT INTO orders (table_id, status, created_by) VALUES (?, 'pending', 1)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tableId);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer orderId = generatedKeys.getInt(1);
                        System.out.println("Created new order with ID: " + orderId);
                        return orderId;
                    }
                }
            }
        }
        return null;
    }

    // Tìm order pending của bàn
    public Order findPendingByTable(int tableId) throws SQLException {
        String sql = "SELECT id, table_id, order_time, status, total_amount FROM orders WHERE table_id = ? AND status = 'pending' ORDER BY order_time DESC LIMIT 1";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setTableId(rs.getInt("table_id"));
                    order.setOrderTime(rs.getTimestamp("order_time"));
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    System.out.println("Found pending order: " + order.getId() + " for table " + tableId);
                    return order;
                }
            }
        }
        System.out.println("No pending order found for table " + tableId);
        return null;
    }

    // Thêm item vào order
    public void addItem(int orderId, int menuItemId, int quantity) throws SQLException {
        System.out.println("Adding item to order: OrderID=" + orderId + ", MenuItemID=" + menuItemId + ", Qty=" + quantity);

        // Lấy giá hiện tại của món
        String getPrice = "SELECT price FROM menu_items WHERE id = ?";
        double price = 0;

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(getPrice)) {

            ps.setInt(1, menuItemId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    price = rs.getDouble("price");
                    System.out.println("Found price for menu item " + menuItemId + ": " + price);
                } else {
                    throw new SQLException("Menu item not found: " + menuItemId);
                }
            }
        }

        // Kiểm tra xem item đã có trong order chưa
        String checkExist = "SELECT id, quantity FROM order_items WHERE order_id = ? AND menu_item_id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(checkExist)) {

            ps.setInt(1, orderId);
            ps.setInt(2, menuItemId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Item đã tồn tại - cập nhật quantity
                    int existingId = rs.getInt("id");
                    int existingQty = rs.getInt("quantity");

                    System.out.println("Item exists, updating quantity from " + existingQty + " to " + (existingQty + quantity));

                    String updateSql = "UPDATE order_items SET quantity = ? WHERE id = ?";
                    try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                        updatePs.setInt(1, existingQty + quantity);
                        updatePs.setInt(2, existingId);
                        updatePs.executeUpdate();
                    }
                } else {
                    // Item chưa tồn tại - thêm mới
                    System.out.println("Adding new item to order_items");

                    String insertSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                        insertPs.setInt(1, orderId);
                        insertPs.setInt(2, menuItemId);
                        insertPs.setInt(3, quantity);
                        insertPs.setDouble(4, price);
                        int result = insertPs.executeUpdate();
                        System.out.println("Insert result: " + result + " rows affected");
                    }
                }
            }
        }

        System.out.println("Successfully added item to order");
    }

    // Lấy danh sách items của order
    public List<OrderItem> findItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();

        String sql = "SELECT oi.id, oi.order_id, oi.menu_item_id, oi.quantity, oi.price, " +
                "mi.name as menu_item_name, mi.image_path, mi.description, mi.category " +
                "FROM order_items oi " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                "WHERE oi.order_id = ? " +
                "ORDER BY oi.created_at, oi.id";

        System.out.println("Finding items for order: " + orderId);

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setId(rs.getInt("id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setMenuItemId(rs.getInt("menu_item_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getDouble("price"));
                    item.setMenuItemName(rs.getString("menu_item_name"));

                    // Log để debug
                    System.out.println("Found OrderItem: " + rs.getString("menu_item_name") +
                            " x" + rs.getInt("quantity") +
                            " (MenuItemID: " + rs.getInt("menu_item_id") + ")");

                    items.add(item);
                }
            }
        }

        System.out.println("Total items found: " + items.size());
        return items;
    }

    // Tính tổng tiền order
    public BigDecimal calcTotal(int orderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * price), 0) as total FROM order_items WHERE order_id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    System.out.println("Calculated total for order " + orderId + ": " + total);
                    return total;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    // Xóa item khỏi order
    public void removeItem(int orderItemId) throws SQLException {
        System.out.println("Removing order item: " + orderItemId);

        String sql = "DELETE FROM order_items WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderItemId);
            int result = ps.executeUpdate();
            System.out.println("Remove result: " + result + " rows affected");
        }
    }

    // Hoàn thành order - sử dụng stored procedure hoặc manual SQL
    public void complete(int orderId) throws SQLException {
        System.out.println("Completing order: " + orderId);

        try {
            // Thử dùng stored procedure trước
            String sql = "CALL CompleteOrder(?, ?)";

            try (Connection conn = JDBCConnect.getJDBCConnection();
                 CallableStatement cs = conn.prepareCall(sql)) {

                cs.setInt(1, orderId);
                cs.setInt(2, 1); // user_id mặc định là 1
                cs.execute();
                System.out.println("Order completed successfully using stored procedure");
            }
        } catch (SQLException e) {
            // Nếu stored procedure không có, dùng manual SQL
            System.out.println("Stored procedure not available, using manual completion");
            completeOrderManual(orderId);
        }
    }

    // Manual completion nếu stored procedure không có
    private void completeOrderManual(int orderId) throws SQLException {
        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            conn.setAutoCommit(false);

            try {
                // Tính tổng tiền
                BigDecimal total = calcTotal(orderId);

                // Lấy table_id
                String getTableSql = "SELECT table_id FROM orders WHERE id = ?";
                int tableId = 0;
                try (PreparedStatement ps = conn.prepareStatement(getTableSql)) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            tableId = rs.getInt("table_id");
                        }
                    }
                }

                // Cập nhật order
                String updateOrderSql = "UPDATE orders SET status = 'completed', completed_time = CURRENT_TIMESTAMP, total_amount = ? WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateOrderSql)) {
                    ps.setBigDecimal(1, total);
                    ps.setInt(2, orderId);
                    ps.executeUpdate();
                }

                // Tạo payment
                String insertPaymentSql = "INSERT INTO payments(order_id, total_amount, payment_method, processed_by) VALUES(?, ?, 'cash', 1)";
                try (PreparedStatement ps = conn.prepareStatement(insertPaymentSql)) {
                    ps.setInt(1, orderId);
                    ps.setBigDecimal(2, total);
                    ps.executeUpdate();
                }

                // Giải phóng bàn
                String updateTableSql = "UPDATE tables SET status = 'empty' WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateTableSql)) {
                    ps.setInt(1, tableId);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("Order completed manually");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Hủy order
    public void cancel(int orderId) throws SQLException {
        System.out.println("Cancelling order: " + orderId);

        try {
            String sql = "CALL CancelOrder(?)";

            try (Connection conn = JDBCConnect.getJDBCConnection();
                 CallableStatement cs = conn.prepareCall(sql)) {

                cs.setInt(1, orderId);
                cs.execute();
                System.out.println("Order cancelled successfully");
            }
        } catch (SQLException e) {
            // Manual cancel nếu stored procedure không có
            cancelOrderManual(orderId);
        }
    }

    private void cancelOrderManual(int orderId) throws SQLException {
        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lấy table_id
                String getTableSql = "SELECT table_id FROM orders WHERE id = ?";
                int tableId = 0;
                try (PreparedStatement ps = conn.prepareStatement(getTableSql)) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            tableId = rs.getInt("table_id");
                        }
                    }
                }

                // Cập nhật order status
                String updateSql = "UPDATE orders SET status = 'cancelled' WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // Giải phóng bàn
                String updateTableSql = "UPDATE tables SET status = 'empty' WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateTableSql)) {
                    ps.setInt(1, tableId);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("Order cancelled manually");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Lấy tất cả orders
    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, table_id, order_time, completed_time, status, total_amount FROM orders ORDER BY order_time DESC";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setTableId(rs.getInt("table_id"));
                order.setOrderTime(rs.getTimestamp("order_time"));
                order.setCompletedTime(rs.getTimestamp("completed_time"));
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                orders.add(order);
            }
        }
        return orders;
    }

    // Tìm order theo ID
    public Order findById(int id) throws SQLException {
        String sql = "SELECT id, table_id, order_time, completed_time, status, total_amount FROM orders WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setId(rs.getInt("id"));
                    order.setTableId(rs.getInt("table_id"));
                    order.setOrderTime(rs.getTimestamp("order_time"));
                    order.setCompletedTime(rs.getTimestamp("completed_time"));
                    order.setStatus(rs.getString("status"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    return order;
                }
            }
        }
        return null;
    }

    // Cập nhật quantity của order item
    public void updateItemQuantity(int orderItemId, int newQuantity) throws SQLException {
        String sql = "UPDATE order_items SET quantity = ? WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, newQuantity);
            ps.setInt(2, orderItemId);
            ps.executeUpdate();
        }
    }
}
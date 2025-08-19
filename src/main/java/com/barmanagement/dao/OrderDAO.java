package com.barmanagement.dao;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;


import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // =========================
    // Create
    // =========================
    /** Tạo order rỗng ở trạng thái 'pending' */
    public Integer createEmptyOrder(int tableId) throws SQLException {
        String sql = "INSERT INTO orders (table_id, status, created_by) VALUES (?, 'pending', 1)";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tableId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        int orderId = rs.getInt(1);
                        System.out.println("[OrderDAO] Created new order id=" + orderId + " for table " + tableId);
                        return orderId;
                    }
                }
            }
        }
        return null;
    }

    // =========================
    // Read
    // =========================
    /** Lấy order 'pending' mới nhất của một bàn */
    public Order findPendingByTable(int tableId) throws SQLException {
        String sql = """
            SELECT id, table_id, order_time, completed_time, status, total_amount, created_by
              FROM orders
             WHERE table_id = ? AND status = 'pending'
             ORDER BY order_time DESC
             LIMIT 1
        """;

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[OrderDAO] No pending order for table " + tableId);
                    return null;
                }
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setTableId(rs.getInt("table_id"));
                o.setOrderTime(rs.getTimestamp("order_time"));
                o.setCompletedTime(rs.getTimestamp("completed_time"));
                o.setStatus(rs.getString("status"));
                o.setTotalAmount(rs.getBigDecimal("total_amount"));
                System.out.println("[OrderDAO] Found pending order id=" + o.getId() + " for table " + tableId);
                return o;
            }
        }
    }

    // Lấy order MỚI NHẤT của 1 bàn có ít nhất 1 item (fallback khi không có 'pending')
    public Order findLatestOrderWithItemsByTable(int tableId) throws SQLException {
        String sql = """
        SELECT o.id, o.table_id, o.order_time, o.completed_time, o.status, o.total_amount
        FROM orders o
        WHERE o.table_id = ?
          AND EXISTS (SELECT 1 FROM order_items oi WHERE oi.order_id = o.id)
        ORDER BY o.order_time DESC
        LIMIT 1
    """;
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setTableId(rs.getInt("table_id"));
                o.setOrderTime(rs.getTimestamp("order_time"));
                o.setCompletedTime(rs.getTimestamp("completed_time"));
                o.setStatus(rs.getString("status"));
                o.setTotalAmount(rs.getBigDecimal("total_amount"));
                return o;
            }
        }
    }

    /** Lấy danh sách items của order (kèm tên món) */
    public List<OrderItem> findItems(int orderId) throws SQLException {
        List<OrderItem> items = new ArrayList<>();

        String sql = """
            SELECT oi.id, oi.order_id, oi.menu_item_id, oi.quantity, oi.price,
                   mi.name AS menu_item_name, mi.image_path, mi.description, mi.category
              FROM order_items oi
              JOIN menu_items mi ON oi.menu_item_id = mi.id
             WHERE oi.order_id = ?
             ORDER BY oi.created_at, oi.id
        """;

        System.out.println("[OrderDAO] Finding items for order " + orderId);

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem it = new OrderItem();
                    it.setId(rs.getInt("id"));
                    it.setOrderId(rs.getInt("order_id"));
                    it.setMenuItemId(rs.getInt("menu_item_id"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setPrice(rs.getDouble("price"));
                    it.setMenuItemName(rs.getString("menu_item_name"));

                    System.out.println("[OrderDAO]  item: " + it.getMenuItemName()
                            + " x" + it.getQuantity()
                            + " (menu_item_id=" + it.getMenuItemId() + ")");

                    items.add(it);
                }
            }
        }
        System.out.println("[OrderDAO] Total items found: " + items.size());
        return items;
    }

    /** Lấy toàn bộ orders (mới nhất trước) */
    public List<Order> findAll() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT id, table_id, order_time, completed_time, status, total_amount FROM orders ORDER BY order_time DESC";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order o = new Order();
                o.setId(rs.getInt("id"));
                o.setTableId(rs.getInt("table_id"));
                o.setOrderTime(rs.getTimestamp("order_time"));
                o.setCompletedTime(rs.getTimestamp("completed_time"));
                o.setStatus(rs.getString("status"));
                o.setTotalAmount(rs.getBigDecimal("total_amount"));
                orders.add(o);
            }
        }
        return orders;
    }

    /** Tìm order theo id */
    public Order findById(int id) throws SQLException {
        String sql = "SELECT id, table_id, order_time, completed_time, status, total_amount FROM orders WHERE id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setTableId(rs.getInt("table_id"));
                    o.setOrderTime(rs.getTimestamp("order_time"));
                    o.setCompletedTime(rs.getTimestamp("completed_time"));
                    o.setStatus(rs.getString("status"));
                    o.setTotalAmount(rs.getBigDecimal("total_amount"));
                    return o;
                }
            }
        }
        return null;
    }

    // =========================
    // Write (items)
    // =========================
    /** Thêm item vào order (nếu đã có thì cộng dồn quantity) */
    public void addItem(int orderId, int menuItemId, int quantity) throws SQLException {
        System.out.println("[OrderDAO] Add item: order=" + orderId + ", menu_item=" + menuItemId + ", qty=" + quantity);

        // Lấy giá hiện tại của món
        String getPrice = "SELECT price FROM menu_items WHERE id = ?";
        double price;

        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            // 1) price
            try (PreparedStatement ps = conn.prepareStatement(getPrice)) {
                ps.setInt(1, menuItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Menu item not found: " + menuItemId);
                    price = rs.getDouble("price");
                }
            }

            // 2) check exist
            String checkExist = "SELECT id, quantity FROM order_items WHERE order_id = ? AND menu_item_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkExist)) {
                ps.setInt(1, orderId);
                ps.setInt(2, menuItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        // Update quantity
                        int existingId = rs.getInt("id");
                        int existingQty = rs.getInt("quantity");
                        String updateSql = "UPDATE order_items SET quantity = ? WHERE id = ?";
                        try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                            upd.setInt(1, existingQty + quantity);
                            upd.setInt(2, existingId);
                            upd.executeUpdate();
                        }
                    } else {
                        // Insert new
                        String insertSql = "INSERT INTO order_items (order_id, menu_item_id, quantity, price) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                            ins.setInt(1, orderId);
                            ins.setInt(2, menuItemId);
                            ins.setInt(3, quantity);
                            ins.setDouble(4, price);
                            ins.executeUpdate();
                        }
                    }
                }
            }
        }

        System.out.println("[OrderDAO] Add item OK");
    }

    /** Xóa 1 dòng order item */
    public void removeItem(int orderItemId) throws SQLException {
        String sql = "DELETE FROM order_items WHERE id = ?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItemId);
            ps.executeUpdate();
        }
    }

    /** Cập nhật lại quantity cho order item */
    public void updateItemQuantity(int orderItemId, int newQuantity) throws SQLException {
        String sql = "UPDATE order_items SET quantity = ? WHERE id = ?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, orderItemId);
            ps.executeUpdate();
        }
    }

    // =========================
    // Totals
    // =========================
    /** Tính tổng tiền order (dựa trên quantity * price) */
    public BigDecimal calcTotal(int orderId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * price), 0) AS total FROM order_items WHERE order_id = ?";

        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal("total");
            }
        }
        return BigDecimal.ZERO;
    }

    /** Giữ lại để tương thích chỗ gọi cũ (delegate sang calcTotal) */
    public BigDecimal calcOrderTotal(int orderId) throws SQLException {
        return calcTotal(orderId);
    }

    // =========================
    // Complete / Cancel
    // =========================
    /** Hoàn thành đơn: ưu tiên gọi Stored Procedure CompleteOrder(order_id, user_id). Nếu lỗi → manual. */
    public void complete(int orderId) throws SQLException {
        System.out.println("[OrderDAO] Completing order id=" + orderId);
        try {
            String sql = "CALL CompleteOrder(?, ?)";
            try (Connection conn = JDBCConnect.getJDBCConnection();
                 CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, orderId);
                cs.setInt(2, 1); // TODO: thay bằng user_id hiện đăng nhập
                cs.execute();
            }
            System.out.println("[OrderDAO] Completed via Stored Procedure");
        } catch (SQLException spEx) {
            System.out.println("[OrderDAO] SP not available -> manual completion");
            completeOrderManual(orderId);
        }
    }

    /** Manual completion nếu không có SP: set orders.completed, tạo payments(cash), free table */
    private void completeOrderManual(int orderId) throws SQLException {
        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1) Tổng
                BigDecimal total = calcTotal(orderId);

                // 2) table_id
                int tableId = 0;
                String getTable = "SELECT table_id FROM orders WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(getTable)) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) tableId = rs.getInt("table_id");
                    }
                }

                // 3) Update order -> completed
                String updOrder = "UPDATE orders SET status='completed', completed_time=CURRENT_TIMESTAMP, total_amount=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(updOrder)) {
                    ps.setBigDecimal(1, total);
                    ps.setInt(2, orderId);
                    ps.executeUpdate();
                }

                // 4) Insert payment (mặc định 'cash')
                String insPay = "INSERT INTO payments(order_id, total_amount, payment_method, processed_by) VALUES(?, ?, 'cash', 1)";
                try (PreparedStatement ps = conn.prepareStatement(insPay)) {
                    ps.setInt(1, orderId);
                    ps.setBigDecimal(2, total);
                    ps.executeUpdate();
                }

                // 5) Free table
                String freeTable = "UPDATE tables SET status='empty' WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(freeTable)) {
                    ps.setInt(1, tableId);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("[OrderDAO] Manual completion DONE");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Hủy đơn: ưu tiên SP CancelOrder(order_id). Nếu lỗi → manual. */
    public void cancel(int orderId) throws SQLException {
        System.out.println("[OrderDAO] Cancelling order id=" + orderId);
        try {
            String sql = "CALL CancelOrder(?)";
            try (Connection conn = JDBCConnect.getJDBCConnection();
                 CallableStatement cs = conn.prepareCall(sql)) {
                cs.setInt(1, orderId);
                cs.execute();
            }
            System.out.println("[OrderDAO] Cancelled via Stored Procedure");
        } catch (SQLException spEx) {
            System.out.println("[OrderDAO] SP not available -> manual cancel");
            cancelOrderManual(orderId);
        }
    }

    /** Manual cancel nếu không có SP: set 'cancelled', free table */
    private void cancelOrderManual(int orderId) throws SQLException {
        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            conn.setAutoCommit(false);
            try {
                // table_id
                int tableId = 0;
                String getTable = "SELECT table_id FROM orders WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(getTable)) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) tableId = rs.getInt("table_id");
                    }
                }

                // update order -> cancelled
                String upd = "UPDATE orders SET status='cancelled' WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(upd)) {
                    ps.setInt(1, orderId);
                    ps.executeUpdate();
                }

                // free table
                String freeTable = "UPDATE tables SET status='empty' WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(freeTable)) {
                    ps.setInt(1, tableId);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("[OrderDAO] Manual cancel DONE");
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

}

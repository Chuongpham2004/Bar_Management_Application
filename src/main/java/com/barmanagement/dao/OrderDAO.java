package com.barmanagement.dao;

import com.barmanagement.config.DatabaseConnection;
import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // Tạo order kèm danh sách item trong 1 transaction
    public int createOrderWithItems(Order order) throws SQLException {
        String insertOrder = "INSERT INTO orders(table_id, staff_id, total_amount, status) VALUES(?,?,?,?)";
        String insertItem  = "INSERT INTO order_item(order_id, menu_item_id, quantity, price) VALUES(?,?,?,?)";
        String updateTable = "UPDATE `tables` SET status='occupied' WHERE id=?";

        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // 1) tạo order (total tạm thời = 0)
                int orderId;
                try (PreparedStatement ps = c.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, order.getTableId());
                    ps.setInt(2, order.getStaffId());
                    ps.setBigDecimal(3, BigDecimal.ZERO);
                    ps.setString(4, order.getStatus() == null ? "pending" : order.getStatus());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) {
                            orderId = keys.getInt(1);
                            order.setId(orderId);
                        } else {
                            throw new SQLException("Cannot obtain generated order id");
                        }
                    }
                }

                // 2) thêm items
                BigDecimal total = BigDecimal.ZERO;
                if (order.getItems() != null && !order.getItems().isEmpty()) {
                    try (PreparedStatement ps = c.prepareStatement(insertItem)) {
                        for (OrderItem it : order.getItems()) {
                            ps.setInt(1, order.getId());
                            ps.setInt(2, it.getMenuItemId());
                            ps.setInt(3, it.getQuantity());
                            ps.setBigDecimal(4, it.getPrice());
                            ps.addBatch();
                            total = total.add(it.getPrice().multiply(BigDecimal.valueOf(it.getQuantity())));
                        }
                        ps.executeBatch();
                    }
                }

                // 3) cập nhật total
                try (PreparedStatement ps = c.prepareStatement("UPDATE orders SET total_amount=? WHERE id=?")) {
                    ps.setBigDecimal(1, total);
                    ps.setInt(2, order.getId());
                    ps.executeUpdate();
                }
                order.setTotalAmount(total);

                // 4) đổi trạng thái bàn -> occupied
                try (PreparedStatement ps = c.prepareStatement(updateTable)) {
                    ps.setInt(1, order.getTableId());
                    ps.executeUpdate();
                }

                c.commit();
                return order.getId();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public Order findById(int id) throws SQLException {
        String sqlOrder = "SELECT id, table_id, staff_id, total_amount, status, created_at FROM orders WHERE id=?";
        String sqlItems = "SELECT id, order_id, menu_item_id, quantity, price FROM order_item WHERE order_id=?";
        Order o = null;

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlOrder)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setTableId(rs.getInt("table_id"));
                    o.setStaffId(rs.getInt("staff_id"));
                    o.setTotalAmount(rs.getBigDecimal("total_amount"));
                    o.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
                }
            }
            if (o == null) return null;

            try (PreparedStatement psi = c.prepareStatement(sqlItems)) {
                psi.setInt(1, id);
                try (ResultSet rs = psi.executeQuery()) {
                    List<OrderItem> items = new ArrayList<>();
                    while (rs.next()) {
                        OrderItem it = new OrderItem();
                        it.setId(rs.getInt("id"));
                        it.setOrderId(rs.getInt("order_id"));
                        it.setMenuItemId(rs.getInt("menu_item_id"));
                        it.setQuantity(rs.getInt("quantity"));
                        it.setPrice(rs.getBigDecimal("price"));
                        items.add(it);
                    }
                    o.setItems(items);
                }
            }
        }
        return o;
    }

    public List<Order> listByStatus(String status) throws SQLException {
        String sql = "SELECT id, table_id, staff_id, total_amount, status, created_at FROM orders " +
                "WHERE (? IS NULL OR status=?) ORDER BY created_at DESC";
        List<Order> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (status == null) {
                ps.setNull(1, Types.VARCHAR);
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(1, status);
                ps.setString(2, status);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getInt("id"));
                    o.setTableId(rs.getInt("table_id"));
                    o.setStaffId(rs.getInt("staff_id"));
                    o.setTotalAmount(rs.getBigDecimal("total_amount"));
                    o.setStatus(rs.getString("status"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) o.setCreatedAt(ts.toLocalDateTime());
                    list.add(o);
                }
            }
        }
        return list;
    }

    public boolean updateStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status=? WHERE id=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            boolean ok = ps.executeUpdate() > 0;

            // Nếu paid/cancelled -> giải phóng bàn
            if (ok && ("paid".equals(status) || "cancelled".equals(status))) {
                try (PreparedStatement ps2 = c.prepareStatement(
                        "UPDATE `tables` t JOIN orders o ON o.table_id=t.id SET t.status='available' WHERE o.id=?")) {
                    ps2.setInt(1, orderId);
                    ps2.executeUpdate();
                }
            }
            return ok;
        }
    }

    /** Thêm item (đối tượng) rồi cập nhật lại total */
    public boolean addItem(int orderId, OrderItem it) throws SQLException {
        return addItem(orderId, it.getMenuItemId(), it.getQuantity(), it.getPrice());
    }

    /** Overload: thêm item theo 4 tham số (hợp với controller của bạn) */
    public boolean addItem(int orderId, int menuItemId, int qty, BigDecimal price) throws SQLException {
        String sql = "INSERT INTO order_item(order_id, menu_item_id, quantity, price) VALUES(?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);

            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, menuItemId);
                ps.setInt(3, qty);
                ps.setBigDecimal(4, price);
                ps.executeUpdate();
            }

            recomputeTotal(c, orderId);
            c.commit();
            return true;
        }
    }

    /** Xoá 1 order_item và cập nhật lại total của order */
    public boolean removeItem(int orderItemId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            int orderId;

            try (PreparedStatement ps = c.prepareStatement("SELECT order_id FROM order_item WHERE id=?")) {
                ps.setInt(1, orderItemId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { c.rollback(); return false; }
                    orderId = rs.getInt(1);
                }
            }

            try (PreparedStatement ps = c.prepareStatement("DELETE FROM order_item WHERE id=?")) {
                ps.setInt(1, orderItemId);
                ps.executeUpdate();
            }

            recomputeTotal(c, orderId);
            c.commit();
            return true;
        }
    }

    /** Tìm order đang mở của 1 bàn (pending/served) -> trả về id hoặc null */
    public Integer findActiveOrderIdByTable(int tableId) throws SQLException {
        String sql = "SELECT id FROM orders WHERE table_id=? AND status IN ('pending','served') " +
                "ORDER BY created_at DESC LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, tableId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("id");
            }
        }
        return null;
    }

    /** Cập nhật total_amount = SUM(line) */
    private void recomputeTotal(Connection c, int orderId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE orders SET total_amount=(" +
                        "SELECT COALESCE(SUM(price*quantity),0) FROM order_item WHERE order_id=?" +
                        ") WHERE id=?")) {
            ps.setInt(1, orderId);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }
}

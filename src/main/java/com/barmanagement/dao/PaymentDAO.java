package com.barmanagement.dao;

import com.barmanagement.model.Payment;

import java.sql.Connection;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PaymentDAO {
    public void createPayment(int orderId, BigDecimal totalAmount, String paymentMethod) {
        String sql = "INSERT INTO payments (order_id, total_amount, payment_method) VALUES (?, ?, ?)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setBigDecimal(2, totalAmount);
            stmt.setString(3, paymentMethod);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean settleOrderAndCreatePayment(
            int tableId,
            int orderId,
            java.math.BigDecimal totalAmount,
            String paymentMethod
    ) {
        String insertPayment = """
        INSERT INTO payments (order_id, total_amount, payment_method, payment_status, processed_by)
        VALUES (?,?,?,?, 1)
    """;
        // Lưu ý: processed_by=1 minh họa. Nếu bạn có user đăng nhập, truyền đúng user_id vào.

        String updateOrder = """
        UPDATE orders
        SET status='completed',
            total_amount=?,
            completed_time=CURRENT_TIMESTAMP
        WHERE id=? AND status='pending'
    """;

        String freeTable = """
        UPDATE tables
        SET status='empty'
        WHERE id=? AND status<>'empty'
    """;

        try (Connection conn = JDBCConnect.getJDBCConnection()) {
            try {
                conn.setAutoCommit(false);

                // 1) Insert payment
                try (PreparedStatement ps = conn.prepareStatement(insertPayment)) {
                    ps.setInt(1, orderId);
                    ps.setBigDecimal(2, totalAmount);
                    ps.setString(3, paymentMethod);            // 'cash' | 'card' | 'transfer'...
                    ps.setString(4, "completed");               // payment_status
                    ps.executeUpdate();
                }

                // 2) Update order -> completed
                try (PreparedStatement ps = conn.prepareStatement(updateOrder)) {
                    ps.setBigDecimal(1, totalAmount);
                    ps.setInt(2, orderId);
                    ps.executeUpdate();
                }

                // 3) Free table
                try (PreparedStatement ps = conn.prepareStatement(freeTable)) {
                    ps.setInt(1, tableId);
                    ps.executeUpdate();
                }

                // 4) (Tuỳ chọn) cập nhật revenue theo SP của bạn
                //   CALL CompleteOrder(orderId, userId);
                //   Nếu dùng SP, bạn có thể bỏ 1-3 ở trên và chỉ gọi SP.

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertPayment(Payment payment) {
        String sql = "INSERT INTO payments (order_id, total_amount, payment_method, payment_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, payment.getOrderId());
            stmt.setBigDecimal(2, BigDecimal.valueOf(payment.getTotalAmount()));
            stmt.setString(3, payment.getPaymentMethod());
            stmt.setTimestamp(4, payment.getPaymentTime());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}


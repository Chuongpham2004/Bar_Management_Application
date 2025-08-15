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


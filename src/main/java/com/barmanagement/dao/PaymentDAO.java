package com.barmanagement.dao;

import com.barmanagement.model.Payment;
import com.barmanagement.dao.JDBCConnect;

import java.sql.Connection;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DAO class for Payment operations
 * FIXED: All SQL queries use correct column names from database
 */
public class PaymentDAO {

    /**
     * Create payment record - FIXED: uses total_amount column
     */
    public void createPayment(int orderId, BigDecimal totalAmount, String paymentMethod) {
        String sql = "INSERT INTO payments (order_id, total_amount, payment_method) VALUES (?, ?, ?)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setBigDecimal(2, totalAmount);
            stmt.setString(3, paymentMethod);
            stmt.executeUpdate();
            System.out.println("Payment created: Order #" + orderId + ", Amount: " + totalAmount);
        } catch (Exception e) {
            System.err.println("Error creating payment: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Insert payment from Payment object - FIXED: uses total_amount column
     */
    public boolean insertPayment(Payment payment) {
        String sql = "INSERT INTO payments (order_id, total_amount, payment_method, payment_time) VALUES (?, ?, ?, ?)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, payment.getOrderId());
            stmt.setBigDecimal(2, BigDecimal.valueOf(payment.getTotalAmount()));
            stmt.setString(3, payment.getPaymentMethod());
            stmt.setTimestamp(4, payment.getPaymentTime());

            boolean result = stmt.executeUpdate() > 0;
            if (result) {
                System.out.println("Payment inserted successfully: " + payment.getOrderId());
            }
            return result;
        } catch (SQLException e) {
            System.err.println("Error inserting payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * NEW: Check if order has been paid
     */
    public boolean isOrderPaid(int orderId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments WHERE order_id = ?";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * NEW: Get payment by order ID
     */
    public Payment findByOrderId(int orderId) throws SQLException {
        String sql = "SELECT * FROM payments WHERE order_id = ? ORDER BY payment_time DESC LIMIT 1";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Payment payment = new Payment();
                    payment.setId(rs.getInt("id"));
                    payment.setOrderId(rs.getInt("order_id"));
                    payment.setTotalAmount(rs.getDouble("total_amount"));
                    payment.setPaymentTime(rs.getTimestamp("payment_time"));
                    payment.setPaymentMethod(rs.getString("payment_method"));
                    return payment;
                }
            }
        }
        return null;
    }
}
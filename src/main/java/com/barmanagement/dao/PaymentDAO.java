package com.barmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class PaymentDAO {
    public void createPayment(int orderId, double totalAmount, String paymentMethod) {
        String sql = "INSERT INTO payments (order_id, total_amount, payment_method) VALUES (?, ?, ?)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setDouble(2, totalAmount);
            stmt.setString(3, paymentMethod);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


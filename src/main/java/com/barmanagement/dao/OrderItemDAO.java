package com.barmanagement.dao;

import com.barmanagement.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class OrderItemDAO {
    public void addOrderItem(int orderId, int menuItemId, int quantity) {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = JDBCConnect.getJDBCConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, menuItemId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


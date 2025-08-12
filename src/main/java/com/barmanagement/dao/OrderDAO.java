package com.barmanagement.dao;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    
    public int createOrder(Order order) {
        String sql = "INSERT INTO orders (table_id, table_number, order_time, status, total_amount) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, order.getTableId());
            pstmt.setString(2, order.getTableNumber());
            pstmt.setTimestamp(3, Timestamp.valueOf(order.getOrderTime()));
            pstmt.setString(4, order.getStatus());
            pstmt.setDouble(5, order.getTotalAmount());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return -1;
    }
    
    public boolean addOrderItem(OrderItem orderItem) {
        String sql = "INSERT INTO order_items (order_id, menu_item_id, menu_item_name, quantity, unit_price, total_price) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderItem.getOrderId());
            pstmt.setInt(2, orderItem.getMenuItemId());
            pstmt.setString(3, orderItem.getMenuItemName());
            pstmt.setInt(4, orderItem.getQuantity());
            pstmt.setDouble(5, orderItem.getUnitPrice());
            pstmt.setDouble(6, orderItem.getTotalPrice());
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public Order getOrderById(int orderId) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Order order = new Order(
                    rs.getInt("id"),
                    rs.getInt("table_id"),
                    rs.getString("table_number")
                );
                order.setOrderTime(rs.getTimestamp("order_time").toLocalDateTime());
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                
                Timestamp paymentTime = rs.getTimestamp("payment_time");
                if (paymentTime != null) {
                    order.setPaymentTime(paymentTime.toLocalDateTime());
                }
                
                // Load order items
                order.setItems(getOrderItems(orderId));
                
                return order;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    public Order getOrderByTableId(int tableId) {
        String sql = "SELECT * FROM orders WHERE table_id = ? AND status = 'PENDING' ORDER BY order_time DESC LIMIT 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tableId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Order order = new Order(
                    rs.getInt("id"),
                    rs.getInt("table_id"),
                    rs.getString("table_number")
                );
                order.setOrderTime(rs.getTimestamp("order_time").toLocalDateTime());
                order.setStatus(rs.getString("status"));
                order.setTotalAmount(rs.getDouble("total_amount"));
                
                Timestamp paymentTime = rs.getTimestamp("payment_time");
                if (paymentTime != null) {
                    order.setPaymentTime(paymentTime.toLocalDateTime());
                }
                
                // Load order items
                order.setItems(getOrderItems(order.getId()));
                
                return order;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    private List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                OrderItem item = new OrderItem(
                    rs.getInt("order_id"),
                    rs.getInt("menu_item_id"),
                    rs.getString("menu_item_name"),
                    rs.getInt("quantity"),
                    rs.getDouble("unit_price")
                );
                item.setId(rs.getInt("id"));
                item.setTotalPrice(rs.getDouble("total_price"));
                items.add(item);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return items;
    }
    
    public boolean updateOrderTotal(int orderId, double totalAmount) {
        String sql = "UPDATE orders SET total_amount = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, totalAmount);
            pstmt.setInt(2, orderId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean completeOrder(int orderId) {
        String sql = "UPDATE orders SET status = 'PAID', payment_time = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setInt(2, orderId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

package com.example.bar_management_application.dao;

import com.example.bar_management_application.model.Order;
import com.example.bar_management_application.model.Payment;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public double getTotalRevenue(LocalDate start, LocalDate end) {
        double total = 0;
        String sql = "SELECT SUM(amount) FROM payments WHERE DATE(payment_time) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                total = rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    public List<Order> getOrdersInDateRange(LocalDate start, LocalDate end) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE DATE(order_time) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = new Order(
                        rs.getInt("id"),
                        rs.getInt("table_id"),
                        rs.getTimestamp("order_time").toLocalDateTime(),
                        rs.getString("status")
                );
                list.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Payment> getPaymentsInDateRange(LocalDate start, LocalDate end) {
        List<Payment> list = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE DATE(payment_time) BETWEEN ? AND ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(start));
            stmt.setDate(2, Date.valueOf(end));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Payment payment = new Payment(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getDouble("amount"),
                        rs.getString("method"),
                        rs.getTimestamp("payment_time").toLocalDateTime()
                );
                list.add(payment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}


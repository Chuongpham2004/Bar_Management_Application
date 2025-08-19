package com.barmanagement.dao;

import com.barmanagement.dao.JDBCConnect;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class RevenueDAO {

    /**
     * Cập nhật doanh thu theo ngày (được gọi tự động khi thanh toán)
     */
    public void updateDailyRevenue(LocalDate date, BigDecimal amount) throws SQLException {
        String sql = "INSERT INTO revenue (date, total_amount, total_orders) VALUES (?, ?, 1) " +
                "ON DUPLICATE KEY UPDATE " +
                "total_amount = total_amount + VALUES(total_amount), " +
                "total_orders = total_orders + 1";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setBigDecimal(2, amount);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy doanh thu theo ngày
     */
    public BigDecimal getTodayRevenue() throws SQLException {
        String sql = "SELECT COALESCE(total_amount, 0) FROM revenue WHERE date = CURDATE()";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                BigDecimal result = rs.getBigDecimal(1);
                return result != null ? result : BigDecimal.ZERO;
            }
            return BigDecimal.ZERO;
        }
    }

    /**
     * Lấy số đơn hàng hôm nay
     */
    public int getTodayOrders() throws SQLException {
        String sql = "SELECT COALESCE(total_orders, 0) FROM revenue WHERE date = CURDATE()";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    /**
     * Lấy doanh thu 7 ngày gần nhất cho biểu đồ
     */
    public Map<String, BigDecimal> getWeeklyRevenue() throws SQLException {
        String sql = "SELECT date, total_amount FROM revenue " +
                "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "ORDER BY date";

        Map<String, BigDecimal> weeklyData = new LinkedHashMap<>();

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String date = rs.getDate("date").toString();
                BigDecimal amount = rs.getBigDecimal("total_amount");
                weeklyData.put(date, amount != null ? amount : BigDecimal.ZERO);
            }
        }

        return weeklyData;
    }

    /**
     * Lấy số đơn hàng 7 ngày gần nhất cho biểu đồ
     */
    public Map<String, Integer> getWeeklyOrders() throws SQLException {
        String sql = "SELECT date, total_orders FROM revenue " +
                "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                "ORDER BY date";

        Map<String, Integer> weeklyData = new LinkedHashMap<>();

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String date = rs.getDate("date").toString();
                int orders = rs.getInt("total_orders");
                weeklyData.put(date, orders);
            }
        }

        return weeklyData;
    }

    /**
     * Lấy tổng doanh thu trong khoảng thời gian
     */
    public BigDecimal getRevenueByDateRange(LocalDate fromDate, LocalDate toDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(total_amount), 0) as total " +
                "FROM revenue WHERE date BETWEEN ? AND ?";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(fromDate));
            ps.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal result = rs.getBigDecimal("total");
                    return result != null ? result : BigDecimal.ZERO;
                }
                return BigDecimal.ZERO;
            }
        }
    }

    /**
     * Khởi tạo dữ liệu revenue cho ngày hiện tại (nếu chưa có)
     */
    public void initTodayRevenue() throws SQLException {
        String sql = "INSERT IGNORE INTO revenue (date, total_amount, total_orders) " +
                "VALUES (CURDATE(), 0, 0)";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
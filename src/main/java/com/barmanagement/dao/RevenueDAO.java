package com.barmanagement.dao;

import com.barmanagement.dao.JDBCConnect;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Revenue DAO - ENHANCED WITH PAYMENT INTEGRATION
 * Handles all revenue-related database operations with real-time updates
 */
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

            System.out.println("✅ Updated daily revenue: " + date + " - " + amount + " VND");
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
     * ENHANCED: Lấy doanh thu 7 ngày gần nhất cho biểu đồ với đầy đủ dữ liệu
     */
    public Map<String, BigDecimal> getWeeklyRevenue() throws SQLException {
        String sql = "SELECT date, COALESCE(total_amount, 0) as amount " +
                "FROM revenue " +
                "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND date <= CURDATE() " +
                "ORDER BY date";

        Map<String, BigDecimal> weeklyData = new LinkedHashMap<>();

        // Initialize all 7 days with zero
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            weeklyData.put(date.toString(), BigDecimal.ZERO);
        }

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String date = rs.getDate("date").toString();
                BigDecimal amount = rs.getBigDecimal("amount");
                weeklyData.put(date, amount != null ? amount : BigDecimal.ZERO);
            }
        }

        return weeklyData;
    }

    /**
     * ENHANCED: Lấy số đơn hàng 7 ngày gần nhất cho biểu đồ với đầy đủ dữ liệu
     */
    public Map<String, Integer> getWeeklyOrders() throws SQLException {
        String sql = "SELECT date, COALESCE(total_orders, 0) as orders " +
                "FROM revenue " +
                "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND date <= CURDATE() " +
                "ORDER BY date";

        Map<String, Integer> weeklyData = new LinkedHashMap<>();

        // Initialize all 7 days with zero
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            weeklyData.put(date.toString(), 0);
        }

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String date = rs.getDate("date").toString();
                int orders = rs.getInt("orders");
                weeklyData.put(date, orders);
            }
        }

        return weeklyData;
    }

    /**
     * NEW: Lấy doanh thu theo tháng cho báo cáo
     */
    public Map<String, BigDecimal> getMonthlyRevenue(int year) throws SQLException {
        String sql = "SELECT MONTH(date) as month, SUM(total_amount) as total " +
                "FROM revenue " +
                "WHERE YEAR(date) = ? " +
                "GROUP BY MONTH(date) " +
                "ORDER BY month";

        Map<String, BigDecimal> monthlyData = new LinkedHashMap<>();

        // Initialize all 12 months
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        for (String month : months) {
            monthlyData.put(month, BigDecimal.ZERO);
        }

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, year);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int monthNum = rs.getInt("month");
                    BigDecimal total = rs.getBigDecimal("total");
                    if (monthNum >= 1 && monthNum <= 12) {
                        monthlyData.put(months[monthNum - 1], total != null ? total : BigDecimal.ZERO);
                    }
                }
            }
        }

        return monthlyData;
    }

    /**
     * NEW: Lấy top selling items
     */
    public List<Map<String, Object>> getTopSellingItems(int limit) throws SQLException {
        String sql = "SELECT mi.name, SUM(oi.quantity) as total_sold, " +
                "SUM(oi.quantity * oi.price) as total_revenue " +
                "FROM order_items oi " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE o.status IN ('completed', 'paid') " +
                "GROUP BY mi.id, mi.name " +
                "ORDER BY total_sold DESC " +
                "LIMIT ?";

        List<Map<String, Object>> topItems = new ArrayList<>();

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", rs.getString("name"));
                    item.put("quantity", rs.getInt("total_sold"));
                    item.put("revenue", rs.getBigDecimal("total_revenue"));
                    topItems.add(item);
                }
            }
        }

        return topItems;
    }

    /**
     * NEW: Lấy doanh thu theo giờ trong ngày - FIXED để sử dụng bảng payments
     */
    public Map<String, BigDecimal> getHourlyRevenue(LocalDate date) throws SQLException {
        String sql = "SELECT HOUR(p.payment_time) as hour, SUM(p.total_amount) as total " +
                "FROM payments p " +
                "WHERE DATE(p.payment_time) = ? " +
                "GROUP BY HOUR(p.payment_time) " +
                "ORDER BY hour";

        Map<String, BigDecimal> hourlyData = new LinkedHashMap<>();

        // Initialize all 24 hours
        for (int i = 0; i < 24; i++) {
            hourlyData.put(String.format("%02d:00", i), BigDecimal.ZERO);
        }

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int hour = rs.getInt("hour");
                    BigDecimal total = rs.getBigDecimal("total");
                    hourlyData.put(String.format("%02d:00", hour), total != null ? total : BigDecimal.ZERO);
                }
            }
        }

        return hourlyData;
    }

    /**
     * NEW: Lấy thống kê payment methods - FIXED
     */
    public Map<String, Integer> getPaymentMethodStats() throws SQLException {
        String sql = "SELECT payment_method, COUNT(*) as count " +
                "FROM payments " +
                "WHERE DATE(payment_time) = CURDATE() " +
                "GROUP BY payment_method " +
                "ORDER BY count DESC";

        Map<String, Integer> methodStats = new LinkedHashMap<>();

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String method = rs.getString("payment_method");
                int count = rs.getInt("count");
                methodStats.put(method, count);
            }
        }

        return methodStats;
    }

    /**
     * NEW: Lấy average order value - FIXED
     */
    public BigDecimal getAverageOrderValue() throws SQLException {
        String sql = "SELECT AVG(total_amount) as avg_value " +
                "FROM payments " +
                "WHERE DATE(payment_time) = CURDATE()";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                BigDecimal avg = rs.getBigDecimal("avg_value");
                return avg != null ? avg : BigDecimal.ZERO;
            }
            return BigDecimal.ZERO;
        }
    }

    /**
     * NEW: Lấy revenue growth compared to yesterday
     */
    public double getRevenueGrowthPercentage() throws SQLException {
        String sql = "SELECT " +
                "(SELECT COALESCE(total_amount, 0) FROM revenue WHERE date = CURDATE()) as today, " +
                "(SELECT COALESCE(total_amount, 0) FROM revenue WHERE date = DATE_SUB(CURDATE(), INTERVAL 1 DAY)) as yesterday";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                BigDecimal today = rs.getBigDecimal("today");
                BigDecimal yesterday = rs.getBigDecimal("yesterday");

                if (yesterday.compareTo(BigDecimal.ZERO) == 0) {
                    return today.compareTo(BigDecimal.ZERO) > 0 ? 100.0 : 0.0;
                }

                BigDecimal growth = today.subtract(yesterday)
                        .divide(yesterday, 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                return growth.doubleValue();
            }
            return 0.0;
        }
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
     * NEW: Get peak hours analysis - FIXED
     */
    public Map<String, Object> getPeakHoursAnalysis() throws SQLException {
        String sql = "SELECT " +
                "HOUR(p.payment_time) as hour, " +
                "COUNT(*) as order_count, " +
                "SUM(p.total_amount) as total_revenue " +
                "FROM payments p " +
                "WHERE DATE(p.payment_time) = CURDATE() " +
                "GROUP BY HOUR(p.payment_time) " +
                "ORDER BY order_count DESC " +
                "LIMIT 1";

        Map<String, Object> peakData = new LinkedHashMap<>();
        peakData.put("peak_hour", "19:00-20:00"); // Default
        peakData.put("order_count", 0);
        peakData.put("revenue", BigDecimal.ZERO);

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int hour = rs.getInt("hour");
                int orderCount = rs.getInt("order_count");
                BigDecimal revenue = rs.getBigDecimal("total_revenue");

                peakData.put("peak_hour", String.format("%02d:00-%02d:00", hour, hour + 1));
                peakData.put("order_count", orderCount);
                peakData.put("revenue", revenue != null ? revenue : BigDecimal.ZERO);
            }
        }

        return peakData;
    }

    /**
     * NEW: Get table turnover rate
     */
    public double getTableTurnoverRate() throws SQLException {
        String sql = "SELECT " +
                "COUNT(DISTINCT o.id) as total_orders, " +
                "COUNT(DISTINCT o.table_id) as unique_tables " +
                "FROM orders o " +
                "WHERE DATE(o.order_time) = CURDATE() AND o.status IN ('completed', 'paid')";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int totalOrders = rs.getInt("total_orders");
                int uniqueTables = rs.getInt("unique_tables");

                if (uniqueTables > 0) {
                    return (double) totalOrders / uniqueTables;
                }
            }
            return 0.0;
        }
    }

    /**
     * NEW: Get category performance - FIXED
     */
    public List<Map<String, Object>> getCategoryPerformance() throws SQLException {
        String sql = "SELECT " +
                "mi.category, " +
                "SUM(oi.quantity) as total_quantity, " +
                "SUM(oi.quantity * oi.price) as total_revenue, " +
                "COUNT(DISTINCT oi.order_id) as order_count " +
                "FROM order_items oi " +
                "JOIN menu_items mi ON oi.menu_item_id = mi.id " +
                "JOIN orders o ON oi.order_id = o.id " +
                "WHERE DATE(o.order_time) = CURDATE() AND o.status IN ('completed', 'paid') " +
                "GROUP BY mi.category " +
                "ORDER BY total_revenue DESC";

        List<Map<String, Object>> categoryData = new ArrayList<>();

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> category = new LinkedHashMap<>();
                category.put("category", rs.getString("category"));
                category.put("quantity", rs.getInt("total_quantity"));
                category.put("revenue", rs.getBigDecimal("total_revenue"));
                category.put("order_count", rs.getInt("order_count"));
                categoryData.add(category);
            }
        }

        return categoryData;
    }

    /**
     * NEW: Get real-time dashboard summary
     */
    public Map<String, Object> getDashboardSummary() throws SQLException {
        Map<String, Object> summary = new LinkedHashMap<>();

        // Today's basic stats
        summary.put("today_revenue", getTodayRevenue());
        summary.put("today_orders", getTodayOrders());
        summary.put("average_order_value", getAverageOrderValue());
        summary.put("revenue_growth", getRevenueGrowthPercentage());

        // Peak hours
        summary.put("peak_hours", getPeakHoursAnalysis());

        // Table turnover
        summary.put("table_turnover", getTableTurnoverRate());

        // Payment methods
        summary.put("payment_methods", getPaymentMethodStats());

        // Top category
        List<Map<String, Object>> categories = getCategoryPerformance();
        if (!categories.isEmpty()) {
            summary.put("top_category", categories.get(0).get("category"));
        } else {
            summary.put("top_category", "Đồ uống");
        }

        return summary;
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

    /**
     * NEW: Update revenue immediately after payment (for real-time charts)
     */
    public void updateRevenueImmediately(BigDecimal amount) throws SQLException {
        // First ensure today's record exists
        initTodayRevenue();

        // Then update it
        updateDailyRevenue(LocalDate.now(), amount);

        System.out.println("📊 Revenue updated immediately: " + amount + " VND");
    }

    /**
     * NEW: Get recent revenue trend (last 30 days)
     */
    public List<Map<String, Object>> getRecentRevenueTrend() throws SQLException {
        String sql = "SELECT date, total_amount, total_orders " +
                "FROM revenue " +
                "WHERE date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) " +
                "ORDER BY date DESC";

        List<Map<String, Object>> trendData = new ArrayList<>();

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> day = new LinkedHashMap<>();
                day.put("date", rs.getDate("date"));
                day.put("revenue", rs.getBigDecimal("total_amount"));
                day.put("orders", rs.getInt("total_orders"));
                trendData.add(day);
            }
        }

        return trendData;
    }

    /**
     * NEW: Clean old revenue data (keep last 1 year)
     */
    public void cleanOldRevenueData() throws SQLException {
        String sql = "DELETE FROM revenue WHERE date < DATE_SUB(CURDATE(), INTERVAL 1 YEAR)";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int deleted = ps.executeUpdate();
            if (deleted > 0) {
                System.out.println("🧹 Cleaned " + deleted + " old revenue records");
            }
        }
    }

    /**
     * NEW: Get revenue comparison with previous period
     */
    public Map<String, Object> getRevenueComparison() throws SQLException {
        String sql = "SELECT " +
                "(SELECT COALESCE(SUM(total_amount), 0) FROM revenue WHERE date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)) as this_week, " +
                "(SELECT COALESCE(SUM(total_amount), 0) FROM revenue WHERE date >= DATE_SUB(CURDATE(), INTERVAL 14 DAY) AND date < DATE_SUB(CURDATE(), INTERVAL 7 DAY)) as last_week, " +
                "(SELECT COALESCE(SUM(total_amount), 0) FROM revenue WHERE MONTH(date) = MONTH(CURDATE()) AND YEAR(date) = YEAR(CURDATE())) as this_month, " +
                "(SELECT COALESCE(SUM(total_amount), 0) FROM revenue WHERE MONTH(date) = MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)) AND YEAR(date) = YEAR(DATE_SUB(CURDATE(), INTERVAL 1 MONTH))) as last_month";

        Map<String, Object> comparison = new LinkedHashMap<>();

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                BigDecimal thisWeek = rs.getBigDecimal("this_week");
                BigDecimal lastWeek = rs.getBigDecimal("last_week");
                BigDecimal thisMonth = rs.getBigDecimal("this_month");
                BigDecimal lastMonth = rs.getBigDecimal("last_month");

                comparison.put("this_week", thisWeek);
                comparison.put("last_week", lastWeek);
                comparison.put("this_month", thisMonth);
                comparison.put("last_month", lastMonth);

                // Calculate growth percentages
                if (lastWeek.compareTo(BigDecimal.ZERO) > 0) {
                    double weeklyGrowth = thisWeek.subtract(lastWeek)
                            .divide(lastWeek, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    comparison.put("weekly_growth", weeklyGrowth);
                } else {
                    comparison.put("weekly_growth", 0.0);
                }

                if (lastMonth.compareTo(BigDecimal.ZERO) > 0) {
                    double monthlyGrowth = thisMonth.subtract(lastMonth)
                            .divide(lastMonth, 4, BigDecimal.ROUND_HALF_UP)
                            .multiply(BigDecimal.valueOf(100)).doubleValue();
                    comparison.put("monthly_growth", monthlyGrowth);
                } else {
                    comparison.put("monthly_growth", 0.0);
                }
            }
        }

        return comparison;
    }

    /**
     * CRITICAL: Update revenue when a payment is processed - ENHANCED VERSION
     * Called directly from payment processing to ensure real-time dashboard updates
     */
    public void updateRevenueFromPayment(BigDecimal amount) throws SQLException {
        LocalDate today = LocalDate.now();

        // First ensure today's record exists
        initTodayRevenue();

        // Then update with payment amount
        String sql = "UPDATE revenue SET " +
                "total_amount = total_amount + ?, " +
                "total_orders = total_orders + 1, " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE date = CURDATE()";

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBigDecimal(1, amount);
            int updated = ps.executeUpdate();

            if (updated > 0) {
                System.out.println("✅ Revenue updated from payment: " + amount + " VND");
            } else {
                System.out.println("⚠️ WARNING: No revenue record updated for today");
                // Try to create today's record if it doesn't exist
                String insertSql = "INSERT INTO revenue (date, total_amount, total_orders) VALUES (CURDATE(), ?, 1)";
                try (PreparedStatement insertPs = c.prepareStatement(insertSql)) {
                    insertPs.setBigDecimal(1, amount);
                    int inserted = insertPs.executeUpdate();
                    if (inserted > 0) {
                        System.out.println("✅ Created new revenue record for today: " + amount + " VND");
                    }
                }
            }
        }
    }

    /**
     * Real-time revenue update for dashboard notifications - ENHANCED
     */
    public void notifyRevenueUpdate(BigDecimal amount, String paymentMethod) throws SQLException {
        updateRevenueFromPayment(amount);
        System.out.println("📊 Dashboard notified of revenue update: " + amount + " VND via " + paymentMethod);
    }

    /**
     * ENHANCED: Force refresh all dashboard statistics for real-time updates
     */
    public void refreshDashboardStatistics() throws SQLException {
        System.out.println("🔄 Refreshing all dashboard statistics...");

        // Ensure today's revenue record exists
        initTodayRevenue();

        // Log current statistics for debugging
        BigDecimal todayRevenue = getTodayRevenue();
        int todayOrders = getTodayOrders();
        BigDecimal avgOrderValue = getAverageOrderValue();

        System.out.println("📊 Current statistics - Revenue: " + todayRevenue +
                ", Orders: " + todayOrders +
                ", Avg Order: " + avgOrderValue);
    }

    /**
     * Get payment statistics summary for dashboard
     */
    public Map<String, Object> getPaymentStatisticsSummary() throws SQLException {
        Map<String, Object> summary = new LinkedHashMap<>();

        // Basic stats
        summary.put("today_revenue", getTodayRevenue());
        summary.put("today_orders", getTodayOrders());
        summary.put("avg_order_value", getAverageOrderValue());

        // Payment methods breakdown
        summary.put("payment_methods", getPaymentMethodStats());

        // Peak hour
        Map<String, Object> peakData = getPeakHoursAnalysis();
        summary.put("peak_hour", peakData.get("peak_hour"));

        return summary;
    }
}
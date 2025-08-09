package com.barmanagement.dao;

import com.barmanagement.config.DatabaseConnection; // 👈 Import từ config
import com.barmanagement.model.Staff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StaffDAO {
    // Singleton instance
    private static StaffDAO instance;

    private StaffDAO() {
    }

    public static StaffDAO getInstance() {
        if (instance == null) {
            instance = new StaffDAO();
        }
        return instance;
    }

    // Kiểm tra đăng nhập
    public Staff login(String username, String password) {
        String sql = "SELECT * FROM staff WHERE username = ? AND password = ? AND status = TRUE";

        try (Connection conn = DatabaseConnection.getConnection(); // 👈 Gọi static method
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Staff staff = new Staff();
                    staff.setId(rs.getInt("id"));
                    staff.setEmployeeId(rs.getString("employee_id"));
                    staff.setUsername(rs.getString("username"));
                    staff.setFullName(rs.getString("full_name"));
                    staff.setPosition(rs.getString("position"));
                    staff.setRole(rs.getString("role"));
                    staff.setSalary(rs.getDouble("salary"));
                    staff.setPhone(rs.getString("phone"));
                    staff.setEmail(rs.getString("email"));
                    staff.setAddress(rs.getString("address"));
                    staff.setStatus(rs.getBoolean("status"));

                    // Xử lý date/time
                    if (rs.getTimestamp("hire_date") != null) {
                        staff.setHireDate(rs.getTimestamp("hire_date").toLocalDateTime());
                    }
                    if (rs.getTimestamp("last_login") != null) {
                        staff.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());
                    }

                    return staff;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đăng nhập StaffDAO: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
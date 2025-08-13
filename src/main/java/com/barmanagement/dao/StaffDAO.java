package com.barmanagement.dao;

import com.barmanagement.config.DatabaseConnection; // 👈 Import từ config
import com.barmanagement.controller.PasswordUtil;
import com.barmanagement.model.Staff;

import java.sql.*;
import java.time.LocalDateTime;

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

    /**
     * Kiểm tra đăng nhập với mật khẩu đã mã hóa
     */
    public Staff login(String username, String password) {
        // Chỉ lấy thông tin user theo username, không check password trong SQL
        String sql = "SELECT * FROM staff WHERE username = ? AND status = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("password");

                    // Kiểm tra mật khẩu bằng PasswordUtil
                    if (PasswordUtil.verifyPassword(password, storedHashedPassword)) {
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

                        // Cập nhật last_login
                        updateLastLogin(staff.getId());

                        return staff;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đăng nhập StaffDAO: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Cập nhật thời gian đăng nhập cuối cùng
     */
    private void updateLastLogin(int staffId) {
        String sql = "UPDATE staff SET last_login = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, staffId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("⚠️ Không thể cập nhật last_login: " + e.getMessage());
        }
    }

    /**
     * Thêm nhân viên mới với mật khẩu đã mã hóa
     */
    public boolean addStaff(Staff staff, String password) {
        String sql = "INSERT INTO staff (employee_id, username, password, full_name, position, role, salary, phone, email, address, hire_date, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staff.getEmployeeId());
            stmt.setString(2, staff.getUsername());
            stmt.setString(3, PasswordUtil.hashPassword(password)); // Mã hóa password
            stmt.setString(4, staff.getFullName());
            stmt.setString(5, staff.getPosition());
            stmt.setString(6, staff.getRole());
            stmt.setDouble(7, staff.getSalary());
            stmt.setString(8, staff.getPhone());
            stmt.setString(9, staff.getEmail());
            stmt.setString(10, staff.getAddress());

            // Xử lý hire_date
            if (staff.getHireDate() != null) {
                stmt.setTimestamp(11, Timestamp.valueOf(staff.getHireDate()));
            } else {
                stmt.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            }

            stmt.setBoolean(12, staff.isStatus());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi thêm nhân viên: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật thông tin nhân viên (không bao gồm password)
     */
    public boolean updateStaff(Staff staff) {
        String sql = "UPDATE staff SET employee_id = ?, username = ?, full_name = ?, position = ?, role = ?, " +
                "salary = ?, phone = ?, email = ?, address = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, staff.getEmployeeId());
            stmt.setString(2, staff.getUsername());
            stmt.setString(3, staff.getFullName());
            stmt.setString(4, staff.getPosition());
            stmt.setString(5, staff.getRole());
            stmt.setDouble(6, staff.getSalary());
            stmt.setString(7, staff.getPhone());
            stmt.setString(8, staff.getEmail());
            stmt.setString(9, staff.getAddress());
            stmt.setBoolean(10, staff.isStatus());
            stmt.setInt(11, staff.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi cập nhật nhân viên: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Đổi mật khẩu
     */
    public boolean changePassword(int staffId, String oldPassword, String newPassword) {
        // Kiểm tra mật khẩu cũ trước
        String selectSql = "SELECT password FROM staff WHERE id = ?";
        String updateSql = "UPDATE staff SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Kiểm tra mật khẩu cũ
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, staffId);
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    // Xác thực mật khẩu cũ
                    if (!PasswordUtil.verifyPassword(oldPassword, storedPassword)) {
                        System.err.println("❌ Mật khẩu cũ không chính xác");
                        return false;
                    }
                } else {
                    System.err.println("❌ Không tìm thấy nhân viên");
                    return false;
                }
            }

            // Cập nhật mật khẩu mới
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, PasswordUtil.hashPassword(newPassword));
                updateStmt.setInt(2, staffId);
                return updateStmt.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đổi mật khẩu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Reset mật khẩu (dành cho admin)
     */
    public boolean resetPassword(int staffId, String newPassword) {
        String sql = "UPDATE staff SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, PasswordUtil.hashPassword(newPassword));
            stmt.setInt(2, staffId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("❌ Lỗi reset mật khẩu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra username có tồn tại không
     */
    public boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM staff WHERE username = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi kiểm tra username: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Script để mã hóa lại tất cả password hiện có trong database
     * ⚠️ Chạy một lần duy nhất để chuyển đổi password cũ
     */
    public void encryptExistingPasswords() {
        String selectSql = "SELECT id, username, password FROM staff";
        String updateSql = "UPDATE staff SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                ResultSet rs = selectStmt.executeQuery();
                int count = 0;

                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String currentPassword = rs.getString("password");

                    // Kiểm tra xem password đã được mã hóa chưa
                    // Password đã mã hóa sẽ có dạng "salt:hash" (chứa dấu ":")
                    if (!currentPassword.contains(":")) {
                        String hashedPassword = PasswordUtil.hashPassword(currentPassword);

                        updateStmt.setString(1, hashedPassword);
                        updateStmt.setInt(2, id);
                        updateStmt.executeUpdate();

                        count++;
                        System.out.println("✅ Đã mã hóa password cho: " + username + " (ID: " + id + ")");
                    }
                }

                System.out.println("🎉 Hoàn tất! Đã mã hóa " + count + " mật khẩu.");

            }

        } catch (SQLException e) {
            System.err.println("❌ Lỗi mã hóa password: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tìm nhân viên theo ID
     */
    public Staff getStaffById(int id) {
        String sql = "SELECT * FROM staff WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

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
            System.err.println("❌ Lỗi tìm nhân viên: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
}
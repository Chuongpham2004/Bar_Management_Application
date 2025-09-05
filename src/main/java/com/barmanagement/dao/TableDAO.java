package com.barmanagement.dao;

import com.barmanagement.model.Table;
import java.sql.*;
import java.util.*;

public class TableDAO {

    public List<Table> findAll() throws SQLException {
        String sql = "SELECT id, table_name, status FROM tables WHERE COALESCE(status,'') <> 'inactive' ORDER BY id";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Table> list = new ArrayList<>();
            while (rs.next()) {
                Table t = new Table();
                t.setId(rs.getInt("id"));
                t.setTableName(rs.getString("table_name"));
                t.setStatus(rs.getString("status"));
                list.add(t);
            }
            return list;
        }
    }

    // Load all tables including inactive ones (for management screen)
    public List<Table> findAllIncludingInactive() throws SQLException {
        String sql = "SELECT id, table_name, status FROM tables ORDER BY id";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Table> list = new ArrayList<>();
            while (rs.next()) {
                Table t = new Table();
                t.setId(rs.getInt("id"));
                t.setTableName(rs.getString("table_name"));
                t.setStatus(rs.getString("status"));
                list.add(t);
            }
            return list;
        }
    }

    public int insert(Table t) throws SQLException {
        String sql = "INSERT INTO tables(table_name, status) VALUES(?,?)";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTableName());
            ps.setString(2, t.getStatus());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { return k.next() ? k.getInt(1) : 0; }
        }
    }

    public void update(Table t) throws SQLException {
        String sql = "UPDATE tables SET table_name=?, status=? WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getTableName());
            ps.setString(2, t.getStatus());
            ps.setInt(3, t.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection()) {
            // 1) Chặn nếu có đơn TRONG NGÀY
            try (PreparedStatement checkToday = c.prepareStatement(
                    "SELECT EXISTS(SELECT 1 FROM orders WHERE table_id = ? AND DATE(order_time) = CURDATE())")) {
                checkToday.setInt(1, id);
                try (ResultSet rs = checkToday.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 1) {
                        throw new SQLException("Không thể xóa bàn vì trong ngày có đơn hàng.");
                    }
                }
            }

            // 2) Xóa cứng tất cả dữ liệu liên quan trong một transaction
            boolean oldAutoCommit = c.getAutoCommit();
            c.setAutoCommit(false);
            try {
                // Xóa payments theo orders của bàn
                try (PreparedStatement delPayments = c.prepareStatement(
                        "DELETE FROM payments WHERE order_id IN (SELECT id FROM orders WHERE table_id = ?)")) {
                    delPayments.setInt(1, id);
                    delPayments.executeUpdate();
                }

                // Xóa order_items qua join theo orders của bàn
                try (PreparedStatement delItems = c.prepareStatement(
                        "DELETE oi FROM order_items oi INNER JOIN orders o ON oi.order_id = o.id WHERE o.table_id = ?")) {
                    delItems.setInt(1, id);
                    delItems.executeUpdate();
                }

                // Xóa orders của bàn
                try (PreparedStatement delOrders = c.prepareStatement(
                        "DELETE FROM orders WHERE table_id = ?")) {
                    delOrders.setInt(1, id);
                    delOrders.executeUpdate();
                }

                // Xóa chính bàn
                try (PreparedStatement delTable = c.prepareStatement(
                        "DELETE FROM tables WHERE id = ?")) {
                    delTable.setInt(1, id);
                    delTable.executeUpdate();
                }

                c.commit();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(oldAutoCommit);
            }
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE tables SET status=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}

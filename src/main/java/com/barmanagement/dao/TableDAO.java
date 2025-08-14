package com.barmanagement.dao;

import com.barmanagement.model.Table;
import java.sql.*;
import java.util.*;

public class TableDAO {

    public List<Table> findAll() throws SQLException {
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
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tables WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
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

package com.example.bar_management_application.dao;

import com.example.bar_management_application.config.IDBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    public static Connection getConnection() {
        try {
            // Load driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Tạo URL kết nối
            String url = "jdbc:mysql://" + IDBConfig.HOSTNAME + ":" + IDBConfig.PORT + "/" + IDBConfig.DBNAME
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

            Connection conn = DriverManager.getConnection(url, IDBConfig.USERNAME, IDBConfig.PASSWORD);
            System.out.println("✅ MySQL connected successfully!");
            return conn;

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        Connection conn = getConnection();
        System.out.println(conn != null ? "Connected!" : "Failed!");
    }
}


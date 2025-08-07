package com.example.bar_management_application.dao;

import com.example.bar_management_application.config.IDBConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBCConnect {

    public static Connection getJDBCConnection() {
        Connection con = null;
        String connectionUrl = "jdbc:mysql://" + IDBConfig.HOSTNAME
                + ":" + IDBConfig.PORT + "/"
                + IDBConfig.DBNAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver Registered!");
        } catch (ClassNotFoundException ex) {
            System.err.println("Where is your MySQL JDBC Driver?");
            return null;
        }

        try {
            con = DriverManager.getConnection(connectionUrl, IDBConfig.USERNAME, IDBConfig.PASSWORD);
        } catch (SQLException ex) {
            System.err.println("Connection Failed! Check output console");
            ex.printStackTrace();
            return null;
        }
        return con;
    }

    public static void main(String[] args) {
        System.out.println(JDBCConnect.getJDBCConnection());
    }
}


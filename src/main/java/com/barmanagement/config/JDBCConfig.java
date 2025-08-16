package com.barmanagement.config;

public interface JDBCConfig {
    String HOSTNAME = "localhost";
    String PORT = "3306";
    String DBNAME = "bar_management";
    String USERNAME = "root";      // đổi nếu khác
    String PASSWORD = "1234567890";    // đổi nếu khác

    String CONNECTION_URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT + "/" + DBNAME
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
}


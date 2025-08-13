package com.barmanagement.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * Database Connection Configuration
 * Manages MySQL database connections for Bar Management System
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

    // Database configuration
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/bar_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=UTF-8";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "Nghia537991014@";
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private String url;
    private String username;
    private String password;

    // Private constructor for singleton
    private DatabaseConnection() {
        loadConfiguration();
        establishConnection();
    }

    /**
     * Get singleton instance of DatabaseConnection
     */
    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    /**
     * Static method to get connection directly
     */
    public static Connection getConnection() throws SQLException {
        return getInstance().getConnectionInstance();
    }

    /**
     * Load database configuration
     */
    private void loadConfiguration() {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config/database.properties")) {
            if (input != null) {
                props.load(input);
                this.url = props.getProperty("db.url", DEFAULT_URL);
                this.username = props.getProperty("db.username", DEFAULT_USERNAME);
                this.password = props.getProperty("db.password", DEFAULT_PASSWORD);

                System.out.println("✅ Database configuration loaded from properties file");
            } else {
                // Use default configuration if properties file not found
                useDefaultConfiguration();
                System.out.println("⚠️ Properties file not found, using default configuration");
            }
        } catch (IOException e) {
            useDefaultConfiguration();
            System.out.println("⚠️ Error loading properties file: " + e.getMessage());
        }
    }

    /**
     * Use default database configuration
     */
    private void useDefaultConfiguration() {
        this.url = DEFAULT_URL;
        this.username = DEFAULT_USERNAME;
        this.password = DEFAULT_PASSWORD;
    }

    /**
     * Establish database connection
     */
    private void establishConnection() {
        try {
            // Load MySQL JDBC driver
            Class.forName(DRIVER_CLASS);

            // Create connection
            this.connection = DriverManager.getConnection(url, username, password);

            System.out.println("✅ Database connected successfully!");
            System.out.println("📍 URL: " + url);
            System.out.println("👤 User: " + username);

        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found!");
            System.err.println("💡 Add mysql-connector-java dependency to your project");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("🔍 Check:");
            System.err.println("   • MySQL server is running");
            System.err.println("   • Database 'bar_management' exists");
            System.err.println("   • Username/password are correct");
            e.printStackTrace();
        }
    }

    /**
     * Get connection instance
     */
    public Connection getConnectionInstance() throws SQLException {
        // Check if connection is closed or invalid
        if (connection == null || connection.isClosed() || !connection.isValid(5)) {
            establishConnection();
        }
        return connection;
    }

    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            System.err.println("Connection test failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Database connection closed");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    /**
     * Get database connection info
     */
    public String getConnectionInfo() {
        try {
            if (connection != null && !connection.isClosed()) {
                return String.format("Connected to: %s as %s",
                        connection.getMetaData().getURL(),
                        connection.getMetaData().getUserName());
            }
        } catch (SQLException e) {
            return "Error getting connection info: " + e.getMessage();
        }
        return "No active connection";
    }

    /**
     * Create database if not exists
     */
    public static void createDatabaseIfNotExists() {
        String baseUrl = "jdbc:mysql://localhost:3306/?useSSL=false&serverTimezone=UTC";
        String dbName = "bar_management";

        try (Connection conn = DriverManager.getConnection(baseUrl, DEFAULT_USERNAME, DEFAULT_PASSWORD)) {
            String sql = "CREATE DATABASE IF NOT EXISTS " + dbName +
                    " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            conn.createStatement().executeUpdate(sql);
            System.out.println("✅ Database '" + dbName + "' created or already exists");
        } catch (SQLException e) {
            System.err.println("❌ Error creating database: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        System.out.println(DatabaseConnection.getInstance());
    }
}

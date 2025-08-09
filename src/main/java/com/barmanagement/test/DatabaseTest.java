package com.barmanagement.test;

import com.barmanagement.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Test class to verify database connection
 */
public class DatabaseTest {

    public static void main(String[] args) {
        System.out.println("🔄 Testing Database Connection...");
        System.out.println("=====================================");

        testBasicConnection();
        testDatabaseExists();
        testTableExists();
        testSampleQuery();

        System.out.println("=====================================");
        System.out.println("✅ Database test completed!");
    }

    /**
     * Test 1: Basic connection
     */
    private static void testBasicConnection() {
        System.out.println("\n📡 Test 1: Basic Connection");
        System.out.println("-----------------------------");

        try {
            DatabaseConnection dbConn = DatabaseConnection.getInstance();

            if (dbConn.testConnection()) {
                System.out.println("✅ Connection successful!");
                System.out.println("📊 " + dbConn.getConnectionInfo());
            } else {
                System.out.println("❌ Connection failed!");
                return;
            }

        } catch (Exception e) {
            System.out.println("❌ Connection error: " + e.getMessage());
            e.printStackTrace();
            return;
        }
    }

    /**
     * Test 2: Check if database exists
     */
    private static void testDatabaseExists() {
        System.out.println("\n🗄️ Test 2: Database Existence");
        System.out.println("-----------------------------");

        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet catalogs = metaData.getCatalogs()) {
                boolean databaseExists = false;
                System.out.println("📋 Available databases:");

                while (catalogs.next()) {
                    String dbName = catalogs.getString(1);
                    System.out.println("   • " + dbName);

                    if ("bar_management".equals(dbName)) {
                        databaseExists = true;
                    }
                }

                if (databaseExists) {
                    System.out.println("✅ Database 'bar_management' exists!");
                } else {
                    System.out.println("❌ Database 'bar_management' not found!");
                    System.out.println("💡 Run this SQL to create it:");
                    System.out.println("   CREATE DATABASE bar_management;");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error checking database: " + e.getMessage());
        }
    }

    /**
     * Test 3: Check if staff table exists
     */
    private static void testTableExists() {
        System.out.println("\n📋 Test 3: Table Existence");
        System.out.println("-----------------------------");

        try (Connection conn = DatabaseConnection.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            try (ResultSet tables = metaData.getTables("bar_management", null, null, new String[]{"TABLE"})) {
                System.out.println("📊 Available tables:");
                boolean staffTableExists = false;

                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("   • " + tableName);

                    if ("staff".equals(tableName)) {
                        staffTableExists = true;
                    }
                }

                if (staffTableExists) {
                    System.out.println("✅ Table 'staff' exists!");
                } else {
                    System.out.println("❌ Table 'staff' not found!");
                    System.out.println("💡 Run the CREATE TABLE script to create it.");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Error checking tables: " + e.getMessage());
        }
    }

    /**
     * Test 4: Sample query
     */
    private static void testSampleQuery() {
        System.out.println("\n🔍 Test 4: Sample Query");
        System.out.println("-----------------------------");

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // Test query
            String sql = "SELECT COUNT(*) as staff_count FROM staff";

            try (ResultSet rs = stmt.executeQuery(sql)) {
                if (rs.next()) {
                    int count = rs.getInt("staff_count");
                    System.out.println("✅ Query successful!");
                    System.out.println("👥 Total staff records: " + count);

                    if (count == 0) {
                        System.out.println("💡 No staff data found. Insert sample data:");
                        System.out.println("   INSERT INTO staff (username, password, full_name, role) VALUES ('admin', 'admin123', 'Admin User', 'ADMIN');");
                    }
                } else {
                    System.out.println("❌ No results from query");
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Query error: " + e.getMessage());

            if (e.getMessage().contains("Table") && e.getMessage().contains("doesn't exist")) {
                System.out.println("💡 Table doesn't exist. Create it first:");
                printCreateTableSQL();
            }
        }
    }

    /**
     * Print CREATE TABLE SQL
     */
    private static void printCreateTableSQL() {
        System.out.println("\n📝 CREATE TABLE SQL:");
        System.out.println("```sql");
        System.out.println("CREATE TABLE staff (");
        System.out.println("    id INT AUTO_INCREMENT PRIMARY KEY,");
        System.out.println("    employee_id VARCHAR(20) UNIQUE,");
        System.out.println("    username VARCHAR(50) UNIQUE NOT NULL,");
        System.out.println("    password VARCHAR(255) NOT NULL,");
        System.out.println("    full_name VARCHAR(100) NOT NULL,");
        System.out.println("    position VARCHAR(50),");
        System.out.println("    role ENUM('ADMIN', 'MANAGER', 'BARTENDER', 'WAITER') DEFAULT 'WAITER',");
        System.out.println("    salary DOUBLE DEFAULT 0,");
        System.out.println("    phone VARCHAR(15),");
        System.out.println("    email VARCHAR(100),");
        System.out.println("    address TEXT,");
        System.out.println("    status BOOLEAN DEFAULT TRUE,");
        System.out.println("    hire_date DATETIME DEFAULT CURRENT_TIMESTAMP,");
        System.out.println("    last_login DATETIME NULL");
        System.out.println(");");
        System.out.println("```");
    }
}

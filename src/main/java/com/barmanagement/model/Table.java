package com.barmanagement.model;

public class Table {
    private int id;
    private String tableName;
    private String status;

    public Table() {}

    public Table(int id, String tableName, String status) {
        this.id = id;
        this.tableName = tableName;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    /**
     * Get display name for table (e.g., "Bàn 1")
     */
    public String getTableDisplayName() {
        return "Bàn " + id;
    }
}

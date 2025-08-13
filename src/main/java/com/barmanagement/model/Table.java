package com.barmanagement.model;

import java.time.LocalDateTime;

public class Table {
    private int id;
    private int tableNumber;
    private int capacity;
    private String status; // 'available', 'occupied', 'reserved'
    private LocalDateTime createdAt;

    public Table() {}

    public Table(int id, int tableNumber, int capacity, String status, LocalDateTime createdAt) {
        this.id = id;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Table(int tableNumber, int capacity, String status) {
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTableNumber() { return tableNumber; }
    public void setTableNumber(int tableNumber) { this.tableNumber = tableNumber; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override public String toString() {
        return "Table{id=" + id + ", no=" + tableNumber + ", cap=" + capacity + ", status=" + status + "}";
    }
}

package com.example.bar_management_application.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int id;
    private int tableId;
    private LocalDateTime orderTime;
    private String status;
    private List<OrderItem> items;

    public Order() {}

    public Order(int id, int tableId, LocalDateTime orderTime, String status) {
        this.id = id;
        this.tableId = tableId;
        this.orderTime = orderTime;
        this.status = status;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }

    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

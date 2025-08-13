package com.barmanagement.model;

import java.sql.Timestamp;

public class Order {
    private int id;
    private int tableId;
    private Timestamp orderTime;
    private String status;

    public Order() {}

    public Order(int id, int tableId, Timestamp orderTime, String status) {
        this.id = id;
        this.tableId = tableId;
        this.orderTime = orderTime;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }
    public Timestamp getOrderTime() { return orderTime; }
    public void setOrderTime(Timestamp orderTime) { this.orderTime = orderTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

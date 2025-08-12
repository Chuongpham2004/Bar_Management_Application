package com.barmanagement.model;

import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int id;
    private int tableId;
    private String tableNumber;
    private LocalDateTime orderTime;
    private LocalDateTime paymentTime;
    private double totalAmount;
    private String status; // PENDING, PAID, CANCELLED
    private List<OrderItem> items;
    
    public Order() {}
    
    public Order(int id, int tableId, String tableNumber) {
        this.id = id;
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.orderTime = LocalDateTime.now();
        this.status = "PENDING";
        this.totalAmount = 0.0;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getTableId() { return tableId; }
    public void setTableId(int tableId) { this.tableId = tableId; }
    
    public String getTableNumber() { return tableNumber; }
    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    
    public LocalDateTime getOrderTime() { return orderTime; }
    public void setOrderTime(LocalDateTime orderTime) { this.orderTime = orderTime; }
    
    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}

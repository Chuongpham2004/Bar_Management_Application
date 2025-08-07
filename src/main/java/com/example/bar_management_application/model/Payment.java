package com.example.bar_management_application.model;

import java.time.LocalDateTime;

public class Payment {
    private int id;
    private int orderId;
    private double amount;
    private String method; // Cash, Card, E-Wallet
    private LocalDateTime paymentTime;

    public Payment() {}

    public Payment(int id, int orderId, double amount, String method, LocalDateTime paymentTime) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.paymentTime = paymentTime;
    }

    // Getter & Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public LocalDateTime getPaymentTime() { return paymentTime; }
    public void setPaymentTime(LocalDateTime paymentTime) { this.paymentTime = paymentTime; }
}

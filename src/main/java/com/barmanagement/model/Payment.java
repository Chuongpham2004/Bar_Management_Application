package com.barmanagement.model;

import java.sql.Timestamp;

public class Payment {
    private int id;
    private int orderId;
    private double totalAmount;
    private Timestamp paymentTime;
    private String paymentMethod;

    public Payment() {}

    public Payment(int id, int orderId, double totalAmount, Timestamp paymentTime, String paymentMethod) {
        this.id = id;
        this.orderId = orderId;
        this.totalAmount = totalAmount;
        this.paymentTime = paymentTime;
        this.paymentMethod = paymentMethod;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public Timestamp getPaymentTime() { return paymentTime; }
    public void setPaymentTime(Timestamp paymentTime) { this.paymentTime = paymentTime; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}


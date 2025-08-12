package com.barmanagement.model;

import java.math.BigDecimal;

public class OrderItem {
    private int id;
    private int orderId;
    private MenuItem menuItem;
    private int quantity;
    private BigDecimal price; // unit price at the time

    public OrderItem() {}

    public OrderItem(int id, int orderId, MenuItem menuItem, int quantity, BigDecimal price) {
        this.id = id;
        this.orderId = orderId;
        this.menuItem = menuItem;
        this.quantity = quantity;
        this.price = price;
    }

    public int getId() { return id; }
    public int getOrderId() { return orderId; }
    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }

    public void setId(int id) { this.id = id; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    public void setMenuItem(MenuItem menuItem) { this.menuItem = menuItem; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getLineTotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}

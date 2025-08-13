package com.barmanagement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MenuItem {
    private int id;
    private String name;
    private String category; // 'drink' | 'food'
    private BigDecimal price;
    private boolean status;
    private LocalDateTime createdAt;

    public MenuItem() {}

    public MenuItem(int id, String name, String category, BigDecimal price, boolean status, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }

    public MenuItem(String name, String category, BigDecimal price, boolean status) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.status = status;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

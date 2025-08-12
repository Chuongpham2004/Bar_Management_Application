package com.barmanagement.model;

public class MenuItem {
    private int id;
    private String name;
    private String category;   // 'drink' | 'food' (enum trong DB)
    private double price;      // DECIMAL(10,2) map sang double cho đơn giản
    private String description; // DB không có cột này -> để null khi load
    private boolean available;  // map từ cột 'status' trong DB

    public MenuItem() {}

    public MenuItem(int id, String name, String category, double price, String description) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.available = true;
    }

    // constructor tiện dụng khi không quan tâm description
    public MenuItem(int id, String name, String category, double price, boolean available) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.available = available;
        this.description = null;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return name + " (" + price + ")";
    }
}

package com.barmanagement.model;

import javafx.beans.property.*;

public class CartItem {
    private final MenuItem menuItem;
    private final IntegerProperty quantity;

    public CartItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = new SimpleIntegerProperty(quantity);
    }

    public MenuItem getMenuItem() {
        return menuItem;
    }

    public int getQuantity() {
        return quantity.get();
    }

    public void setQuantity(int quantity) {
        this.quantity.set(quantity);
    }

    public IntegerProperty quantityProperty() {
        return quantity;
    }

    public StringProperty menuItemNameProperty() {
        return new SimpleStringProperty(menuItem.getName());
    }

    public DoubleProperty totalPriceProperty() {
        return new SimpleDoubleProperty(menuItem.getPrice() * getQuantity());
    }

    public double getTotalPrice() {
        return menuItem.getPrice() * getQuantity();
    }
}

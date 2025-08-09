package com.barmanagement.dao;

import com.barmanagement.model.MenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MenuItemDAO {

    private final List<MenuItem> menuItems;

    public MenuItemDAO() {
        // Mock dữ liệu menu, sau này thay bằng DB query
        menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(1, "Bia Heineken", "Beer", 30000));
        menuItems.add(new MenuItem(2, "Bia Tiger", "Beer", 25000));
        menuItems.add(new MenuItem(3, "Coca Cola", "Soft Drink", 15000));
        menuItems.add(new MenuItem(4, "Pepsi", "Soft Drink", 15000));
        menuItems.add(new MenuItem(5, "Mojito", "Cocktail", 50000));
        menuItems.add(new MenuItem(6, "Margarita", "Cocktail", 60000));
    }

    // Lấy tất cả món
    public List<MenuItem> getAllMenuItems() {
        return new ArrayList<>(menuItems);
    }

    // Lấy danh sách nhóm món
    public List<String> getAllCategories() {
        return menuItems.stream()
                .map(MenuItem::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    // Lấy món theo nhóm
    public List<MenuItem> getMenuItemsByCategory(String category) {
        return menuItems.stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }
}

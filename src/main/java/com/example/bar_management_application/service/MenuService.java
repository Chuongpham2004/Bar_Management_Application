package com.example.bar_management_application.service;

import com.example.bar_management_application.dao.MenuItemDAO;
import com.example.bar_management_application.model.MenuItem;

import java.util.List;

public class MenuService {

    private final MenuItemDAO menuItemDAO;

    public MenuService() {
        this.menuItemDAO = new MenuItemDAO();
    }

    public List<MenuItem> getAllMenuItems() {
        return menuItemDAO.getAllMenuItems();
    }

    public boolean createMenuItem(MenuItem item) {
        return menuItemDAO.insertMenuItem(item);
    }

    public boolean deleteMenuItem(int id) {
        return menuItemDAO.deleteMenuItem(id);
    }

    public boolean updateMenuItem(MenuItem item) {
        return menuItemDAO.updateMenuItem(item);
    }
}

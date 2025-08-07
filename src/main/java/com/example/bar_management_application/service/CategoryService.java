package com.example.bar_management_application.service;

import com.example.bar_management_application.dao.CategoryDAO;
import com.example.bar_management_application.model.Category;

import java.util.List;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public boolean createCategory(Category category) {
        return categoryDAO.insertCategory(category);
    }

    public boolean deleteCategory(int categoryId) {
        return categoryDAO.deleteCategory(categoryId);
    }
}


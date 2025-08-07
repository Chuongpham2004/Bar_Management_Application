package com.example.bar_management_application.service;

import com.example.bar_management_application.dao.TableDAO;
import com.example.bar_management_application.model.Table;

import java.util.List;

public class TableService {

    private final TableDAO tableDAO;

    public TableService() {
        this.tableDAO = new TableDAO();
    }

    public List<Table> getAllTables() {
        return tableDAO.getAllTables();
    }

    public boolean createTable(Table table) {
        return tableDAO.insertTable(table);
    }

    public boolean deleteTable(int tableId) {
        return tableDAO.deleteTable(tableId);
    }

    public boolean updateTableStatus(int tableId, String status) {
        return tableDAO.updateTableStatus(tableId, status);
    }
}

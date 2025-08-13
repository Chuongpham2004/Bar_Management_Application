package com.barmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class MenuManagementController {

    @FXML
    private TableView<?> menuTable;

    @FXML
    private TableColumn<?, ?> nameColumn;

    @FXML
    private TableColumn<?, ?> priceColumn;

    @FXML
    public void initialize() {
        System.out.println("Load danh sách menu...");
    }

    @FXML
    public void handleAddItem() {
        System.out.println("Thêm món mới...");
    }

    @FXML
    public void handleDeleteItem() {
        System.out.println("Xóa món...");
    }
}

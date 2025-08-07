package com.example.bar_management_application.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MenuController {

    @FXML
    private TableView<MenuData> menuTableView;
    @FXML
    private TableColumn<MenuData, String> colItemId, colItemName, colCategory, colPrice;

    private final ObservableList<MenuData> menuList = FXCollections.observableArrayList();

    public static class MenuData {
        String id, name, category, price;
        public MenuData(String id, String name, String category, String price) {
            this.id = id; this.name = name; this.category = category; this.price = price;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getPrice() { return price; }
    }

    @FXML
    public void initialize() {
        colItemId.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getId()));
        colItemName.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getName()));
        colCategory.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCategory()));
        colPrice.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPrice()));

        menuList.addAll(
                new MenuData("M1", "Beer", "Drinks", "$5"),
                new MenuData("M2", "Burger", "Food", "$10")
        );

        menuTableView.setItems(menuList);
    }
}


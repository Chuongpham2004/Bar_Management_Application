package com.barmanagement.controller;

import com.barmanagement.model.Staff;
import com.barmanagement.model.Table;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

import java.io.IOException;

public class OrderScreenController {

    @FXML
    private ListView<String> categoryListView;
    @FXML
    private FlowPane menuFlowPane;
    @FXML
    private TableView<?> cartTableView;
    @FXML
    private Label tableInfoLabel;
    @FXML
    private Label totalLabel;

    private Staff currentStaff;
    private Table currentTable;

    public void setData(Staff staff, Table table) {
        this.currentStaff = staff;
        this.currentTable = table;
        tableInfoLabel.setText("Bàn: " + table.getName() + " | Nhân viên: " + staff.getFullName());
        loadCategories();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/barmanagement/view/TableScreen.fxml"));
            Scene scene = new Scene(loader.load());

            TableScreenController tableController = loader.getController();
            tableController.setCurrentStaff(currentStaff); // giữ lại nhân viên khi quay lại

            Stage stage = (Stage) tableInfoLabel.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCategories() {
        // TODO: Load category từ DB
        categoryListView.getItems().addAll("Cocktail", "Beer", "Mocktail", "Soft Drink");
    }
}

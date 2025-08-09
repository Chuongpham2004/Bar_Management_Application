package com.barmanagement.controller;

import com.barmanagement.model.Staff;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class TableScreenController {

    @FXML
    private FlowPane tableFlowPane;

    @FXML
    private Label staffNameLabel;

    private List<TableModel> tableList = new ArrayList<>();
    private Staff currentStaff;

    @FXML
    public void initialize() {
        // Tạo dữ liệu mẫu
        tableList.add(new TableModel(1, "Bàn 1", "Trống"));
        tableList.add(new TableModel(2, "Bàn 2", "Đang phục vụ"));
        tableList.add(new TableModel(3, "Bàn 3", "Đã đặt"));

        loadTables();
    }

    /** Nhận nhân viên hiện tại từ DashboardController */
    public void setCurrentStaff(Staff staff) {
        this.currentStaff = staff;
        if (staffNameLabel != null) {
            staffNameLabel.setText("Nhân viên: " + staff.getFullName());
        } else {
            Platform.runLater(() -> staffNameLabel.setText("Nhân viên: " + staff.getFullName()));
        }
    }

    private void loadTables() {
        tableFlowPane.getChildren().clear();

        for (TableModel table : tableList) {
            VBox card = createTableCard(table);
            tableFlowPane.getChildren().add(card);
        }
    }

    private VBox createTableCard(TableModel table) {
        VBox card = new VBox(8);
        card.setPrefSize(120, 120);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: #0f3460; -fx-background-radius: 12; -fx-cursor: hand;");

        Circle statusCircle = new Circle(8);
        switch (table.getStatus()) {
            case "Trống" -> statusCircle.setFill(Color.LIMEGREEN);
            case "Đang phục vụ" -> statusCircle.setFill(Color.ORANGE);
            case "Đã đặt" -> statusCircle.setFill(Color.RED);
            default -> statusCircle.setFill(Color.GRAY);
        }

        Label nameLabel = new Label(table.getName());
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label statusLabel = new Label(table.getStatus());
        statusLabel.setTextFill(Color.LIGHTGRAY);
        statusLabel.setStyle("-fx-font-size: 12px;");

        card.getChildren().addAll(statusCircle, nameLabel, statusLabel);

        // Sự kiện click vào bàn
        card.setOnMouseClicked(e -> openOrderScreen(table));

        return card;
    }

    @FXML
    private void handleAddTable() {
        int newId = tableList.size() + 1;
        tableList.add(new TableModel(newId, "Bàn " + newId, "Trống"));
        loadTables();
    }

    private void openOrderScreen(TableModel table) {
        System.out.println("Mở OrderScreen cho " + table.getName());
        // TODO: Mở màn hình Order, truyền cả currentStaff & table
    }

    /** Model bàn đơn giản */
    public static class TableModel {
        private final int id;
        private final String name;
        private final String status;

        public TableModel(int id, String name, String status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getStatus() { return status; }
    }
}

package com.barmanagement.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class RevenueController {

    @FXML
    private TableView<?> revenueTable;

    @FXML
    private TableColumn<?, ?> dateColumn;

    @FXML
    private TableColumn<?, ?> totalColumn;

    @FXML
    public void initialize() {
        System.out.println("Load báo cáo doanh thu...");
    }
}


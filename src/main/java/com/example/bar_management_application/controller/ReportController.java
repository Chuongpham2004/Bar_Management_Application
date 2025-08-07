package com.example.bar_management_application.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.*;

public class ReportController {

    @FXML
    private BarChart<String, Number> salesChart;
    @FXML
    private CategoryAxis monthAxis;
    @FXML
    private NumberAxis revenueAxis;
    @FXML
    private PieChart categoryPieChart;

    @FXML
    public void initialize() {
        // Dummy data cho BarChart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("2025");
        series.getData().add(new XYChart.Data<>("Jan", 1200));
        series.getData().add(new XYChart.Data<>("Feb", 1500));
        series.getData().add(new XYChart.Data<>("Mar", 1800));

        salesChart.getData().add(series);

        // Dummy data cho PieChart
        categoryPieChart.getData().addAll(
                new PieChart.Data("Drinks", 60),
                new PieChart.Data("Food", 30),
                new PieChart.Data("Others", 10)
        );
    }
}

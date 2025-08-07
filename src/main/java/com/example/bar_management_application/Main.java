package com.example.bar_management_application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import com.example.bar_management_application.dao.DatabaseConnection;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {


        // Load main view
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml"));

        Scene scene = new Scene(root, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setTitle("Bar Management System");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);

        // Set application icon
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
        } catch (Exception e) {
            System.out.println("Could not load application icon");
        }

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

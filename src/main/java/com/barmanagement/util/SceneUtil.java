package com.barmanagement.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Tiện ích chuyển đổi màn hình trong JavaFX
 */
public class SceneUtil {

    /**
     * Mở scene mới từ file FXML
     *
     * @param fxmlPath đường dẫn FXML (bắt đầu từ /, ví dụ: "/fxml/dashboard.fxml")
     * @param anyNode  bất kỳ Node nào trong Scene hiện tại (để lấy Stage)
     */
    public static void openScene(String fxmlPath, Node anyNode) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneUtil.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) anyNode.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package com.barmanagement.util;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class để thông báo cập nhật Dashboard khi có thay đổi dữ liệu
 * Sử dụng Observer pattern đơn giản
 */
public class DashboardUpdateUtil {

    private static final List<Runnable> updateListeners = new ArrayList<>();

    /**
     * Đăng ký listener để nhận thông báo cập nhật
     */
    public static void addUpdateListener(Runnable listener) {
        updateListeners.add(listener);
    }

    /**
     * Bỏ đăng ký listener
     */
    public static void removeUpdateListener(Runnable listener) {
        updateListeners.remove(listener);
    }

    /**
     * Thông báo cho tất cả listeners về việc cần cập nhật Dashboard
     */
    public static void notifyDashboardUpdate() {
        Platform.runLater(() -> {
            for (Runnable listener : updateListeners) {
                try {
                    listener.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Thông báo cập nhật với delay (để tránh spam updates)
     */
    public static void notifyDashboardUpdateWithDelay(long delayMillis) {
        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                notifyDashboardUpdate();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Clear tất cả listeners (thường dùng khi shutdown app)
     */
    public static void clearAllListeners() {
        updateListeners.clear();
    }
}
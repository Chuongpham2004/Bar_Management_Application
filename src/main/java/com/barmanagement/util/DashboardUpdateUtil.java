package com.barmanagement.util;

import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Utility class để thông báo cập nhật Dashboard khi có thay đổi dữ liệu
 * ENHANCED VERSION với thread-safe operations và better error handling
 */
public class DashboardUpdateUtil {

    // Thread-safe list để tránh ConcurrentModificationException
    private static final CopyOnWriteArrayList<Runnable> updateListeners = new CopyOnWriteArrayList<>();

    // Logging flag
    private static final boolean DEBUG_MODE = true;

    /**
     * Đăng ký listener để nhận thông báo cập nhật
     */
    public static void addUpdateListener(Runnable listener) {
        if (listener != null) {
            updateListeners.add(listener);
            if (DEBUG_MODE) {
                System.out.println("📋 Registered dashboard update listener. Total listeners: " + updateListeners.size());
            }
        }
    }

    /**
     * Bỏ đăng ký listener
     */
    public static void removeUpdateListener(Runnable listener) {
        if (listener != null) {
            boolean removed = updateListeners.remove(listener);
            if (DEBUG_MODE && removed) {
                System.out.println("📋 Removed dashboard update listener. Total listeners: " + updateListeners.size());
            }
        }
    }

    /**
     * Thông báo cho tất cả listeners về việc cần cập nhật Dashboard
     * ENHANCED với better error handling
     */
    public static void notifyDashboardUpdate() {
        if (DEBUG_MODE) {
            System.out.println("📊 Broadcasting dashboard update to " + updateListeners.size() + " listeners...");
        }

        Platform.runLater(() -> {
            int successCount = 0;
            int errorCount = 0;

            for (Runnable listener : updateListeners) {
                try {
                    listener.run();
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    if (DEBUG_MODE) {
                        System.err.println("❌ Error in dashboard update listener: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }

            if (DEBUG_MODE) {
                System.out.println("✅ Dashboard update completed - Success: " + successCount + ", Errors: " + errorCount);
            }
        });
    }

    /**
     * Thông báo cập nhật với delay (để tránh spam updates)
     */
    public static void notifyDashboardUpdateWithDelay(long delayMillis) {
        if (DEBUG_MODE) {
            System.out.println("⏰ Scheduling dashboard update with " + delayMillis + "ms delay...");
        }

        new Thread(() -> {
            try {
                Thread.sleep(delayMillis);
                notifyDashboardUpdate();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                if (DEBUG_MODE) {
                    System.err.println("⚠️ Dashboard update delay interrupted");
                }
            }
        }).start();
    }

    /**
     * Thông báo cập nhật với thông tin chi tiết
     */
    public static void notifyDashboardUpdate(String eventType, Object eventData) {
        if (DEBUG_MODE) {
            System.out.println("📊 Dashboard update triggered by: " + eventType + " - Data: " + eventData);
        }
        notifyDashboardUpdate();
    }

    /**
     * Clear tất cả listeners (thường dùng khi shutdown app)
     */
    public static void clearAllListeners() {
        int count = updateListeners.size();
        updateListeners.clear();
        if (DEBUG_MODE) {
            System.out.println("🧹 Cleared all " + count + " dashboard update listeners");
        }
    }

    /**
     * Get số lượng listeners hiện tại
     */
    public static int getListenerCount() {
        return updateListeners.size();
    }

    /**
     * Kiểm tra có listeners nào không
     */
    public static boolean hasListeners() {
        return !updateListeners.isEmpty();
    }

    /**
     * Force immediate update for critical operations
     */
    public static void forceImmediateUpdate() {
        if (DEBUG_MODE) {
            System.out.println("🚨 FORCE IMMEDIATE dashboard update requested!");
        }

        // Run immediately on JavaFX thread
        if (Platform.isFxApplicationThread()) {
            // Already on FX thread, run directly
            for (Runnable listener : updateListeners) {
                try {
                    listener.run();
                } catch (Exception e) {
                    if (DEBUG_MODE) {
                        System.err.println("❌ Error in immediate dashboard update: " + e.getMessage());
                    }
                }
            }
        } else {
            // Not on FX thread, use Platform.runLater
            notifyDashboardUpdate();
        }
    }

    /**
     * Enable/disable debug mode
     */
    public static void setDebugMode(boolean enabled) {
        // Note: DEBUG_MODE is final, so this would require making it non-final
        // For now, debug mode is always enabled
        System.out.println("Debug mode is always enabled for dashboard updates");
    }
}
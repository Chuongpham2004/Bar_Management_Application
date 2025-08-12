package com.barmanagement.util;

import com.barmanagement.model.Staff;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Session Manager - Manages user session and application state
 * Singleton pattern to maintain single session across application
 */
public class SessionManager {

    private static SessionManager instance;
    private Staff currentStaff;
    private LocalDateTime loginTime;
    private Stage primaryStage;
    private Map<String, Object> sessionData;

    // Private constructor for singleton
    private SessionManager() {
        this.sessionData = new HashMap<>();
    }

    /**
     * Get singleton instance
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Login user and create session
     */
    public void login(Staff staff) {
        this.currentStaff = staff;
        this.loginTime = LocalDateTime.now();

        // Store login info in session data
        sessionData.put("login_time", loginTime);
        sessionData.put("user_id", staff.getId());
        sessionData.put("username", staff.getUsername());
        sessionData.put("role", staff.getRole());

        System.out.println("✅ Session created for: " + staff.getFullName());
        System.out.println("🕐 Login time: " + loginTime);
    }

    /**
     * Logout user and clear session
     */
    public void logout() {
        if (currentStaff != null) {
            System.out.println("🔒 Logging out user: " + currentStaff.getFullName());

            // Calculate session duration
            if (loginTime != null) {
                LocalDateTime now = LocalDateTime.now();
                long sessionMinutes = java.time.Duration.between(loginTime, now).toMinutes();
                System.out.println("⏱️ Session duration: " + sessionMinutes + " minutes");
            }
        }

        clearSession();
    }

    /**
     * Clear all session data
     */
    public void clearSession() {
        this.currentStaff = null;
        this.loginTime = null;
        this.sessionData.clear();
        System.out.println("🧹 Session cleared");
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentStaff != null;
    }

    /**
     * Get current logged in staff
     */
    public Staff getCurrentStaff() {
        return currentStaff;
    }

    /**
     * Get login time
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Get session duration in minutes
     */
    public long getSessionDurationMinutes() {
        if (loginTime == null) return 0;
        return java.time.Duration.between(loginTime, LocalDateTime.now()).toMinutes();
    }

    /**
     * Check if current user has specific role
     */
    public boolean hasRole(String role) {
        return currentStaff != null && role.equalsIgnoreCase(currentStaff.getRole());
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }

    /**
     * Check if current user is manager or above
     */
    public boolean isManagerOrAbove() {
        return hasRole("admin") || hasRole("manager");
    }

    /**
     * Set primary stage reference
     */
    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Get primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Store data in session
     */
    public void setSessionData(String key, Object value) {
        sessionData.put(key, value);
    }

    /**
     * Get data from session
     */
    public Object getSessionData(String key) {
        return sessionData.get(key);
    }

    /**
     * Get session data as specific type
     */
    @SuppressWarnings("unchecked")
    public <T> T getSessionData(String key, Class<T> type) {
        Object value = sessionData.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    /**
     * Remove data from session
     */
    public void removeSessionData(String key) {
        sessionData.remove(key);
    }

    /**
     * Get all session data
     */
    public Map<String, Object> getAllSessionData() {
        return new HashMap<>(sessionData);
    }

    /**
     * Get session info as string
     */
    public String getSessionInfo() {
        if (!isLoggedIn()) {
            return "No active session";
        }

        StringBuilder info = new StringBuilder();
        info.append("Session Info:\n");
        info.append("User: ").append(currentStaff.getFullName()).append("\n");
        info.append("Role: ").append(getRoleDisplay()).append("\n");
        info.append("Login: ").append(loginTime).append("\n");
        info.append("Duration: ").append(getSessionDurationMinutes()).append(" minutes\n");
        info.append("Session Data: ").append(sessionData.size()).append(" items");

        return info.toString();
    }

    /**
     * Get role display name
     */
    public String getRoleDisplay() {
        if (currentStaff == null) return "Guest";

        switch (currentStaff.getRole().toLowerCase()) {
            case "admin":
                return "Quản trị viên";
            case "manager":
                return "Quản lý";
            case "staff":
                return "Nhân viên";
            default:
                return currentStaff.getRole();
        }
    }

    /**
     * Validate session (check if still valid)
     */
    public boolean validateSession() {
        if (!isLoggedIn()) {
            return false;
        }

        // Check if session is too old (e.g., 8 hours)
        long sessionHours = java.time.Duration.between(loginTime, LocalDateTime.now()).toHours();
        if (sessionHours > 8) {
            System.out.println("⚠️ Session expired after " + sessionHours + " hours");
            clearSession();
            return false;
        }

        // Check if staff is still active (you could check database here)
        if (currentStaff != null && !currentStaff.isStatus()) {
            System.out.println("⚠️ User account is no longer active");
            clearSession();
            return false;
        }

        return true;
    }

    /**
     * Refresh session (update last activity)
     */
    public void refreshSession() {
        if (isLoggedIn()) {
            setSessionData("last_activity", LocalDateTime.now());
        }
    }

    /**
     * Get current user's display name
     */
    public String getCurrentUserDisplayName() {
        if (currentStaff != null) {
            return currentStaff.getFullName() + " (" + getRoleDisplay() + ")";
        }
        return "Guest";
    }

    /**
     * Get current user ID
     */
    public Integer getCurrentUserId() {
        return currentStaff != null ? currentStaff.getId() : null;
    }

    /**
     * Get current username
     */
    public String getCurrentUsername() {
        return currentStaff != null ? currentStaff.getUsername() : null;
    }

    /**
     * Update current staff info (after profile changes)
     */
    public void updateCurrentStaff(Staff updatedStaff) {
        if (currentStaff != null && updatedStaff != null &&
                currentStaff.getId() == updatedStaff.getId()) {
            this.currentStaff = updatedStaff;
            System.out.println("✅ Current staff info updated");
        }
    }

    /**
     * Check if session is about to expire (within 30 minutes)
     */
    public boolean isSessionNearExpiry() {
        if (!isLoggedIn()) return false;

        long sessionHours = java.time.Duration.between(loginTime, LocalDateTime.now()).toHours();
        return sessionHours >= 7.5; // 7.5 hours = 30 minutes before 8 hour expiry
    }

    /**
     * Get formatted session duration
     */
    public String getFormattedSessionDuration() {
        if (loginTime == null) return "N/A";

        long minutes = getSessionDurationMinutes();
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours > 0) {
            return String.format("%d giờ %d phút", hours, remainingMinutes);
        } else {
            return String.format("%d phút", remainingMinutes);
        }
    }
}

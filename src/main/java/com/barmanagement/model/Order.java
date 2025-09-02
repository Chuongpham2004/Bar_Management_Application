package com.barmanagement.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Order model - ENHANCED VERSION
 * Fixed all formatting and validation methods
 */
public class Order {
    private int id;
    private int tableId;
    private Timestamp orderTime;
    private Timestamp completedTime;
    private String status;
    private String notes;
    private BigDecimal totalAmount;
    private int createdBy;

    // Formatters
    private static final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private static final SimpleDateFormat timeOnlyFormatter = new SimpleDateFormat("HH:mm");

    static {
        currencyFormatter.setMaximumFractionDigits(0);
    }

    // Constructors
    public Order() {
        this.totalAmount = BigDecimal.ZERO;
        this.status = "pending";
        this.orderTime = new Timestamp(System.currentTimeMillis());
        this.createdBy = 1; // Default admin user
    }

    public Order(int id, int tableId, Timestamp orderTime, String status) {
        this.id = id;
        this.tableId = tableId;
        this.orderTime = orderTime;
        this.status = status;
        this.totalAmount = BigDecimal.ZERO;
        this.createdBy = 1;
    }

    public Order(int tableId) {
        this();
        this.tableId = tableId;
    }

    public Order(int tableId, String status) {
        this();
        this.tableId = tableId;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public Timestamp getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(Timestamp orderTime) {
        this.orderTime = orderTime;
    }

    public Timestamp getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Timestamp completedTime) {
        this.completedTime = completedTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    // Utility methods for formatting
    public String getFormattedTotal() {
        return currencyFormatter.format(getTotalAmount()) + " VNĐ";
    }

    public String getFormattedOrderTime() {
        return orderTime != null ? dateTimeFormatter.format(orderTime) : "";
    }

    public String getFormattedOrderTimeShort() {
        return orderTime != null ? timeOnlyFormatter.format(orderTime) : "";
    }

    public String getFormattedCompletedTime() {
        return completedTime != null ? dateTimeFormatter.format(completedTime) : "";
    }

    public String getFormattedCompletedTimeShort() {
        return completedTime != null ? timeOnlyFormatter.format(completedTime) : "";
    }

    // Status display methods
    public String getStatusDisplayName() {
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
                return "Đang chờ";
            case "ordering":
                return "Đang chọn món";
            case "completed":
                return "Hoàn thành";
            case "paid":
                return "Đã thanh toán";
            case "cancelled":
                return "Đã hủy";
            default:
                return status != null ? status : "Không xác định";
        }
    }

    public String getStatusColor() {
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
            case "ordering":
                return "#FF9800"; // Orange
            case "completed":
                return "#4CAF50"; // Green
            case "paid":
                return "#2196F3"; // Blue
            case "cancelled":
                return "#f44336"; // Red
            default:
                return "#9E9E9E"; // Gray
        }
    }

    // Status checking methods
    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isOrdering() {
        return "ordering".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public boolean isPaid() {
        return "paid".equals(status);
    }

    public boolean isCancelled() {
        return "cancelled".equals(status);
    }

    public boolean isActive() {
        return isPending() || isOrdering() || isCompleted();
    }

    public boolean canBeModified() {
        return isPending() || isOrdering();
    }

    public boolean canBeCompleted() {
        return isPending() || isOrdering();
    }

    public boolean canBePaid() {
        return isCompleted();
    }

    // Additional utility methods
    public double getTotalAmountAsDouble() {
        return getTotalAmount().doubleValue();
    }

    public String getTableDisplayName() {
        return "Bàn " + tableId;
    }

    public boolean hasItems() {
        return getTotalAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    public String getOrderSummary() {
        return String.format("Đơn hàng #%d - %s - %s",
                id, getTableDisplayName(), getFormattedTotal());
    }

    public String getDetailedSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Đơn hàng #").append(id).append("\n");
        sb.append("Bàn: ").append(tableId).append("\n");
        sb.append("Thời gian: ").append(getFormattedOrderTime()).append("\n");
        sb.append("Trạng thái: ").append(getStatusDisplayName()).append("\n");
        sb.append("Tổng tiền: ").append(getFormattedTotal());

        if (completedTime != null) {
            sb.append("\nHoàn thành: ").append(getFormattedCompletedTime());
        }

        return sb.toString();
    }

    // Duration calculation methods
    public long getOrderDurationMinutes() {
        if (orderTime != null && completedTime != null) {
            long diffInMillis = completedTime.getTime() - orderTime.getTime();
            return diffInMillis / (60 * 1000); // Convert to minutes
        }
        return 0;
    }

    public long getCurrentDurationMinutes() {
        if (orderTime != null) {
            long currentTime = System.currentTimeMillis();
            long diffInMillis = currentTime - orderTime.getTime();
            return diffInMillis / (60 * 1000);
        }
        return 0;
    }

    public String getFormattedDuration() {
        long minutes = getOrderDurationMinutes();
        return formatDuration(minutes);
    }

    public String getFormattedCurrentDuration() {
        long minutes = getCurrentDurationMinutes();
        return formatDuration(minutes);
    }

    private String formatDuration(long minutes) {
        if (minutes <= 0) return "";

        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours > 0) {
            return String.format("%d giờ %d phút", hours, remainingMinutes);
        } else {
            return String.format("%d phút", remainingMinutes);
        }
    }

    // Priority and urgency methods
    public boolean isUrgent() {
        if (isActive()) {
            long currentDuration = getCurrentDurationMinutes();
            return currentDuration > 30; // Orders older than 30 minutes are urgent
        }
        return false;
    }

    public String getUrgencyLevel() {
        if (!isActive()) {
            return "normal";
        }

        long currentDuration = getCurrentDurationMinutes();

        if (currentDuration > 60) {
            return "critical";
        } else if (currentDuration > 45) {
            return "high";
        } else if (currentDuration > 30) {
            return "medium";
        } else if (currentDuration > 15) {
            return "low";
        }

        return "normal";
    }

    public String getUrgencyDisplayName() {
        switch (getUrgencyLevel()) {
            case "critical": return "Rất khẩn cấp";
            case "high": return "Khẩn cấp";
            case "medium": return "Cần chú ý";
            case "low": return "Bình thường";
            default: return "Không khẩn cấp";
        }
    }

    // Validation methods
    public boolean isValid() {
        return tableId > 0 &&
                status != null && !status.trim().isEmpty() &&
                orderTime != null;
    }

    public String getValidationError() {
        if (tableId <= 0) return "Table ID không hợp lệ";
        if (status == null || status.trim().isEmpty()) return "Trạng thái không được để trống";
        if (orderTime == null) return "Thời gian order không hợp lệ";
        return null;
    }

    // Methods for UI display
    public String getShortDisplay() {
        return "#" + id + " - " + getTableDisplayName();
    }

    public String getTimeDisplay() {
        if (isCompleted() && completedTime != null) {
            return getFormattedCompletedTimeShort();
        } else {
            return getFormattedOrderTimeShort();
        }
    }

    // Comparison methods for sorting
    public int compareByTime(Order other) {
        if (this.orderTime == null && other.orderTime == null) return 0;
        if (this.orderTime == null) return 1;
        if (other.orderTime == null) return -1;
        return other.orderTime.compareTo(this.orderTime); // Most recent first
    }

    public int compareByTable(Order other) {
        return Integer.compare(this.tableId, other.tableId);
    }

    public int compareByStatus(Order other) {
        // Custom order: pending, ordering, completed, paid, cancelled
        int thisOrder = getStatusOrder(this.status);
        int otherOrder = getStatusOrder(other.status);
        return Integer.compare(thisOrder, otherOrder);
    }

    private int getStatusOrder(String status) {
        switch (status != null ? status.toLowerCase() : "") {
            case "pending": return 1;
            case "ordering": return 2;
            case "completed": return 3;
            case "paid": return 4;
            case "cancelled": return 5;
            default: return 6;
        }
    }

    // For display in lists/tables
    @Override
    public String toString() {
        return getShortDisplay() + " - " + getStatusDisplayName() + " - " + getFormattedTotal();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Order order = (Order) obj;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
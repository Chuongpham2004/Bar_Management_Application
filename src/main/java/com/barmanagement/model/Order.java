package com.barmanagement.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Order {
    private int id;
    private int tableId;
    private Timestamp orderTime;
    private Timestamp completedTime;  // THÊM MỚI
    private String status;
    private String notes;            // THÊM MỚI
    private BigDecimal totalAmount;  // THÊM MỚI
    private int createdBy;          // THÊM MỚI

    // Formatters
    private static final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    static {
        currencyFormatter.setMaximumFractionDigits(0);
    }

    // Constructors
    public Order() {
        this.totalAmount = BigDecimal.ZERO;
        this.status = "pending";
        this.orderTime = new Timestamp(System.currentTimeMillis());
    }

    public Order(int id, int tableId, Timestamp orderTime, String status) {
        this.id = id;
        this.tableId = tableId;
        this.orderTime = orderTime;
        this.status = status;
        this.totalAmount = BigDecimal.ZERO;
    }

    public Order(int tableId) {
        this();
        this.tableId = tableId;
    }

    public Order(int tableId, String status) {
        this.tableId = tableId;
        this.status = status;
        this.totalAmount = BigDecimal.ZERO;
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

    // THÊM MỚI - methods bị thiếu
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
        return totalAmount;
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

    // Utility methods
    public String getFormattedTotal() {
        return currencyFormatter.format(totalAmount) + " VNĐ";
    }

    public String getFormattedOrderTime() {
        return orderTime != null ? dateTimeFormatter.format(orderTime) : "";
    }

    public String getFormattedCompletedTime() {
        return completedTime != null ? dateTimeFormatter.format(completedTime) : "";
    }

    public String getStatusDisplayName() {
        switch (status != null ? status.toLowerCase() : "") {
            case "pending":
                return "Đang chờ";
            case "completed":
                return "Hoàn thành";
            case "cancelled":
                return "Đã hủy";
            case "processing":
                return "Đang xử lý";
            default:
                return status != null ? status : "Không xác định";
        }
    }

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isCompleted() {
        return "completed".equals(status);
    }

    public boolean isCancelled() {
        return "cancelled".equals(status);
    }

    // Additional utility methods for controllers
    public double getTotalAmountAsDouble() {
        return totalAmount != null ? totalAmount.doubleValue() : 0.0;
    }

    public String getTableDisplayName() {
        return "Bàn " + tableId;
    }

    public boolean hasItems() {
        return totalAmount != null && totalAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public String getOrderSummary() {
        return String.format("Đơn hàng #%d - %s - %s",
                id, getTableDisplayName(), getFormattedTotal());
    }

    // For duration calculation
    public long getOrderDurationMinutes() {
        if (orderTime != null && completedTime != null) {
            long diffInMillis = completedTime.getTime() - orderTime.getTime();
            return diffInMillis / (60 * 1000); // Convert to minutes
        }
        return 0;
    }

    public String getFormattedDuration() {
        long minutes = getOrderDurationMinutes();
        if (minutes > 0) {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            if (hours > 0) {
                return String.format("%d giờ %d phút", hours, remainingMinutes);
            } else {
                return String.format("%d phút", remainingMinutes);
            }
        }
        return "";
    }

    // Priority and urgency methods
    public boolean isUrgent() {
        if (orderTime != null && "pending".equals(status)) {
            long diffInMillis = System.currentTimeMillis() - orderTime.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);
            return diffInMinutes > 30; // Orders older than 30 minutes are urgent
        }
        return false;
    }

    public String getUrgencyLevel() {
        if (!isPending()) {
            return "normal";
        }

        if (orderTime != null) {
            long diffInMillis = System.currentTimeMillis() - orderTime.getTime();
            long diffInMinutes = diffInMillis / (60 * 1000);

            if (diffInMinutes > 45) {
                return "critical";
            } else if (diffInMinutes > 30) {
                return "high";
            } else if (diffInMinutes > 15) {
                return "medium";
            }
        }
        return "normal";
    }

    // Validation methods
    public boolean isValid() {
        return tableId > 0 && status != null && !status.isEmpty();
    }

    // For display in lists/tables
    @Override
    public String toString() {
        return "Order #" + id + " - Bàn " + tableId + " - " + getFormattedTotal();
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
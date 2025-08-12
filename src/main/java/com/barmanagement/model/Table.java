package com.barmanagement.model;

import java.time.LocalDateTime;

/**
 * Table Model - Updated for Bar Management System
 * Represents dining tables in the bar with complete functionality
 */
public class Table {
    private int id;
    private String tableNumber; // Số bàn (B01, A5, 12, etc.)
    private int capacity; // Sức chứa
    private String status; // available, occupied, reserved, cleaning
    private String location; // MAIN_FLOOR, VIP, OUTDOOR, BAR_COUNTER
    private double positionX; // Vị trí X trong layout
    private double positionY; // Vị trí Y trong layout
    private LocalDateTime occupiedSince; // Thời gian bắt đầu sử dụng
    private LocalDateTime reservationTime; // Thời gian đặt bàn
    private String reservedBy; // Người đặt bàn
    private int currentOrderId; // ID đơn hàng hiện tại
    private double currentBill; // Tổng bill hiện tại
    private int guestCount; // Số khách hiện tại
    private String notes; // Ghi chú
    private boolean isActive; // Bàn có đang hoạt động không
    private LocalDateTime createdAt; // Ngày tạo
    private LocalDateTime updatedAt; // Ngày cập nhật cuối

    // Constructors
    public Table() {
        this.isActive = true;
        this.status = "available";
        this.location = "MAIN_FLOOR";
        this.capacity = 4;
        this.currentOrderId = 0;
        this.currentBill = 0.0;
        this.guestCount = 0;
        this.positionX = 0.0;
        this.positionY = 0.0;
        this.createdAt = LocalDateTime.now();
    }

    public Table(String tableNumber, int capacity, String location) {
        this();
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.location = location;
    }

    public Table(int id, String tableNumber, int capacity, String status, String location) {
        this(tableNumber, capacity, location);
        this.id = id;
        this.status = status;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public LocalDateTime getOccupiedSince() {
        return occupiedSince;
    }

    public void setOccupiedSince(LocalDateTime occupiedSince) {
        this.occupiedSince = occupiedSince;
    }

    public LocalDateTime getReservationTime() {
        return reservationTime;
    }

    public void setReservationTime(LocalDateTime reservationTime) {
        this.reservationTime = reservationTime;
    }

    public String getReservedBy() {
        return reservedBy;
    }

    public void setReservedBy(String reservedBy) {
        this.reservedBy = reservedBy;
    }

    public int getCurrentOrderId() {
        return currentOrderId;
    }

    public void setCurrentOrderId(int currentOrderId) {
        this.currentOrderId = currentOrderId;
    }

    public double getCurrentBill() {
        return currentBill;
    }

    public void setCurrentBill(double currentBill) {
        this.currentBill = currentBill;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Status checking methods
    public boolean isAvailable() {
        return "available".equalsIgnoreCase(status);
    }

    public boolean isOccupied() {
        return "occupied".equalsIgnoreCase(status);
    }

    public boolean isReserved() {
        return "reserved".equalsIgnoreCase(status);
    }

    public boolean isCleaning() {
        return "cleaning".equalsIgnoreCase(status);
    }

    public boolean isOutOfOrder() {
        return !isActive || "out_of_order".equalsIgnoreCase(status);
    }

    // Status display methods
    public String getStatusDisplay() {
        switch (status.toLowerCase()) {
            case "available":
                return "Trống";
            case "occupied":
                return "Đang sử dụng";
            case "reserved":
                return "Đã đặt";
            case "cleaning":
                return "Đang dọn dẹp";
            case "out_of_order":
                return "Tạm ngừng";
            default:
                return status;
        }
    }

    public String getLocationDisplay() {
        switch (location) {
            case "MAIN_FLOOR":
                return "Sảnh chính";
            case "VIP":
                return "Khu VIP";
            case "OUTDOOR":
                return "Ngoài trời";
            case "BAR_COUNTER":
                return "Quầy bar";
            default:
                return location;
        }
    }

    // Duration calculations
    public long getOccupiedDurationMinutes() {
        if (occupiedSince == null) return 0;
        return java.time.Duration.between(occupiedSince, LocalDateTime.now()).toMinutes();
    }

    public long getOccupiedDurationHours() {
        return getOccupiedDurationMinutes() / 60;
    }

    public String getOccupiedDurationFormatted() {
        if (occupiedSince == null) return "N/A";

        long minutes = getOccupiedDurationMinutes();
        long hours = minutes / 60;
        long remainingMinutes = minutes % 60;

        if (hours > 0) {
            return String.format("%d giờ %d phút", hours, remainingMinutes);
        } else {
            return String.format("%d phút", remainingMinutes);
        }
    }

    // Business logic methods
    public boolean isOverdue() {
        return getOccupiedDurationMinutes() > 180; // 3 giờ
    }

    public boolean canBeOccupied() {
        return isActive && (isAvailable() || isCleaning());
    }

    public boolean canBeReserved() {
        return isActive && isAvailable();
    }

    public boolean hasActiveOrder() {
        return currentOrderId > 0;
    }

    public boolean hasBill() {
        return currentBill > 0;
    }

    // Table operations
    public void occupy(int guestCount, String notes) {
        if (!canBeOccupied()) {
            throw new IllegalStateException("Table cannot be occupied in current state: " + status);
        }

        this.status = "occupied";
        this.occupiedSince = LocalDateTime.now();
        this.guestCount = guestCount;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();

        // Clear reservation data if was reserved
        this.reservationTime = null;
        this.reservedBy = null;
    }

    public void reserve(String reservedBy, LocalDateTime reservationTime, String notes) {
        if (!canBeReserved()) {
            throw new IllegalStateException("Table cannot be reserved in current state: " + status);
        }

        this.status = "reserved";
        this.reservedBy = reservedBy;
        this.reservationTime = reservationTime;
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public void free() {
        this.status = "available";
        this.occupiedSince = null;
        this.reservationTime = null;
        this.reservedBy = null;
        this.currentOrderId = 0;
        this.currentBill = 0.0;
        this.guestCount = 0;
        this.notes = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void startCleaning() {
        this.status = "cleaning";
        this.occupiedSince = null;
        this.guestCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public void markOutOfOrder(String reason) {
        this.status = "out_of_order";
        this.isActive = false;
        this.notes = reason;
        this.updatedAt = LocalDateTime.now();

        // Clear all current data
        this.occupiedSince = null;
        this.reservationTime = null;
        this.reservedBy = null;
        this.currentOrderId = 0;
        this.currentBill = 0.0;
        this.guestCount = 0;
    }

    public void activate() {
        this.isActive = true;
        this.status = "available";
        this.updatedAt = LocalDateTime.now();
    }

    // Order management
    public void assignOrder(int orderId, double billAmount) {
        this.currentOrderId = orderId;
        this.currentBill = billAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateBill(double newAmount) {
        this.currentBill = newAmount;
        this.updatedAt = LocalDateTime.now();
    }

    public void clearOrder() {
        this.currentOrderId = 0;
        this.currentBill = 0.0;
        this.updatedAt = LocalDateTime.now();
    }

    // Information methods
    public String getTableInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Bàn ").append(tableNumber);
        info.append(" (").append(capacity).append(" chỗ)");
        info.append(" - ").append(getStatusDisplay());

        if (isOccupied() && guestCount > 0) {
            info.append(" - ").append(guestCount).append(" khách");
        }

        if (isOccupied() && occupiedSince != null) {
            info.append(" - ").append(getOccupiedDurationFormatted());
        }

        if (hasBill()) {
            info.append(" - ").append(String.format("%,.0f VNĐ", currentBill));
        }

        return info.toString();
    }

    public String getDetailedInfo() {
        StringBuilder info = new StringBuilder();
        info.append("=== THÔNG TIN BÀN ===\n");
        info.append("Số bàn: ").append(tableNumber).append("\n");
        info.append("Sức chứa: ").append(capacity).append(" người\n");
        info.append("Vị trí: ").append(getLocationDisplay()).append("\n");
        info.append("Trạng thái: ").append(getStatusDisplay()).append("\n");

        if (isOccupied()) {
            info.append("Số khách: ").append(guestCount).append("\n");
            info.append("Thời gian sử dụng: ").append(getOccupiedDurationFormatted()).append("\n");
        }

        if (isReserved()) {
            info.append("Đặt bởi: ").append(reservedBy).append("\n");
            info.append("Thời gian đặt: ").append(reservationTime).append("\n");
        }

        if (hasActiveOrder()) {
            info.append("Đơn hàng: #").append(currentOrderId).append("\n");
        }

        if (hasBill()) {
            info.append("Tổng bill: ").append(String.format("%,.0f VNĐ", currentBill)).append("\n");
        }

        if (notes != null && !notes.trim().isEmpty()) {
            info.append("Ghi chú: ").append(notes).append("\n");
        }

        return info.toString();
    }

    // CSS class for UI styling
    public String getStatusCssClass() {
        switch (status.toLowerCase()) {
            case "available":
                return "table-available";
            case "occupied":
                return "table-occupied";
            case "reserved":
                return "table-reserved";
            case "cleaning":
                return "table-cleaning";
            case "out_of_order":
                return "table-out-of-order";
            default:
                return "table-unknown";
        }
    }

    // Validation methods
    public boolean isValidCapacity() {
        return capacity > 0 && capacity <= 20;
    }

    public boolean isValidTableNumber() {
        return tableNumber != null && !tableNumber.trim().isEmpty();
    }

    public boolean isValidLocation() {
        return location != null &&
                ("MAIN_FLOOR".equals(location) || "VIP".equals(location) ||
                        "OUTDOOR".equals(location) || "BAR_COUNTER".equals(location));
    }

    public boolean isValid() {
        return isValidTableNumber() && isValidCapacity() && isValidLocation();
    }

    // Comparison and utility methods
    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", tableNumber='" + tableNumber + '\'' +
                ", capacity=" + capacity +
                ", status='" + status + '\'' +
                ", location='" + location + '\'' +
                ", guestCount=" + guestCount +
                ", currentBill=" + currentBill +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Table table = (Table) obj;
        return id == table.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Clone method for creating copies
    public Table copy() {
        Table copy = new Table();
        copy.id = this.id;
        copy.tableNumber = this.tableNumber;
        copy.capacity = this.capacity;
        copy.status = this.status;
        copy.location = this.location;
        copy.positionX = this.positionX;
        copy.positionY = this.positionY;
        copy.occupiedSince = this.occupiedSince;
        copy.reservationTime = this.reservationTime;
        copy.reservedBy = this.reservedBy;
        copy.currentOrderId = this.currentOrderId;
        copy.currentBill = this.currentBill;
        copy.guestCount = this.guestCount;
        copy.notes = this.notes;
        copy.isActive = this.isActive;
        copy.createdAt = this.createdAt;
        copy.updatedAt = this.updatedAt;
        return copy;
    }
}
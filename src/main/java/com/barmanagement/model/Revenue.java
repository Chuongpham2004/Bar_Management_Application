package com.barmanagement.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Revenue {
    private int id;
    private Date date;
    private BigDecimal totalAmount;
    private int totalOrders;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Formatters
    private static final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    static {
        currencyFormatter.setMaximumFractionDigits(0);
    }

    // Constructors
    public Revenue() {
        this.totalAmount = BigDecimal.ZERO;
        this.totalOrders = 0;
    }

    public Revenue(Date date, BigDecimal totalAmount, int totalOrders) {
        this.date = date;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
        this.totalOrders = totalOrders;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public String getFormattedAmount() {
        return currencyFormatter.format(totalAmount) + " VNĐ";
    }

    public String getFormattedDate() {
        return date != null ? dateFormatter.format(date) : "";
    }

    public double getAmountInMillions() {
        return totalAmount.doubleValue() / 1000000.0;
    }

    public double getAmountInThousands() {
        return totalAmount.doubleValue() / 1000.0;
    }

    public BigDecimal getAverageOrderValue() {
        if (totalOrders == 0) {
            return BigDecimal.ZERO;
        }
        return totalAmount.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
    }

    public String getFormattedAverageOrderValue() {
        return currencyFormatter.format(getAverageOrderValue()) + " VNĐ";
    }

    // Business logic methods
    public boolean isHighRevenueDay() {
        return totalAmount.compareTo(BigDecimal.valueOf(1000000)) > 0; // > 1M VND
    }

    public boolean isBusyDay() {
        return totalOrders > 20;
    }

    public String getRevenueCategory() {
        double amount = totalAmount.doubleValue();
        if (amount >= 5000000) return "Xuất sắc";
        if (amount >= 3000000) return "Tốt";
        if (amount >= 1000000) return "Trung bình";
        if (amount > 0) return "Thấp";
        return "Không có doanh thu";
    }

    // For display in charts and tables
    @Override
    public String toString() {
        return getFormattedDate() + " - " + getFormattedAmount() + " (" + totalOrders + " đơn)";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Revenue revenue = (Revenue) obj;
        return id == revenue.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }


}
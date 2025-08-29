package com.barmanagement.model;

import java.text.NumberFormat;
import java.util.Locale;

public class OrderItem {
    private int id;
    private int orderId;
    private int menuItemId;
    private int quantity;
    private double price;                // Đơn giá lấy từ menu_items khi JOIN
    private double subtotal;             // THÊM MỚI - để cache calculation hoặc set từ DAO

    // Thông tin mở rộng từ MenuItem (không lưu DB, chỉ để hiển thị)
    private String menuItemName;
    private String imagePath;
    private String category;
    private String description;

    // Formatter cho tiền tệ
    private static final NumberFormat currencyFormatter = NumberFormat.getInstance(new Locale("vi", "VN"));

    static {
        currencyFormatter.setMaximumFractionDigits(0);
    }

    // Constructors
    public OrderItem() {}

    public OrderItem(int orderId, int menuItemId, int quantity) {
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
    }

    public OrderItem(int id, int orderId, int menuItemId, int quantity, double price) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = price * quantity; // Auto calculate
    }

    // Constructor đầy đủ với thông tin MenuItem
    public OrderItem(int id, int orderId, int menuItemId, int quantity, double price,
                     String menuItemName, String imagePath, String category, String description) {
        this.id = id;
        this.orderId = orderId;
        this.menuItemId = menuItemId;
        this.quantity = quantity;
        this.price = price;
        this.menuItemName = menuItemName;
        this.imagePath = imagePath;
        this.category = category;
        this.description = description;
        this.subtotal = price * quantity; // Auto calculate
    }

    // Basic getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getMenuItemId() {
        return menuItemId;
    }

    public void setMenuItemId(int menuItemId) {
        this.menuItemId = menuItemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        // Tự động update subtotal nếu đã có price
        if (this.price > 0) {
            this.subtotal = this.price * quantity;
        }
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        // Tự động update subtotal nếu đã có quantity
        if (this.quantity > 0) {
            this.subtotal = price * this.quantity;
        }
    }

    // Extended properties getters and setters
    public String getMenuItemName() {
        return menuItemName;
    }

    public void setMenuItemName(String menuItemName) {
        this.menuItemName = menuItemName;
    }

    public String getMenuItemCategory() {
        return category;
    }

    public void setMenuItemCategory(String category) {
        this.category = category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Calculated properties

    /**
     * Tính thành tiền (quantity * price)
     * Ưu tiên subtotal đã được set, nếu không thì tính toán
     */
    public double getSubtotal() {
        // Nếu subtotal đã được set (từ DAO hoặc manual), dùng nó
        if (subtotal > 0) {
            return subtotal;
        }
        // Nếu không, tính toán từ price * quantity
        return price * quantity;
    }

    /**
     * Set subtotal trực tiếp (dùng bởi DAO hoặc manual calculation)
     * THÊM MỚI - Method này cần thiết cho PaymentController và DAO
     */
    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    /**
     * Lấy đường dẫn ảnh đầy đủ
     */
    public String getFullImagePath() {
        if (imagePath == null || imagePath.isEmpty()) {
            return "/images/menu/default.jpg";
        }

        if (imagePath.startsWith("/")) {
            return imagePath;
        }

        return "/images/menu/" + imagePath;
    }

    // Formatted display methods

    /**
     * Format đơn giá với VNĐ
     */
    public String getFormattedPrice() {
        return currencyFormatter.format(price) + " VNĐ";
    }

    /**
     * Format thành tiền với VNĐ
     */
    public String getFormattedSubtotal() {
        return currencyFormatter.format(getSubtotal()) + " VNĐ";
    }

    /**
     * Hiển thị số lượng với format đẹp
     */
    public String getQuantityText() {
        return "x" + quantity;
    }

    /**
     * Hiển thị thông tin ngắn gọn
     */
    public String getSummaryText() {
        return quantity + " x " + (menuItemName != null ? menuItemName : "Unknown Item");
    }

    /**
     * Lấy tên hiển thị (fallback nếu null)
     */
    public String getDisplayName() {
        return menuItemName != null ? menuItemName : "Unknown Item";
    }

    /**
     * Kiểm tra có phải item VIP không (giá > 200k)
     */
    public boolean isVipItem() {
        return price > 200000;
    }

    /**
     * Kiểm tra có phải order lớn không (quantity >= 5)
     */
    public boolean isLargeOrder() {
        return quantity >= 5;
    }

    /**
     * Lấy CSS class cho hiển thị UI
     */
    public String getCssClass() {
        if (isVipItem()) {
            return "vip-item";
        } else if (isLargeOrder()) {
            return "large-order";
        }
        return "normal-item";
    }

    /**
     * Tạo OrderItem từ MenuItem
     */
    public static OrderItem fromMenuItem(MenuItem menuItem, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItemId(menuItem.getId());
        orderItem.setQuantity(quantity);
        orderItem.setPrice(menuItem.getPrice());
        orderItem.setMenuItemName(menuItem.getName());
        orderItem.setImagePath(menuItem.getImagePath());
        orderItem.setCategory(menuItem.getCategory());
        orderItem.setDescription(menuItem.getDescription());
        return orderItem;
    }

    /**
     * Cập nhật thông tin từ MenuItem
     */
    public void updateFromMenuItem(MenuItem menuItem) {
        this.price = menuItem.getPrice();
        this.menuItemName = menuItem.getName();
        this.imagePath = menuItem.getImagePath();
        this.category = menuItem.getCategory();
        this.description = menuItem.getDescription();
        // Update subtotal với giá mới
        this.subtotal = this.price * this.quantity;
    }

    /**
     * Tăng số lượng
     */
    public void increaseQuantity(int amount) {
        this.quantity += amount;
        // Update subtotal
        this.subtotal = this.price * this.quantity;
    }

    /**
     * Giảm số lượng (không cho âm)
     */
    public void decreaseQuantity(int amount) {
        this.quantity = Math.max(0, this.quantity - amount);
        // Update subtotal
        this.subtotal = this.price * this.quantity;
    }

    /**
     * Kiểm tra có hợp lệ không
     */
    public boolean isValid() {
        return menuItemId > 0 && quantity > 0 && price >= 0;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return String.format("OrderItem{id=%d, orderId=%d, menuItemId=%d, quantity=%d, price=%.0f, subtotal=%.0f, name='%s'}",
                id, orderId, menuItemId, quantity, price, subtotal, menuItemName);
    }

    // equals and hashCode for collections
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderItem orderItem = (OrderItem) obj;
        return id == orderItem.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Utility methods for calculations

    /**
     * Tính % discount nếu có (cho future use)
     */
    public double getDiscountAmount(double discountPercent) {
        return getSubtotal() * discountPercent / 100;
    }

    /**
     * Tính thành tiền sau discount
     */
    public double getSubtotalAfterDiscount(double discountPercent) {
        return getSubtotal() - getDiscountAmount(discountPercent);
    }

    /**
     * Tính thuế VAT (10%)
     */
    public double getVATAmount() {
        return getSubtotal() * 0.1;
    }

    /**
     * Tính tổng tiền bao gồm VAT
     */
    public double getTotalWithVAT() {
        return getSubtotal() + getVATAmount();
    }

    // THÊM MỚI: Các utility methods cho business logic

    /**
     * Reset subtotal để force recalculation
     */
    public void resetSubtotal() {
        this.subtotal = 0;
    }

    /**
     * Force recalculate subtotal từ price và quantity hiện tại
     */
    public void recalculateSubtotal() {
        this.subtotal = this.price * this.quantity;
    }

    /**
     * Kiểm tra subtotal có được set manually hay không
     */
    public boolean hasManualSubtotal() {
        return subtotal > 0 && subtotal != (price * quantity);
    }

    /**
     * Lấy giá trị subtotal tính toán (không dùng cache)
     */
    public double getCalculatedSubtotal() {
        return price * quantity;
    }

    /**
     * So sánh subtotal manual vs calculated
     */
    public double getSubtotalDifference() {
        return subtotal - getCalculatedSubtotal();
    }
}
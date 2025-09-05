package com.barmanagement.model;

public class MenuItem {
    private int id;
    private String name;
    private double price;
    private String category;
    private String imagePath;        // Đường dẫn ảnh
    private String description;      // Mô tả món ăn
    private boolean isAvailable;     // Có sẵn hay không
    private int preparationTime;     // Thời gian chuẩn bị (phút)

    // Constructors
    public MenuItem() {
        this.isAvailable = true;     // Mặc định có sẵn
        this.preparationTime = 15;   // Mặc định 15 phút
    }

    public MenuItem(int id, String name, double price, String category) {
        this();
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    public MenuItem(int id, String name, double price, String category, String imagePath, String description) {
        this();
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
        this.description = description;
    }

    // Constructor đầy đủ
    public MenuItem(int id, String name, double price, String category, String imagePath,
                    String description, boolean isAvailable, int preparationTime) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
        this.description = description;
        this.isAvailable = isAvailable;
        this.preparationTime = preparationTime;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getPreparationTime() {
        return preparationTime;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTime = preparationTime;
    }

    // Utility methods

    /**
     * Lấy đường dẫn ảnh đầy đủ cho resources
     */
    public String getFullImagePath() {
        if (imagePath == null || imagePath.isEmpty()) {
            return "/images/menu/default.jpg";
        }

        // Nếu đã có đường dẫn đầy đủ
        if (imagePath.startsWith("/")) {
            return imagePath;
        }

        // Thêm prefix cho relative path
        return "/images/menu/" + imagePath;
    }

    /**
     * Lấy mô tả ngắn (50 ký tự đầu)
     */
    public String getShortDescription() {
        if (description == null || description.isEmpty()) {
            return "Món ngon đặc biệt";
        }

        if (description.length() <= 50) {
            return description;
        }

        return description.substring(0, 47) + "...";
    }

    /**
     * Format giá tiền với VNĐ
     */
    public String getFormattedPrice() {
        return String.format("%,.0f VNĐ", price);
    }

    /**
     * Kiểm tra có phải món VIP không (giá > 200k)
     */
    public boolean isVipItem() {
        return price > 200000;
    }

    /**
     * Lấy status text cho UI
     */
    public String getStatusText() {
        return isAvailable ? "Có sẵn" : "Hết món";
    }

    /**
     * Lấy màu status cho UI
     */
    public String getStatusColor() {
        return isAvailable ? "#4CAF50" : "#f44336";
    }

    // toString method for debugging and ComboBox display
    @Override
    public String toString() {
        return name + " - " + getFormattedPrice();
    }

    // equals and hashCode for collections
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MenuItem menuItem = (MenuItem) obj;
        return id == menuItem.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // Static factory methods for common items

    /**
     * Tạo menu item mặc định với thông tin cơ bản
     */
    public static MenuItem createBasicItem(String name, double price, String category) {
        MenuItem item = new MenuItem();
        item.setName(name);
        item.setPrice(price);
        item.setCategory(category);
        item.setImagePath(generateImageName(name));
        item.setDescription("Món ngon đặc biệt của nhà hàng");
        return item;
    }

    /**
     * Sinh tên file ảnh từ tên món
     */
    private static String generateImageName(String itemName) {
        if (itemName == null) return "default.jpg";

        return itemName.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "") + ".jpg";

    }
}
package com.barmanagement.util;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.MenuItem;
import com.barmanagement.dao.MenuItemDAO;

import java.awt.Desktop;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Universal Invoice Generator - Tạo hóa đơn đa định dạng, hoạt động trên mọi máy
 * Hỗ trợ: TXT, JSON, HTML, CSV
 */
public class UniversalInvoiceGenerator {
    
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final SimpleDateFormat fileDateFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
    
    public enum InvoiceFormat {
        TXT, JSON, HTML, CSV, AUTO
    }
    
    public UniversalInvoiceGenerator() {
        currencyFormatter.setMaximumFractionDigits(0);
    }
    
    /**
     * Tạo hóa đơn với định dạng tự động (phù hợp với hệ điều hành)
     */
    public File generateInvoice(Order order, List<OrderItem> orderItems) {
        InvoiceFormat format = detectBestFormat();
        return generateInvoice(order, orderItems, format);
    }
    
    /**
     * Tạo hóa đơn với định dạng chỉ định
     */
    public File generateInvoice(Order order, List<OrderItem> orderItems, InvoiceFormat format) {
        if (order == null || orderItems == null || orderItems.isEmpty()) {
            System.err.println("❌ Lỗi: Order hoặc OrderItems không hợp lệ");
            return null;
        }
        
        try {
            File invoiceFile = createInvoiceFile(order.getId(), format);
            
            switch (format) {
                case TXT:
                    generateTextInvoice(order, orderItems, invoiceFile);
                    break;
                case JSON:
                    generateJsonInvoice(order, orderItems, invoiceFile);
                    break;
                case HTML:
                    generateHtmlInvoice(order, orderItems, invoiceFile);
                    break;
                case CSV:
                    generateCsvInvoice(order, orderItems, invoiceFile);
                    break;
                default:
                    generateTextInvoice(order, orderItems, invoiceFile);
            }
            
            System.out.println("✅ Đã tạo hóa đơn " + format + ": " + invoiceFile.getAbsolutePath());
            return invoiceFile;
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi tạo hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tạo và mở hóa đơn
     */
    public void generateAndOpenInvoice(Order order, List<OrderItem> orderItems) {
        File invoiceFile = generateInvoice(order, orderItems);
        if (invoiceFile != null) {
            openFile(invoiceFile);
        }
    }
    
    /**
     * Phát hiện định dạng tốt nhất cho hệ điều hành hiện tại
     */
    private InvoiceFormat detectBestFormat() {
        String osName = System.getProperty("os.name").toLowerCase();
        
        if (osName.contains("windows")) {
            return InvoiceFormat.TXT; // Windows có notepad mặc định
        } else if (osName.contains("mac")) {
            return InvoiceFormat.HTML; // macOS có trình duyệt mặc định
        } else if (osName.contains("linux")) {
            return InvoiceFormat.TXT; // Linux có text editor mặc định
        } else {
            return InvoiceFormat.TXT; // Fallback
        }
    }
    
    /**
     * Tạo file hóa đơn
     */
    private File createInvoiceFile(int orderId, InvoiceFormat format) throws IOException {
        File documentsDir = new File(System.getProperty("user.home"), "Documents");
        File barManagerDir = new File(documentsDir, "BarManager");
        File invoicesDir = new File(barManagerDir, "Invoices");
        
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs();
        }
        
        String timestamp = fileDateFormatter.format(new java.util.Date());
        String extension = getFileExtension(format);
        String fileName = String.format("HoaDon_Order%d_%s.%s", orderId, timestamp, extension);
        
        return new File(invoicesDir, fileName);
    }
    
    /**
     * Lấy extension file theo định dạng
     */
    private String getFileExtension(InvoiceFormat format) {
        switch (format) {
            case TXT: return "txt";
            case JSON: return "json";
            case HTML: return "html";
            case CSV: return "csv";
            default: return "txt";
        }
    }
    
    /**
     * Tạo hóa đơn định dạng TEXT
     */
    private void generateTextInvoice(Order order, List<OrderItem> orderItems, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // Header
            writer.println("=".repeat(50));
            writer.println("           BAR MANAGER");
            writer.println("        HOA DON THANH TOAN");
            writer.println("=".repeat(50));
            writer.println();
            
            // Thông tin đơn hàng
            writer.println("Don hang: #" + order.getId());
            writer.println("Ban: " + order.getTableId());
            writer.println("Thoi gian: " + dateFormatter.format(order.getOrderTime()));
            writer.println("Trang thai: " + getStatusText(order.getStatus()));
            writer.println();
            
            // Đường kẻ
            writer.println("-".repeat(50));
            writer.println();
            
            // Chi tiết món ăn
            writer.println("CHI TIET MON AN:");
            writer.println();
            writer.printf("%-25s %6s %12s %12s%n", "Ten mon", "SL", "Don gia", "Thanh tien");
            writer.println("-".repeat(50));
            
            MenuItemDAO menuDAO = new MenuItemDAO();
            double totalAmount = 0;
            
            for (OrderItem item : orderItems) {
                if (item == null) continue;
                
                String itemName = "Mon khong xac dinh";
                
                // Ưu tiên tên từ OrderItem trước (đã được set sẵn)
                if (item.getMenuItemName() != null && !item.getMenuItemName().trim().isEmpty()) {
                    itemName = fixVietnameseEncoding(item.getMenuItemName());
                } else {
                    // Fallback: lấy từ database
                    try {
                        MenuItem menuItem = menuDAO.findById(item.getMenuItemId());
                        if (menuItem != null && menuItem.getName() != null && !menuItem.getName().trim().isEmpty()) {
                            itemName = fixVietnameseEncoding(menuItem.getName());
                        }
                    } catch (Exception e) {
                        System.err.println("Không thể load menu item ID " + item.getMenuItemId() + ": " + e.getMessage());
                    }
                }
                
                writer.printf("%-25s %6d %12s %12s%n", 
                    itemName, 
                    item.getQuantity(),
                    formatCurrency(item.getPrice()),
                    formatCurrency(item.getSubtotal())
                );
                
                totalAmount += item.getSubtotal();
            }
            
            writer.println("-".repeat(50));
            
            // Hiển thị tổng cộng và giảm giá
            if (order.getDiscountPercent() > 0) {
                // Hiển thị tổng cộng (trước giảm giá)
                writer.printf("%-25s %6s %12s %12s%n", "TONG CONG:", "", "", formatCurrency(order.getOriginalAmount().doubleValue()));
                
                // Hiển thị giảm giá
                writer.printf("%-25s %6s %12s %12s%n", 
                    "GIAM GIA (" + String.format("%.0f", order.getDiscountPercent()) + "%):", 
                    "", "", 
                    "-" + formatCurrency(order.getDiscountAmount().doubleValue()));
                writer.println("-".repeat(50));
                writer.printf("%-25s %6s %12s %12s%n", "THANH TOAN:", "", "", formatCurrency(order.getFinalAmount().doubleValue()));
            } else {
                // Không có giảm giá, hiển thị tổng cộng bình thường
                writer.printf("%-25s %6s %12s %12s%n", "TONG CONG:", "", "", formatCurrency(totalAmount));
            }
            writer.println();
            
            // Footer
            writer.println("=".repeat(50));
            writer.println("Cam on quy khach da su dung dich vu!");
            writer.println("Bar Manager - He thong quan ly quan bar");
            writer.println("=".repeat(50));
        }
    }
    
    /**
     * Tạo hóa đơn định dạng JSON
     */
    private void generateJsonInvoice(Order order, List<OrderItem> orderItems, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.println("{");
            writer.println("  \"invoice\": {");
            writer.println("    \"orderId\": " + order.getId() + ",");
            writer.println("    \"tableId\": " + order.getTableId() + ",");
            writer.println("    \"orderTime\": \"" + dateFormatter.format(order.getOrderTime()) + "\",");
            writer.println("    \"status\": \"" + order.getStatus() + "\",");
            writer.println("    \"statusText\": \"" + getStatusText(order.getStatus()) + "\",");
            writer.println("    \"items\": [");
            
            MenuItemDAO menuDAO = new MenuItemDAO();
            double totalAmount = 0;
            
            for (int i = 0; i < orderItems.size(); i++) {
                OrderItem item = orderItems.get(i);
                if (item == null) continue;
                
                String itemName = "Mon khong xac dinh";
                
                // Ưu tiên tên từ OrderItem trước (đã được set sẵn)
                if (item.getMenuItemName() != null && !item.getMenuItemName().trim().isEmpty()) {
                    itemName = fixVietnameseEncoding(item.getMenuItemName());
                } else {
                    // Fallback: lấy từ database
                    try {
                        MenuItem menuItem = menuDAO.findById(item.getMenuItemId());
                        if (menuItem != null && menuItem.getName() != null && !menuItem.getName().trim().isEmpty()) {
                            itemName = fixVietnameseEncoding(menuItem.getName());
                        }
                    } catch (Exception e) {
                        System.err.println("Không thể load menu item ID " + item.getMenuItemId() + ": " + e.getMessage());
                    }
                }
                
                writer.println("      {");
                writer.println("        \"name\": \"" + escapeJson(itemName) + "\",");
                writer.println("        \"quantity\": " + item.getQuantity() + ",");
                writer.println("        \"price\": " + item.getPrice() + ",");
                writer.println("        \"subtotal\": " + item.getSubtotal());
                writer.print("      }");
                
                if (i < orderItems.size() - 1) {
                    writer.println(",");
                } else {
                    writer.println();
                }
                
                totalAmount += item.getSubtotal();
            }
            
            writer.println("    ],");
            if (order.getDiscountPercent() > 0) {
                writer.println("    \"originalAmount\": " + order.getOriginalAmount() + ",");
                writer.println("    \"originalAmountFormatted\": \"" + formatCurrency(order.getOriginalAmount().doubleValue()) + "\",");
                writer.println("    \"discountPercent\": " + order.getDiscountPercent() + ",");
                writer.println("    \"discountAmount\": " + order.getDiscountAmount() + ",");
                writer.println("    \"discountAmountFormatted\": \"" + order.getFormattedDiscountAmount() + "\",");
                writer.println("    \"finalAmount\": " + order.getFinalAmount() + ",");
                writer.println("    \"finalAmountFormatted\": \"" + order.getFormattedFinalAmount() + "\"");
            } else {
                writer.println("    \"totalAmount\": " + totalAmount + ",");
                writer.println("    \"totalAmountFormatted\": \"" + formatCurrency(totalAmount) + "\"");
            }
            writer.println("  }");
            writer.println("}");
        }
    }
    
    /**
     * Tạo hóa đơn định dạng HTML
     */
    private void generateHtmlInvoice(Order order, List<OrderItem> orderItems, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang='vi'>");
            writer.println("<head>");
            writer.println("    <meta charset='UTF-8'>");
            writer.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
            writer.println("    <title>Hóa đơn #" + order.getId() + "</title>");
            writer.println("    <style>");
            writer.println("        body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("        .header { text-align: center; border-bottom: 2px solid #333; padding-bottom: 10px; }");
            writer.println("        .info { margin: 20px 0; }");
            writer.println("        table { width: 100%; border-collapse: collapse; margin: 20px 0; }");
            writer.println("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("        th { background-color: #f2f2f2; }");
            writer.println("        .total { font-weight: bold; font-size: 18px; }");
            writer.println("        .footer { text-align: center; margin-top: 30px; border-top: 1px solid #333; padding-top: 10px; }");
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            
            // Header
            writer.println("    <div class='header'>");
            writer.println("        <h1>BAR MANAGER</h1>");
            writer.println("        <h2>HÓA ĐƠN THANH TOÁN</h2>");
            writer.println("    </div>");
            
            // Thông tin đơn hàng
            writer.println("    <div class='info'>");
            writer.println("        <p><strong>Đơn hàng:</strong> #" + order.getId() + "</p>");
            writer.println("        <p><strong>Bàn:</strong> " + order.getTableId() + "</p>");
            writer.println("        <p><strong>Thời gian:</strong> " + dateFormatter.format(order.getOrderTime()) + "</p>");
            writer.println("        <p><strong>Trạng thái:</strong> " + getStatusText(order.getStatus()) + "</p>");
            writer.println("    </div>");
            
            // Bảng chi tiết
            writer.println("    <table>");
            writer.println("        <thead>");
            writer.println("            <tr>");
            writer.println("                <th>Tên món</th>");
            writer.println("                <th>Số lượng</th>");
            writer.println("                <th>Đơn giá</th>");
            writer.println("                <th>Thành tiền</th>");
            writer.println("            </tr>");
            writer.println("        </thead>");
            writer.println("        <tbody>");
            
            MenuItemDAO menuDAO = new MenuItemDAO();
            double totalAmount = 0;
            
            for (OrderItem item : orderItems) {
                if (item == null) continue;
                
                String itemName = "Món không xác định";
                
                // Ưu tiên tên từ OrderItem trước (đã được set sẵn)
                if (item.getMenuItemName() != null && !item.getMenuItemName().trim().isEmpty()) {
                    itemName = fixVietnameseEncoding(item.getMenuItemName());
                } else {
                    // Fallback: lấy từ database
                    try {
                        MenuItem menuItem = menuDAO.findById(item.getMenuItemId());
                        if (menuItem != null && menuItem.getName() != null && !menuItem.getName().trim().isEmpty()) {
                            itemName = fixVietnameseEncoding(menuItem.getName());
                        }
                    } catch (Exception e) {
                        System.err.println("Không thể load menu item ID " + item.getMenuItemId() + ": " + e.getMessage());
                    }
                }
                
                writer.println("            <tr>");
                writer.println("                <td>" + escapeHtml(itemName) + "</td>");
                writer.println("                <td>" + item.getQuantity() + "</td>");
                writer.println("                <td>" + formatCurrency(item.getPrice()) + "</td>");
                writer.println("                <td>" + formatCurrency(item.getSubtotal()) + "</td>");
                writer.println("            </tr>");
                
                totalAmount += item.getSubtotal();
            }
            
            writer.println("        </tbody>");
            writer.println("        <tfoot>");
            if (order.getDiscountPercent() > 0) {
                writer.println("            <tr class='total'>");
                writer.println("                <td colspan='3'>TỔNG CỘNG:</td>");
                writer.println("                <td>" + formatCurrency(order.getOriginalAmount().doubleValue()) + "</td>");
                writer.println("            </tr>");
                writer.println("            <tr class='discount'>");
                writer.println("                <td colspan='3'>GIẢM GIÁ (" + String.format("%.0f", order.getDiscountPercent()) + "%):</td>");
                writer.println("                <td>-" + order.getFormattedDiscountAmount() + "</td>");
                writer.println("            </tr>");
                writer.println("            <tr class='final-total'>");
                writer.println("                <td colspan='3'>THÀNH TIỀN:</td>");
                writer.println("                <td>" + order.getFormattedFinalAmount() + "</td>");
                writer.println("            </tr>");
            } else {
                writer.println("            <tr class='total'>");
                writer.println("                <td colspan='3'>TỔNG CỘNG:</td>");
                writer.println("                <td>" + formatCurrency(totalAmount) + "</td>");
                writer.println("            </tr>");
            }
            
            // Hiển thị giảm giá nếu có
            if (order.getDiscountPercent() > 0) {
                writer.println("            <tr>");
                writer.println("                <td colspan='3'>GIẢM GIÁ (" + String.format("%.0f", order.getDiscountPercent()) + "%):</td>");
                writer.println("                <td style='color: red;'>-" + formatCurrency(order.getDiscountAmount().doubleValue()) + "</td>");
                writer.println("            </tr>");
                writer.println("            <tr class='total' style='border-top: 2px solid #333;'>");
                writer.println("                <td colspan='3'><strong>THANH TOÁN:</strong></td>");
                writer.println("                <td><strong>" + formatCurrency(order.getFinalAmount().doubleValue()) + "</strong></td>");
                writer.println("            </tr>");
            }
            
            writer.println("        </tfoot>");
            writer.println("    </table>");
            
            // Footer
            writer.println("    <div class='footer'>");
            writer.println("        <p>Cảm ơn quý khách đã sử dụng dịch vụ!</p>");
            writer.println("        <p>Bar Manager - Hệ thống quản lý quán bar</p>");
            writer.println("    </div>");
            
            writer.println("</body>");
            writer.println("</html>");
        }
    }
    
    /**
     * Tạo hóa đơn định dạng CSV
     */
    private void generateCsvInvoice(Order order, List<OrderItem> orderItems, File file) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            // Header
            writer.println("BAR MANAGER - HOA DON THANH TOAN");
            writer.println("Don hang,#" + order.getId());
            writer.println("Ban," + order.getTableId());
            writer.println("Thoi gian," + dateFormatter.format(order.getOrderTime()));
            writer.println("Trang thai," + getStatusText(order.getStatus()));
            writer.println();
            
            // Chi tiết món ăn
            writer.println("Ten mon,So luong,Don gia,Thanh tien");
            
            MenuItemDAO menuDAO = new MenuItemDAO();
            double totalAmount = 0;
            
            for (OrderItem item : orderItems) {
                if (item == null) continue;
                
                String itemName = "Mon khong xac dinh";
                
                // Ưu tiên tên từ OrderItem trước (đã được set sẵn)
                if (item.getMenuItemName() != null && !item.getMenuItemName().trim().isEmpty()) {
                    itemName = fixVietnameseEncoding(item.getMenuItemName());
                } else {
                    // Fallback: lấy từ database
                    try {
                        MenuItem menuItem = menuDAO.findById(item.getMenuItemId());
                        if (menuItem != null && menuItem.getName() != null && !menuItem.getName().trim().isEmpty()) {
                            itemName = fixVietnameseEncoding(menuItem.getName());
                        }
                    } catch (Exception e) {
                        System.err.println("Không thể load menu item ID " + item.getMenuItemId() + ": " + e.getMessage());
                    }
                }
                
                writer.println("\"" + itemName + "\"," + 
                             item.getQuantity() + "," + 
                             item.getPrice() + "," + 
                             item.getSubtotal());
                
                totalAmount += item.getSubtotal();
            }
            
            writer.println();
            if (order.getDiscountPercent() > 0) {
                writer.println("TONG CONG,," + order.getOriginalAmount() + "," + order.getOriginalAmount());
                writer.println("GIAM GIA (" + String.format("%.0f", order.getDiscountPercent()) + "%),," + order.getDiscountAmount() + "," + order.getDiscountAmount());
                writer.println("THANH TOAN,," + order.getFinalAmount() + "," + order.getFinalAmount());
            } else {
                writer.println("TONG CONG,," + totalAmount + "," + totalAmount);
            }
        }
    }
    
    /**
     * Mở file với ứng dụng mặc định
     */
    private void openFile(File file) {
        boolean opened = false;
        
        // Phương pháp 1: Desktop API
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(file);
                    opened = true;
                }
            } catch (Exception e) {
                System.err.println("Không thể mở file bằng Desktop API: " + e.getMessage());
            }
        }
        
        // Phương pháp 2: Runtime exec
        if (!opened) {
            String osName = System.getProperty("os.name").toLowerCase();
            try {
                if (osName.contains("windows")) {
                    Runtime.getRuntime().exec("cmd /c start \"\" \"" + file.getAbsolutePath() + "\"");
                } else if (osName.contains("mac")) {
                    Runtime.getRuntime().exec("open \"" + file.getAbsolutePath() + "\"");
                } else if (osName.contains("linux")) {
                    Runtime.getRuntime().exec("xdg-open \"" + file.getAbsolutePath() + "\"");
                }
                opened = true;
            } catch (Exception e) {
                System.err.println("Không thể mở file bằng command: " + e.getMessage());
            }
        }
        
        if (!opened) {
            System.out.println("📁 Vui lòng mở file thủ công: " + file.getAbsolutePath());
        }
    }
    
    /**
     * Escape JSON string
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                 .replace("\\", "\\\\")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }
    
    /**
     * Escape HTML string
     */
    private String escapeHtml(String str) {
        if (str == null) return "";
        return str.replace("&", "&amp;")
                 .replace("<", "&lt;")
                 .replace(">", "&gt;")
                 .replace("\"", "&quot;")
                 .replace("'", "&#39;");
    }
    
    /**
     * Format tiền tệ
     */
    private String formatCurrency(double amount) {
        return currencyFormatter.format(amount) + " VNĐ";
    }
    
    /**
     * Sửa lỗi encoding tiếng Việt
     */
    private String fixVietnameseEncoding(String text) {
        if (text == null) return "";
        
        // Sửa các ký tự tiếng Việt bị lỗi phổ biến
        return text
            .replace("ty", "tây")  // Khoai ty -> Khoai tây
            .replace("chin", "chiên")  // chin -> chiên
            .replace("ng", "ng")  // Giữ nguyên
            .replace("uong", "ương")  // uong -> ương
            .replace("ong", "ông")  // ong -> ông
            .replace("ang", "ăng")  // ang -> ăng
            .replace("ung", "ưng")  // ung -> ưng
            .replace("inh", "ình")  // inh -> ình
            .replace("anh", "ành")  // anh -> ành
            .replace("ong", "ông")  // ong -> ông
            .replace("ung", "ưng")  // ung -> ưng
            .replace("inh", "ình")  // inh -> ình
            .replace("anh", "ành");  // anh -> ành
    }
    
    /**
     * Chuyển đổi trạng thái order
     */
    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "Chờ xử lý";
            case "completed": return "Đã hoàn thành";
            case "paid": return "Đã thanh toán";
            default: return status;
        }
    }
}

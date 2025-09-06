package com.barmanagement.util;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import java.io.File;
import java.util.List;

/**
 * Invoice Helper - Class tiện ích để tạo hóa đơn dễ dàng
 */
public class InvoiceHelper {
    
    private static final UniversalInvoiceGenerator generator = new UniversalInvoiceGenerator();
    
    /**
     * Tạo hóa đơn với định dạng tự động (khuyến nghị)
     */
    public static File createInvoice(Order order, List<OrderItem> orderItems) {
        return generator.generateInvoice(order, orderItems);
    }
    
    /**
     * Tạo hóa đơn với định dạng chỉ định
     */
    public static File createInvoice(Order order, List<OrderItem> orderItems, UniversalInvoiceGenerator.InvoiceFormat format) {
        return generator.generateInvoice(order, orderItems, format);
    }
    
    /**
     * Tạo và mở hóa đơn
     */
    public static void createAndOpenInvoice(Order order, List<OrderItem> orderItems) {
        generator.generateAndOpenInvoice(order, orderItems);
    }
    
    /**
     * Tạo hóa đơn TEXT (hoạt động trên mọi máy)
     */
    public static File createTextInvoice(Order order, List<OrderItem> orderItems) {
        return generator.generateInvoice(order, orderItems, UniversalInvoiceGenerator.InvoiceFormat.TXT);
    }
    
    /**
     * Tạo hóa đơn JSON (dễ đọc bằng code)
     */
    public static File createJsonInvoice(Order order, List<OrderItem> orderItems) {
        return generator.generateInvoice(order, orderItems, UniversalInvoiceGenerator.InvoiceFormat.JSON);
    }
    
    /**
     * Tạo hóa đơn HTML (đẹp, mở bằng trình duyệt)
     */
    public static File createHtmlInvoice(Order order, List<OrderItem> orderItems) {
        return generator.generateInvoice(order, orderItems, UniversalInvoiceGenerator.InvoiceFormat.HTML);
    }
    
    /**
     * Tạo hóa đơn CSV (mở bằng Excel)
     */
    public static File createCsvInvoice(Order order, List<OrderItem> orderItems) {
        return generator.generateInvoice(order, orderItems, UniversalInvoiceGenerator.InvoiceFormat.CSV);
    }
    
    /**
     * Kiểm tra xem có thể tạo hóa đơn không
     */
    public static boolean canCreateInvoice(Order order, List<OrderItem> orderItems) {
        return order != null && orderItems != null && !orderItems.isEmpty();
    }
    
    /**
     * Lấy thông báo lỗi nếu không thể tạo hóa đơn
     */
    public static String getErrorMessage(Order order, List<OrderItem> orderItems) {
        if (order == null) {
            return "Order không hợp lệ";
        }
        if (orderItems == null) {
            return "Danh sách món ăn không hợp lệ";
        }
        if (orderItems.isEmpty()) {
            return "Đơn hàng không có món ăn nào";
        }
        return null;
    }
}

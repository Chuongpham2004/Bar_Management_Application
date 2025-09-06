package com.barmanagement.util;

import com.barmanagement.model.Order;
import com.barmanagement.model.OrderItem;
import com.barmanagement.model.MenuItem;
import com.barmanagement.dao.MenuItemDAO;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Invoice Generator - Tạo PDF hóa đơn đẹp với font chữ tiếng Việt
 */
public class InvoiceGenerator {
    
    private final NumberFormat currencyFormatter = NumberFormat.getInstance(Locale.forLanguageTag("vi-VN"));
    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    
    public InvoiceGenerator() {
        currencyFormatter.setMaximumFractionDigits(0);
    }
    
    /**
     * Tạo file PDF hóa đơn mà không tự động mở
     */
    public File generateInvoice(Order order, List<OrderItem> orderItems) {
        if (order == null || orderItems == null || orderItems.isEmpty()) {
            System.err.println("❌ Lỗi: Order hoặc OrderItems không hợp lệ");
            return null;
        }
        
        try {
            // Tạo file PDF với tên cố định trong thư mục Documents
            File invoiceFile = createInvoiceFile(order.getId());
            
            // Tạo PDF
            generateInvoicePDF(order, orderItems, invoiceFile.getAbsolutePath());
            
            System.out.println("✅ Đã tạo hóa đơn PDF: " + invoiceFile.getAbsolutePath());
            return invoiceFile;
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi tạo file PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("❌ Lỗi tạo hóa đơn: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Tạo và mở file PDF hóa đơn
     */
    public void generateAndOpenInvoice(Order order, List<OrderItem> orderItems) {
        if (order == null || orderItems == null || orderItems.isEmpty()) {
            System.err.println("❌ Lỗi: Order hoặc OrderItems không hợp lệ");
            return;
        }
        
        try {
            // Tạo file PDF với tên cố định trong thư mục Documents
            File invoiceFile = createInvoiceFile(order.getId());
            
            // Tạo PDF
            generateInvoicePDF(order, orderItems, invoiceFile.getAbsolutePath());
            
            // Thử mở file PDF với nhiều phương pháp
            boolean opened = tryOpenPDF(invoiceFile);
            
            if (opened) {
                System.out.println("✅ Đã tạo và mở hóa đơn PDF: " + invoiceFile.getName());
            } else {
                System.out.println("✅ Đã tạo hóa đơn PDF: " + invoiceFile.getAbsolutePath());
                System.out.println("📁 Vui lòng mở file thủ công từ: " + invoiceFile.getParent());
            }
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi tạo file PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ Lỗi tạo hóa đơn: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tạo file hóa đơn với đường dẫn cố định
     */
    private File createInvoiceFile(int orderId) throws IOException {
        // Tạo thư mục Documents/BarManager/Invoices nếu chưa có
        File documentsDir = new File(System.getProperty("user.home"), "Documents");
        File barManagerDir = new File(documentsDir, "BarManager");
        File invoicesDir = new File(barManagerDir, "Invoices");
        
        if (!invoicesDir.exists()) {
            invoicesDir.mkdirs();
        }
        
        // Tạo tên file với timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
        String fileName = String.format("HoaDon_Order%d_%s.pdf", orderId, timestamp);
        
        return new File(invoicesDir, fileName);
    }
    
    /**
     * Thử mở file PDF với nhiều phương pháp
     */
    private boolean tryOpenPDF(File pdfFile) {
        // Phương pháp 1: Desktop API
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(pdfFile);
                    return true;
                }
            } catch (Exception e) {
                System.err.println("Không thể mở PDF bằng Desktop API: " + e.getMessage());
            }
        }
        
        // Phương pháp 2: Runtime exec (Windows)
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                Runtime.getRuntime().exec("cmd /c start \"\" \"" + pdfFile.getAbsolutePath() + "\"");
                return true;
            } catch (Exception e) {
                System.err.println("Không thể mở PDF bằng Windows command: " + e.getMessage());
            }
        }
        
        // Phương pháp 3: Runtime exec (macOS)
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            try {
                Runtime.getRuntime().exec("open \"" + pdfFile.getAbsolutePath() + "\"");
                return true;
            } catch (Exception e) {
                System.err.println("Không thể mở PDF bằng macOS command: " + e.getMessage());
            }
        }
        
        // Phương pháp 4: Runtime exec (Linux)
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            try {
                Runtime.getRuntime().exec("xdg-open \"" + pdfFile.getAbsolutePath() + "\"");
                return true;
            } catch (Exception e) {
                System.err.println("Không thể mở PDF bằng Linux command: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Tạo file PDF hóa đơn
     */
    private void generateInvoicePDF(Order order, List<OrderItem> orderItems, String filePath) throws IOException {
        PdfWriter writer = new PdfWriter(filePath);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Thiết lập font - sử dụng font mặc định với encoding UTF-8
        PdfFont font = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA, "UTF-8");
        PdfFont boldFont = PdfFontFactory.createFont(com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD, "UTF-8");
        
        // Header - Logo và tên bar
        Paragraph title = new Paragraph("BAR MANAGER")
                .setFont(boldFont)
                .setFontSize(24)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(5);
        
        Paragraph subtitle = new Paragraph("HOA DON THANH TOAN")
                .setFont(boldFont)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        
        document.add(title);
        document.add(subtitle);
        
        // Thông tin đơn hàng
        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}));
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setMarginBottom(20);
        
        // Thông tin bên trái
        Cell leftCell = new Cell();
        leftCell.add(new Paragraph("Don hang: #" + order.getId())
                .setFont(boldFont)
                .setFontSize(12));
        leftCell.add(new Paragraph("Ban: " + order.getTableId())
                .setFont(font)
                .setFontSize(11)
                .setMarginTop(5));
        leftCell.add(new Paragraph("Thoi gian: " + dateFormatter.format(order.getOrderTime()))
                .setFont(font)
                .setFontSize(11)
                .setMarginTop(5));
        
        // Thông tin bên phải
        Cell rightCell = new Cell();
        rightCell.add(new Paragraph("Tong tien:")
                .setFont(boldFont)
                .setFontSize(12));
        rightCell.add(new Paragraph(formatCurrency(order.getTotalAmount()))
                .setFont(boldFont)
                .setFontSize(16)
                .setMarginTop(5));
        rightCell.add(new Paragraph("Trang thai: " + getStatusText(order.getStatus()))
                .setFont(font)
                .setFontSize(11)
                .setMarginTop(5));
        
        infoTable.addCell(leftCell);
        infoTable.addCell(rightCell);
        document.add(infoTable);
        
        // Đường kẻ ngang
        LineSeparator separator = new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine());
        document.add(separator);
        document.add(new Paragraph(" ").setMarginBottom(10));
        
        // Chi tiết món ăn
        Paragraph itemsTitle = new Paragraph("CHI TIET MON AN:")
                .setFont(boldFont)
                .setFontSize(14)
                .setMarginBottom(10);
        document.add(itemsTitle);
        
        // Bảng chi tiết món ăn
        Table itemsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 2, 2}));
        itemsTable.setWidth(UnitValue.createPercentValue(100));
        itemsTable.setMarginBottom(20);
        
        // Header của bảng
        Cell headerCell1 = new Cell().add(new Paragraph("Ten mon").setFont(boldFont).setFontSize(11));
        Cell headerCell2 = new Cell().add(new Paragraph("SL").setFont(boldFont).setFontSize(11));
        Cell headerCell3 = new Cell().add(new Paragraph("Don gia").setFont(boldFont).setFontSize(11));
        Cell headerCell4 = new Cell().add(new Paragraph("Thanh tien").setFont(boldFont).setFontSize(11));
        
        itemsTable.addHeaderCell(headerCell1);
        itemsTable.addHeaderCell(headerCell2);
        itemsTable.addHeaderCell(headerCell3);
        itemsTable.addHeaderCell(headerCell4);
        
        // Thêm các món ăn
        MenuItemDAO menuDAO = new MenuItemDAO();
        double totalAmount = 0;
        
        for (OrderItem item : orderItems) {
            if (item == null) continue;
            
            try {
                MenuItem menuItem = null;
                String itemName = "Món không xác định";
                
                // Thử lấy tên món từ database
                try {
                    menuItem = menuDAO.findById(item.getMenuItemId());
                    if (menuItem != null && menuItem.getName() != null) {
                        itemName = menuItem.getName();
                    }
                } catch (Exception e) {
                    System.err.println("Không thể load menu item ID " + item.getMenuItemId() + ": " + e.getMessage());
                }
                
                // Fallback: sử dụng display name nếu có
                if (itemName.equals("Món không xác định") && item.getDisplayName() != null) {
                    itemName = item.getDisplayName();
                }
                
                Cell nameCell = new Cell().add(new Paragraph(itemName).setFont(font).setFontSize(10));
                Cell qtyCell = new Cell().add(new Paragraph("x" + item.getQuantity()).setFont(font).setFontSize(10));
                Cell priceCell = new Cell().add(new Paragraph(formatCurrency(item.getPrice())).setFont(font).setFontSize(10));
                Cell totalCell = new Cell().add(new Paragraph(formatCurrency(item.getSubtotal())).setFont(font).setFontSize(10));
                
                itemsTable.addCell(nameCell);
                itemsTable.addCell(qtyCell);
                itemsTable.addCell(priceCell);
                itemsTable.addCell(totalCell);
                
                totalAmount += item.getSubtotal();
                
            } catch (Exception e) {
                System.err.println("Lỗi xử lý order item: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        document.add(itemsTable);
        
        // Đường kẻ ngang
        document.add(separator);
        document.add(new Paragraph(" ").setMarginBottom(10));
        
        // Tổng cộng
        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{2, 1}));
        totalTable.setWidth(UnitValue.createPercentValue(100));
        
        Cell totalLabelCell = new Cell().add(new Paragraph("TONG CONG:").setFont(boldFont).setFontSize(16));
        Cell totalValueCell = new Cell().add(new Paragraph(formatCurrency(totalAmount)).setFont(boldFont).setFontSize(16));
        
        totalTable.addCell(totalLabelCell);
        totalTable.addCell(totalValueCell);
        document.add(totalTable);
        
        // Footer
        document.add(new Paragraph(" ").setMarginTop(30));
        document.add(new Paragraph("Cam on quy khach da su dung dich vu!")
                .setFont(font)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));
        
        document.add(new Paragraph("Bar Manager - He thong quan ly quan bar")
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(5));
        
        document.close();
    }
    
    /**
     * Format tiền tệ
     */
    private String formatCurrency(double amount) {
        return currencyFormatter.format(amount) + " VNĐ";
    }
    
    /**
     * Format tiền tệ cho BigDecimal
     */
    private String formatCurrency(java.math.BigDecimal amount) {
        return currencyFormatter.format(amount) + " VNĐ";
    }
    
    /**
     * Tạo font tiếng Việt với fallback
     */
    private PdfFont createVietnameseFont() {
        // Danh sách font ưu tiên cho tiếng Việt
        String[] fontNames = {
            "Arial",           // Windows
            "Helvetica",       // macOS/Linux
            "Liberation Sans", // Linux
            "DejaVu Sans",     // Linux
            "Tahoma",          // Windows
            "Verdana"          // Windows
        };
        
        for (String fontName : fontNames) {
            try {
                return PdfFontFactory.createFont(fontName, "UTF-8");
            } catch (Exception e) {
                // Thử font tiếp theo
                continue;
            }
        }
        
        // Fallback: sử dụng font mặc định
        try {
            return PdfFontFactory.createFont();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo font", e);
        }
    }
    
    /**
     * Tạo font bold tiếng Việt với fallback
     */
    private PdfFont createVietnameseBoldFont() {
        // Danh sách font bold ưu tiên cho tiếng Việt
        String[] fontNames = {
            "Arial-Bold",           // Windows
            "Helvetica-Bold",       // macOS/Linux
            "Liberation Sans Bold", // Linux
            "DejaVu Sans Bold",     // Linux
            "Tahoma-Bold",          // Windows
            "Verdana-Bold"          // Windows
        };
        
        for (String fontName : fontNames) {
            try {
                return PdfFontFactory.createFont(fontName, "UTF-8");
            } catch (Exception e) {
                // Thử font tiếp theo
                continue;
            }
        }
        
        // Fallback: sử dụng font mặc định
        try {
            return PdfFontFactory.createFont();
        } catch (Exception e) {
            throw new RuntimeException("Không thể tạo font bold", e);
        }
    }
    
    /**
     * Chuyển đổi trạng thái order
     */
    private String getStatusText(String status) {
        switch (status) {
            case "pending": return "Cho xu ly";
            case "completed": return "Da hoan thanh";
            case "paid": return "Da thanh toan";
            default: return status;
        }
    }
}

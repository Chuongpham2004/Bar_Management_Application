-- Script để thêm cột discount_percent vào bảng orders
-- Chạy script này để sửa lỗi "Unknown column 'o.discount_percent' in 'field list'"

USE bar_management;

-- Thêm cột discount_percent vào bảng orders nếu chưa có
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS discount_percent DECIMAL(5,2) DEFAULT 0.00 
COMMENT 'Phần trăm giảm giá (0-100)';

-- Cập nhật tất cả orders hiện tại có discount_percent = 0
UPDATE orders 
SET discount_percent = 0.00 
WHERE discount_percent IS NULL;

-- Kiểm tra kết quả
SELECT 
    id, 
    table_id, 
    status, 
    total_amount, 
    discount_percent,
    order_time 
FROM orders 
ORDER BY order_time DESC 
LIMIT 5;

-- Hiển thị cấu trúc bảng orders
DESCRIBE orders;

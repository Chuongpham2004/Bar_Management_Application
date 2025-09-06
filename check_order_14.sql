-- Kiểm tra Order #14 trong database
USE bar_management;

-- Kiểm tra Order #14
SELECT 
    id, 
    table_id, 
    status, 
    total_amount, 
    discount_percent,
    order_time
FROM orders 
WHERE id = 14;

-- Kiểm tra Order Items của Order #14
SELECT 
    oi.id,
    oi.quantity,
    oi.price,
    oi.subtotal,
    mi.name as item_name
FROM order_items oi
LEFT JOIN menu_items mi ON oi.menu_item_id = mi.id
WHERE oi.order_id = 14;

-- Kiểm tra tất cả orders có discount_percent > 0
SELECT 
    id, 
    table_id, 
    total_amount, 
    discount_percent,
    (total_amount / (1 - discount_percent/100)) as original_amount
FROM orders 
WHERE discount_percent > 0;

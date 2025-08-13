-- Tạo cơ sở dữ liệu
CREATE DATABASE IF NOT EXISTS bar_management;
USE bar_management;

-- Tạo bảng users
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'STAFF', 'MANAGER') DEFAULT 'STAFF',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng tables
CREATE TABLE tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_number VARCHAR(10) UNIQUE NOT NULL,
    capacity INT NOT NULL DEFAULT 4,
    status ENUM('AVAILABLE', 'OCCUPIED', 'RESERVED', 'CLEANING') DEFAULT 'AVAILABLE',
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Tạo bảng menu_items
CREATE TABLE menu_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    description TEXT,
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo bảng orders
CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_id INT NOT NULL,
    table_number VARCHAR(10) NOT NULL,
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_time TIMESTAMP NULL,
    total_amount DECIMAL(10,2) DEFAULT 0.00,
    status ENUM('PENDING', 'PAID', 'CANCELLED') DEFAULT 'PENDING',
    FOREIGN KEY (table_id) REFERENCES tables(id)
);

-- Tạo bảng order_items
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    menu_item_id INT NOT NULL,
    menu_item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
);

-- Thêm dữ liệu mẫu cho users
INSERT INTO users (username, password, full_name, role) VALUES
('admin', 'admin123', 'Quản trị viên', 'ADMIN'),
('staff', 'staff123', 'Nhân viên', 'STAFF'),
('manager', 'manager123', 'Quản lý', 'MANAGER');

-- Thêm dữ liệu mẫu cho tables
INSERT INTO tables (table_number, capacity, status) VALUES
('A1', 4, 'AVAILABLE'),
('A2', 4, 'AVAILABLE'),
('A3', 6, 'AVAILABLE'),
('A4', 6, 'AVAILABLE'),
('B1', 2, 'AVAILABLE'),
('B2', 2, 'AVAILABLE'),
('B3', 4, 'AVAILABLE'),
('B4', 4, 'AVAILABLE'),
('VIP1', 8, 'AVAILABLE'),
('VIP2', 8, 'AVAILABLE');

-- Thêm dữ liệu mẫu cho menu_items
INSERT INTO menu_items (name, category, price, description) VALUES
-- Bia
('Heineken', 'Beer', 30000, 'Bia Heineken 330ml'),
('Tiger', 'Beer', 25000, 'Bia Tiger 330ml'),
('Corona', 'Beer', 35000, 'Bia Corona 330ml'),
('Budweiser', 'Beer', 28000, 'Bia Budweiser 330ml'),

-- Rượu mạnh
('Johnnie Walker Red', 'Whisky', 150000, 'Whisky Johnnie Walker Red Label'),
('Jack Daniel\'s', 'Whisky', 180000, 'Whisky Jack Daniel\'s'),
('Hennessy VSOP', 'Cognac', 250000, 'Cognac Hennessy VSOP'),
('Grey Goose', 'Vodka', 200000, 'Vodka Grey Goose'),

-- Cocktail
('Mojito', 'Cocktail', 80000, 'Cocktail Mojito với rum, chanh, bạc hà'),
('Margarita', 'Cocktail', 90000, 'Cocktail Margarita với tequila, chanh'),
('Martini', 'Cocktail', 100000, 'Cocktail Martini với gin, vermouth'),
('Long Island', 'Cocktail', 120000, 'Cocktail Long Island Iced Tea'),

-- Nước giải khát
('Coca Cola', 'Soft Drink', 15000, 'Coca Cola 330ml'),
('Pepsi', 'Soft Drink', 15000, 'Pepsi 330ml'),
('Sprite', 'Soft Drink', 15000, 'Sprite 330ml'),
('Fanta', 'Soft Drink', 15000, 'Fanta 330ml'),
('Nước suối', 'Soft Drink', 10000, 'Nước suối 500ml'),

-- Đồ ăn nhẹ
('Khoai tây chiên', 'Snack', 30000, 'Khoai tây chiên giòn'),
('Đậu phộng', 'Snack', 20000, 'Đậu phộng rang muối'),
('Bánh mì nướng', 'Snack', 25000, 'Bánh mì nướng bơ tỏi'),
('Phô mai que', 'Snack', 35000, 'Phô mai que chiên giòn');

-- Tạo index để tối ưu hiệu suất
CREATE INDEX idx_orders_table_id ON orders(table_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_menu_items_category ON menu_items(category);
CREATE INDEX idx_tables_status ON tables(status);

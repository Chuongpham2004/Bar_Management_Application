# Bar Management Application

Ứng dụng desktop quản lý quầy bar được xây dựng bằng JavaFX và MySQL.

## Tính năng chính

- 🔐 **Đăng nhập**: Hệ thống xác thực người dùng
- 🪑 **Quản lý bàn**: Theo dõi trạng thái bàn (trống, có khách, đặt trước)
- 🍸 **Đặt hàng**: Gọi món theo bàn với menu đa dạng
- 💳 **Thanh toán**: Xử lý thanh toán với nhiều phương thức
- 📊 **Báo cáo**: Thống kê doanh thu và hiệu suất

## Flow hoạt động

1. **Đăng nhập** → Xác thực người dùng
2. **Dashboard** → Màn hình chính với các chức năng
3. **Chọn bàn** → Xem trạng thái và chọn bàn
4. **Đặt hàng** → Gọi món cho bàn đã chọn
5. **Thanh toán** → Xử lý thanh toán và hoàn tất đơn hàng

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- MySQL 8.0 hoặc cao hơn
- Maven 3.6+

## Cài đặt

### 1. Clone repository
```bash
git clone <repository-url>
cd Bar_Management_Application
```

### 2. Thiết lập cơ sở dữ liệu
1. Tạo database MySQL
2. Chạy script `database_setup.sql` để tạo bảng và dữ liệu mẫu
3. Cập nhật thông tin kết nối trong `DatabaseConfig.java`

### 3. Cấu hình database
Chỉnh sửa file `src/main/java/com/barmanagement/config/DatabaseConfig.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/bar_management";
private static final String USERNAME = "your_username";
private static final String PASSWORD = "your_password";
```

### 4. Build và chạy ứng dụng
```bash
# Build project
mvn clean compile

# Chạy ứng dụng
mvn javafx:run
```

## Tài khoản demo

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Quản trị viên |
| staff | staff123 | Nhân viên |
| manager | manager123 | Quản lý |

## Cấu trúc dự án

```
src/main/java/com/barmanagement/
├── config/
│   └── DatabaseConfig.java          # Cấu hình database
├── controller/
│   ├── LoginController.java         # Xử lý đăng nhập
│   ├── DashboardController.java     # Màn hình chính
│   ├── TablesController.java        # Quản lý bàn
│   ├── OrderController.java         # Đặt hàng
│   ├── PaymentController.java       # Thanh toán
│   ├── OrdersController.java        # Quản lý đơn hàng
│   └── ReportsController.java       # Báo cáo
├── dao/
│   ├── UserDAO.java                 # Truy cập dữ liệu user
│   ├── TableDAO.java                # Truy cập dữ liệu bàn
│   ├── MenuItemDAO.java             # Truy cập dữ liệu menu
│   └── OrderDAO.java                # Truy cập dữ liệu đơn hàng
├── model/
│   ├── User.java                    # Model người dùng
│   ├── Table.java                   # Model bàn
│   ├── MenuItem.java                # Model món ăn
│   ├── Order.java                   # Model đơn hàng
│   └── OrderItem.java               # Model item trong đơn hàng
├── service/
│   ├── AuthService.java             # Dịch vụ xác thực
│   └── OrderService.java            # Dịch vụ đơn hàng
└── Main.java                        # Entry point

src/main/resources/fxml/
├── login.fxml                       # Giao diện đăng nhập
├── dashboard.fxml                   # Giao diện dashboard
├── tables.fxml                      # Giao diện quản lý bàn
├── order.fxml                       # Giao diện đặt hàng
├── payment.fxml                     # Giao diện thanh toán
├── orders.fxml                      # Giao diện quản lý đơn hàng
└── reports.fxml                     # Giao diện báo cáo
```

## Tính năng chi tiết

### Quản lý bàn
- Hiển thị trạng thái bàn theo màu sắc
- Xem thông tin sức chứa và tổng tiền
- Chuyển đổi trạng thái bàn

### Đặt hàng
- Menu phân loại theo danh mục
- Thêm/xóa món với số lượng
- Tính tổng tiền tự động
- Lưu đơn hàng vào database

### Thanh toán
- Chọn phương thức thanh toán
- Tính tiền thối tự động
- Hoàn tất đơn hàng
- Cập nhật trạng thái bàn

## Công nghệ sử dụng

- **Frontend**: JavaFX
- **Backend**: Java 17
- **Database**: MySQL 8.0
- **Build Tool**: Maven
- **Architecture**: MVC Pattern

## Đóng góp

1. Fork dự án
2. Tạo feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit thay đổi (`git commit -m 'Add some AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Tạo Pull Request

## License

Dự án này được phân phối dưới MIT License. Xem file `LICENSE` để biết thêm chi tiết.

## Hỗ trợ

Nếu gặp vấn đề, vui lòng tạo issue trên GitHub hoặc liên hệ qua email.

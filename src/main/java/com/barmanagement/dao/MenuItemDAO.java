package com.barmanagement.dao;

import com.barmanagement.model.MenuItem;

import java.sql.*;
import java.util.*;

public class MenuItemDAO {

    /**
     * Lấy tất cả menu items với thông tin đầy đủ
     */
    public List<MenuItem> findAll() throws SQLException {
        String sql = """
            SELECT id, name, price, category, image_path, description, 
                   is_available, preparation_time 
            FROM menu_items 
            ORDER BY category, name
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<MenuItem> list = new ArrayList<>();
            while (rs.next()) {
                MenuItem m = new MenuItem();
                m.setId(rs.getInt("id"));
                m.setName(rs.getString("name"));
                // DECIMAL -> double
                m.setPrice(rs.getBigDecimal("price").doubleValue());
                m.setCategory(rs.getString("category"));
                m.setImagePath(rs.getString("image_path"));
                m.setDescription(rs.getString("description"));
                m.setAvailable(rs.getBoolean("is_available"));
                m.setPreparationTime(rs.getInt("preparation_time"));
                list.add(m);
            }
            return list;
        }
    }

    /**
     * Lấy menu items theo category
     */
    public List<MenuItem> findByCategory(String category) throws SQLException {
        String sql = """
            SELECT id, name, price, category, image_path, description, 
                   is_available, preparation_time 
            FROM menu_items 
            WHERE category = ? AND is_available = TRUE
            ORDER BY name
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                List<MenuItem> list = new ArrayList<>();
                while (rs.next()) {
                    MenuItem m = mapResultSetToMenuItem(rs);
                    list.add(m);
                }
                return list;
            }
        }
    }

    /**
     * Lấy menu items có sẵn (available = true)
     */
    public List<MenuItem> findAvailable() throws SQLException {
        String sql = """
            SELECT id, name, price, category, image_path, description, 
                   is_available, preparation_time 
            FROM menu_items 
            WHERE is_available = TRUE
            ORDER BY category, name
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<MenuItem> list = new ArrayList<>();
            while (rs.next()) {
                MenuItem m = mapResultSetToMenuItem(rs);
                list.add(m);
            }
            return list;
        }
    }

    /**
     * Thêm menu item mới
     */
    public int insert(MenuItem m) throws SQLException {
        String sql = """
            INSERT INTO menu_items(name, price, category, image_path, description, 
                                 is_available, preparation_time) 
            VALUES(?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getName());
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(m.getPrice()));
            ps.setString(3, m.getCategory());
            ps.setString(4, m.getImagePath());
            ps.setString(5, m.getDescription());
            ps.setBoolean(6, m.isAvailable());
            ps.setInt(7, m.getPreparationTime());

            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                return k.next() ? k.getInt(1) : 0;
            }
        }
    }

    /**
     * Cập nhật menu item
     */
    public void update(MenuItem m) throws SQLException {
        String sql = """
            UPDATE menu_items 
            SET name=?, price=?, category=?, image_path=?, description=?, 
                is_available=?, preparation_time=? 
            WHERE id=?
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, m.getName());
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(m.getPrice()));
            ps.setString(3, m.getCategory());
            ps.setString(4, m.getImagePath());
            ps.setString(5, m.getDescription());
            ps.setBoolean(6, m.isAvailable());
            ps.setInt(7, m.getPreparationTime());
            ps.setInt(8, m.getId());

            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật chỉ thông tin cơ bản (backward compatibility)
     */
    public void updateBasic(MenuItem m) throws SQLException {
        String sql = "UPDATE menu_items SET name=?, price=?, category=? WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, m.getName());
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(m.getPrice()));
            ps.setString(3, m.getCategory());
            ps.setInt(4, m.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật trạng thái available
     */
    public void updateAvailability(int id, boolean isAvailable) throws SQLException {
        String sql = "UPDATE menu_items SET is_available=? WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setBoolean(1, isAvailable);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Cập nhật đường dẫn ảnh
     */
    public void updateImagePath(int id, String imagePath) throws SQLException {
        String sql = "UPDATE menu_items SET image_path=? WHERE id=?";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, imagePath);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Xóa menu item
     */
    public void delete(int id) throws SQLException {
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM menu_items WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Tìm menu item theo ID
     */
    public MenuItem findById(int id) throws SQLException {
        String sql = """
            SELECT id, name, price, category, image_path, description, 
                   is_available, preparation_time 
            FROM menu_items 
            WHERE id=?
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return mapResultSetToMenuItem(rs);
            }
        }
    }

    /**
     * Tìm kiếm menu items theo tên
     */
    public List<MenuItem> searchByName(String keyword) throws SQLException {
        String sql = """
            SELECT id, name, price, category, image_path, description, 
                   is_available, preparation_time 
            FROM menu_items 
            WHERE LOWER(name) LIKE LOWER(?) AND is_available = TRUE
            ORDER BY name
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<MenuItem> list = new ArrayList<>();
                while (rs.next()) {
                    MenuItem m = mapResultSetToMenuItem(rs);
                    list.add(m);
                }
                return list;
            }
        }
    }

    /**
     * Lấy tất cả categories duy nhất
     */
    public List<String> findAllCategories() throws SQLException {
        String sql = "SELECT DISTINCT category FROM menu_items ORDER BY category";
        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<String> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            return categories;
        }
    }

    /**
     * Đếm số menu items theo category
     */
    public Map<String, Integer> countByCategory() throws SQLException {
        String sql = """
            SELECT category, COUNT(*) as count 
            FROM menu_items 
            WHERE is_available = TRUE
            GROUP BY category
        """;

        try (Connection c = JDBCConnect.getJDBCConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            Map<String, Integer> counts = new HashMap<>();
            while (rs.next()) {
                counts.put(rs.getString("category"), rs.getInt("count"));
            }
            return counts;
        }
    }

    /**
     * Thêm menu items mẫu cho demo
     */
    public void insertSampleData() throws SQLException {
        List<MenuItem> sampleItems = Arrays.asList(
                new MenuItem(0, "Cocktail Tequila", 120000, "Đồ uống", "cocktail.jpg",
                        "Cocktail truyền thống với tequila, lime và muối", true, 5),
                new MenuItem(0, "Whiskey Cổ Điển", 150000, "Đồ uống", "whiskey.jpg",
                        "Whiskey 12 năm tuổi, phục vụ với đá", true, 3),
                new MenuItem(0, "Pasta Carbonara", 180000, "Món chính", "pasta.jpg",
                        "Pasta truyền thống với bacon và phô mai", true, 20),
                new MenuItem(0, "Salad Caesar", 95000, "Khai vị", "salad.jpg",
                        "Salad tươi với sốt Caesar đặc biệt", true, 10),
                new MenuItem(0, "Tiramisu", 85000, "Tráng miệng", "tiramisu.jpg",
                        "Bánh Tiramisu Ý nguyên bản", true, 5)
        );

        for (MenuItem item : sampleItems) {
            try {
                insert(item);
            } catch (SQLException e) {
                // Ignore if already exists
                System.out.println("Sample item already exists: " + item.getName());
            }
        }
    }

    /**
     * Helper method để map ResultSet thành MenuItem
     */
    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        MenuItem m = new MenuItem();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));

        // Handle null BigDecimal
        java.math.BigDecimal price = rs.getBigDecimal("price");
        m.setPrice(price != null ? price.doubleValue() : 0.0);

        m.setCategory(rs.getString("category"));
        m.setImagePath(rs.getString("image_path"));
        m.setDescription(rs.getString("description"));
        m.setAvailable(rs.getBoolean("is_available"));
        m.setPreparationTime(rs.getInt("preparation_time"));

        return m;
    }

    /**
     * Backup compatibility - insert với thông tin cơ bản
     */
    public int insertBasic(String name, double price, String category) throws SQLException {
        MenuItem item = MenuItem.createBasicItem(name, price, category);
        return insert(item);
    }
}
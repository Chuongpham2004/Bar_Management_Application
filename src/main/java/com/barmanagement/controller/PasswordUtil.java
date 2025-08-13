package com.barmanagement.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int SALT_LENGTH = 16;

    /**
     * Tạo salt ngẫu nhiên
     */
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        StringBuilder salt = new StringBuilder(SALT_LENGTH);

        for (int i = 0; i < SALT_LENGTH; i++) {
            salt.append(SALT_CHARS.charAt(random.nextInt(SALT_CHARS.length())));
        }

        return salt.toString();
    }

    /**
     * Mã hóa mật khẩu với SHA-256 và salt
     */
    public static String hashPassword(String password) {
        try {
            String salt = generateSalt();
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Kết hợp password và salt
            String saltedPassword = password + salt;

            // Hash password
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());

            // Chuyển đổi thành Base64
            String hashedPassword = Base64.getEncoder().encodeToString(hashedBytes);

            // Trả về salt + hashedPassword (salt ở đầu để dễ tách)
            return salt + ":" + hashedPassword;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi mã hóa mật khẩu", e);
        }
    }

    /**
     * Kiểm tra mật khẩu có khớp với hash không
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Tách salt và hash
            String[] parts = storedHash.split(":", 2);
            if (parts.length != 2) {
                return false;
            }

            String salt = parts[0];
            String originalHash = parts[1];

            // Hash password với salt đã lưu
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            String hashedPassword = Base64.getEncoder().encodeToString(hashedBytes);

            // So sánh hash
            return originalHash.equals(hashedPassword);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Lỗi xác thực mật khẩu", e);
        }
    }

    /**
     * Phương thức để test
     */
    public static void main(String[] args) {
        String password = "123456";
        String hashedPassword = hashPassword(password);

        System.out.println("Password gốc: " + password);
        System.out.println("Password đã hash: " + hashedPassword);
        System.out.println("Verify kết quả: " + verifyPassword(password, hashedPassword));
        System.out.println("Verify sai: " + verifyPassword("wrongpass", hashedPassword));
    }
}

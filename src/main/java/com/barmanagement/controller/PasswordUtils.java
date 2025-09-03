package com.barmanagement.controller;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {
    public static String hashPassword(String password) {
        try {
            // Tạo salt ngẫu nhiên
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            // Hash password với salt
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));

            // Kết hợp salt và hash
            byte[] hashWithSalt = new byte[salt.length + hashedPassword.length];
            System.arraycopy(salt, 0, hashWithSalt, 0, salt.length);
            System.arraycopy(hashedPassword, 0, hashWithSalt, salt.length, hashedPassword.length);

            // Encode thành Base64
            return Base64.getEncoder().encodeToString(hashWithSalt);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Verify password với hash đã lưu
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Decode hash từ Base64
            byte[] hashWithSalt = Base64.getDecoder().decode(storedHash);

            // Tách salt (16 bytes đầu)
            byte[] salt = new byte[16];
            System.arraycopy(hashWithSalt, 0, salt, 0, 16);

            // Hash password với salt đã tách
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedPassword = md.digest(password.getBytes("UTF-8"));

            // So sánh với phần hash đã lưu
            for (int i = 0; i < hashedPassword.length; i++) {
                if (hashWithSalt[i + 16] != hashedPassword[i]) {
                    return false;
                }
            }
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Method đơn giản hơn sử dụng SHA-256 (không có salt - ít an toàn hơn)
    public static String hashPasswordSimple(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return password;
        }
    }

    // Verify cho simple hash
    public static boolean verifyPasswordSimple(String password, String storedHash) {
        String hashedInput = hashPasswordSimple(password);
        return hashedInput != null && hashedInput.equals(storedHash);
    }
}
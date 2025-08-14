package com.barmanagement.controller;

public class PasswordTest {
    public static void main(String[] args) {
        System.out.println("=== TESTING PASSWORD ENCRYPTION SYSTEM ===\n");

        // Test passwords
        String[] testPasswords = {"admin123", "password", "123456", "mySecurePassword!"};

        for (String password : testPasswords) {
            System.out.println("Testing password: " + password);

            // Hash password
            String hashed = PasswordUtils.hashPassword(password);
            System.out.println("Hashed: " + hashed);

            // Verify correct password
            boolean isValid = PasswordUtils.verifyPassword(password, hashed);
            System.out.println("Verification (correct): " + isValid);

            // Verify wrong password
            boolean isInvalid = PasswordUtils.verifyPassword(password + "wrong", hashed);
            System.out.println("Verification (wrong): " + isInvalid);

            System.out.println("Hash length: " + hashed.length());
            System.out.println("---");
        }

        // Test simple hash method
        System.out.println("\n=== TESTING SIMPLE HASH METHOD ===");
        String simplePassword = "test123";
        String simpleHashed = PasswordUtils.hashPasswordSimple(simplePassword);
        System.out.println("Password: " + simplePassword);
        System.out.println("Simple Hash: " + simpleHashed);
        System.out.println("Simple Hash Length: " + simpleHashed.length());

        boolean simpleVerify = PasswordUtils.verifyPasswordSimple(simplePassword, simpleHashed);
        System.out.println("Simple Verification: " + simpleVerify);

        // Test performance
        System.out.println("\n=== PERFORMANCE TEST ===");
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            String hash = PasswordUtils.hashPassword("testpassword" + i);
            PasswordUtils.verifyPassword("testpassword" + i, hash);
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time for 1000 hash+verify operations: " + (endTime - startTime) + "ms");

        System.out.println("\n=== TEST COMPLETED ===");
    }
}

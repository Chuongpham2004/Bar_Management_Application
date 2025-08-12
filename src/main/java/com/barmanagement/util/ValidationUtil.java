package com.barmanagement.util;

import java.util.regex.Pattern;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Validation Utilities - Helper methods for input validation
 */
public class ValidationUtil {

    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^(\\+84|84|0)([3|5|7|8|9])([0-9]{8})$"
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_]{3,20}$"
    );

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$"
    );

    private static final Pattern EMPLOYEE_ID_PATTERN = Pattern.compile(
            "^[A-Z]{2,3}[0-9]{3,6}$"
    );

    private static final Pattern TABLE_NUMBER_PATTERN = Pattern.compile(
            "^[A-Z]?[0-9]{1,3}$"
    );

    /**
     * String validations
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotNullOrEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    public static boolean hasMinLength(String str, int minLength) {
        return str != null && str.length() >= minLength;
    }

    public static boolean hasMaxLength(String str, int maxLength) {
        return str == null || str.length() <= maxLength;
    }

    public static boolean isLengthBetween(String str, int minLength, int maxLength) {
        return hasMinLength(str, minLength) && hasMaxLength(str, maxLength);
    }

    /**
     * Email validation
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static String validateEmail(String email) {
        if (isNullOrEmpty(email)) {
            return "Email không được để trống";
        }
        if (!isValidEmail(email)) {
            return "Email không đúng định dạng";
        }
        return null; // Valid
    }

    /**
     * Phone number validation (Vietnam)
     */
    public static boolean isValidPhoneNumber(String phone) {
        if (isNullOrEmpty(phone)) return false;

        // Remove spaces and special characters
        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");
        return PHONE_PATTERN.matcher(cleanPhone).matches();
    }

    public static String validatePhoneNumber(String phone) {
        if (isNullOrEmpty(phone)) {
            return "Số điện thoại không được để trống";
        }
        if (!isValidPhoneNumber(phone)) {
            return "Số điện thoại không đúng định dạng (VD: 0901234567)";
        }
        return null; // Valid
    }

    public static String formatPhoneNumber(String phone) {
        if (!isValidPhoneNumber(phone)) return phone;

        String cleanPhone = phone.replaceAll("[\\s\\-\\(\\)]", "");

        // Convert to standard format: 0xxx xxx xxx
        if (cleanPhone.startsWith("+84")) {
            cleanPhone = "0" + cleanPhone.substring(3);
        } else if (cleanPhone.startsWith("84")) {
            cleanPhone = "0" + cleanPhone.substring(2);
        }

        if (cleanPhone.length() == 10) {
            return cleanPhone.substring(0, 4) + " " +
                    cleanPhone.substring(4, 7) + " " +
                    cleanPhone.substring(7);
        }

        return cleanPhone;
    }

    /**
     * Username validation
     */
    public static boolean isValidUsername(String username) {
        if (isNullOrEmpty(username)) return false;
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static String validateUsername(String username) {
        if (isNullOrEmpty(username)) {
            return "Tên đăng nhập không được để trống";
        }
        if (username.length() < 3) {
            return "Tên đăng nhập phải có ít nhất 3 ký tự";
        }
        if (username.length() > 20) {
            return "Tên đăng nhập không được quá 20 ký tự";
        }
        if (!isValidUsername(username)) {
            return "Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới";
        }
        return null; // Valid
    }

    /**
     * Password validation
     */
    public static boolean isValidPassword(String password) {
        if (isNullOrEmpty(password)) return false;
        return password.length() >= 6; // Simplified for bar management
    }

    public static boolean isStrongPassword(String password) {
        if (isNullOrEmpty(password)) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static String validatePassword(String password) {
        if (isNullOrEmpty(password)) {
            return "Mật khẩu không được để trống";
        }
        if (password.length() < 6) {
            return "Mật khẩu phải có ít nhất 6 ký tự";
        }
        if (password.length() > 50) {
            return "Mật khẩu không được quá 50 ký tự";
        }
        return null; // Valid
    }

    public static String validateStrongPassword(String password) {
        String basicValidation = validatePassword(password);
        if (basicValidation != null) return basicValidation;

        if (!isStrongPassword(password)) {
            return "Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số";
        }
        return null; // Valid
    }

    /**
     * Full name validation
     */
    public static boolean isValidFullName(String fullName) {
        if (isNullOrEmpty(fullName)) return false;

        // Check length
        if (fullName.length() < 2 || fullName.length() > 100) return false;

        // Check for invalid characters (numbers, special chars except spaces, hyphens, apostrophes)
        return fullName.matches("^[a-zA-ZÀ-ỹ\\s\\-']+$");
    }

    public static String validateFullName(String fullName) {
        if (isNullOrEmpty(fullName)) {
            return "Họ tên không được để trống";
        }
        if (fullName.trim().length() < 2) {
            return "Họ tên phải có ít nhất 2 ký tự";
        }
        if (fullName.length() > 100) {
            return "Họ tên không được quá 100 ký tự";
        }
        if (!isValidFullName(fullName)) {
            return "Họ tên chỉ được chứa chữ cái và khoảng trắng";
        }
        return null; // Valid
    }

    /**
     * Employee ID validation
     */
    public static boolean isValidEmployeeId(String employeeId) {
        if (isNullOrEmpty(employeeId)) return false;
        return EMPLOYEE_ID_PATTERN.matcher(employeeId.toUpperCase()).matches();
    }

    public static String validateEmployeeId(String employeeId) {
        if (isNullOrEmpty(employeeId)) {
            return "Mã nhân viên không được để trống";
        }
        if (!isValidEmployeeId(employeeId)) {
            return "Mã nhân viên không đúng định dạng (VD: EMP001, ST123)";
        }
        return null; // Valid
    }

    /**
     * Table number validation
     */
    public static boolean isValidTableNumber(String tableNumber) {
        if (isNullOrEmpty(tableNumber)) return false;
        return TABLE_NUMBER_PATTERN.matcher(tableNumber.toUpperCase()).matches();
    }

    public static String validateTableNumber(String tableNumber) {
        if (isNullOrEmpty(tableNumber)) {
            return "Số bàn không được để trống";
        }
        if (!isValidTableNumber(tableNumber)) {
            return "Số bàn không đúng định dạng (VD: 1, A1, B12)";
        }
        return null; // Valid
    }

    /**
     * Numeric validations
     */
    public static boolean isValidPositiveNumber(String numberStr) {
        if (isNullOrEmpty(numberStr)) return false;

        try {
            double number = Double.parseDouble(numberStr);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidNonNegativeNumber(String numberStr) {
        if (isNullOrEmpty(numberStr)) return false;

        try {
            double number = Double.parseDouble(numberStr);
            return number >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidInteger(String numberStr) {
        if (isNullOrEmpty(numberStr)) return false;

        try {
            Integer.parseInt(numberStr);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidPositiveInteger(String numberStr) {
        if (!isValidInteger(numberStr)) return false;

        try {
            int number = Integer.parseInt(numberStr);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Price and currency validations
     */
    public static boolean isValidPrice(String priceStr) {
        return isValidPositiveNumber(priceStr);
    }

    public static String validatePrice(String priceStr) {
        if (isNullOrEmpty(priceStr)) {
            return "Giá không được để trống";
        }
        if (!isValidPrice(priceStr)) {
            return "Giá phải là số dương";
        }

        try {
            double price = Double.parseDouble(priceStr);
            if (price > 100000000) { // 100 million VND
                return "Giá không được vượt quá 100,000,000 VNĐ";
            }
        } catch (NumberFormatException e) {
            return "Giá không đúng định dạng";
        }

        return null; // Valid
    }

    public static boolean isValidQuantity(String quantityStr) {
        return isValidPositiveInteger(quantityStr);
    }

    public static String validateQuantity(String quantityStr) {
        if (isNullOrEmpty(quantityStr)) {
            return "Số lượng không được để trống";
        }
        if (!isValidQuantity(quantityStr)) {
            return "Số lượng phải là số nguyên dương";
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity > 1000) {
                return "Số lượng không được vượt quá 1000";
            }
        } catch (NumberFormatException e) {
            return "Số lượng không đúng định dạng";
        }

        return null; // Valid
    }

    /**
     * Date validation
     */
    public static boolean isValidDate(String dateStr) {
        if (isNullOrEmpty(dateStr)) return false;

        try {
            DateUtil.parseDate(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String validateDate(String dateStr) {
        if (isNullOrEmpty(dateStr)) {
            return "Ngày không được để trống";
        }
        if (!isValidDate(dateStr)) {
            return "Ngày không đúng định dạng (dd/MM/yyyy)";
        }
        return null; // Valid
    }

    /**
     * Role validation
     */
    public static boolean isValidRole(String role) {
        if (isNullOrEmpty(role)) return false;

        String[] validRoles = {"admin", "manager", "staff"};
        for (String validRole : validRoles) {
            if (validRole.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }

    public static String validateRole(String role) {
        if (isNullOrEmpty(role)) {
            return "Vai trò không được để trống";
        }
        if (!isValidRole(role)) {
            return "Vai trò không hợp lệ (admin, manager, staff)";
        }
        return null; // Valid
    }

    /**
     * Table status validation
     */
    public static boolean isValidTableStatus(String status) {
        if (isNullOrEmpty(status)) return false;

        String[] validStatuses = {"available", "occupied", "reserved"};
        for (String validStatus : validStatuses) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Order status validation
     */
    public static boolean isValidOrderStatus(String status) {
        if (isNullOrEmpty(status)) return false;

        String[] validStatuses = {"pending", "served", "paid", "cancelled"};
        for (String validStatus : validStatuses) {
            if (validStatus.equalsIgnoreCase(status)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generic validation result class
     */
    public static class ValidationResult {
        private boolean valid;
        private String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        @Override
        public String toString() {
            return valid ? "Valid" : "Invalid: " + errorMessage;
        }
    }

    /**
     * Comprehensive validation methods that return ValidationResult
     */
    public static ValidationResult validateEmailResult(String email) {
        String error = validateEmail(email);
        return error == null ? ValidationResult.valid() : ValidationResult.invalid(error);
    }

    public static ValidationResult validatePhoneNumberResult(String phone) {
        String error = validatePhoneNumber(phone);
        return error == null ? ValidationResult.valid() : ValidationResult.invalid(error);
    }

    public static ValidationResult validateUsernameResult(String username) {
        String error = validateUsername(username);
        return error == null ? ValidationResult.valid() : ValidationResult.invalid(error);
    }

    public static ValidationResult validatePasswordResult(String password) {
        String error = validatePassword(password);
        return error == null ? ValidationResult.valid() : ValidationResult.invalid(error);
    }

    public static ValidationResult validateFullNameResult(String fullName) {
        String error = validateFullName(fullName);
        return error == null ? ValidationResult.valid() : ValidationResult.invalid(error);
    }

    /**
     * Trim and clean input
     */
    public static String cleanInput(String input) {
        if (input == null) return null;
        return input.trim().replaceAll("\\s+", " ");
    }

    public static String sanitizeInput(String input) {
        if (input == null) return null;

        // Remove potentially harmful characters
        return input.replaceAll("[<>\"'&]", "").trim();
    }
}

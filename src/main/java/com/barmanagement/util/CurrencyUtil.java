package com.barmanagement.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Currency and Money Utilities - Helper methods for currency formatting and calculations
 */
public class CurrencyUtil {

    // Currency formatters
    private static final DecimalFormat VND_FORMAT = new DecimalFormat("#,###");
    private static final DecimalFormat VND_FORMAT_WITH_DECIMAL = new DecimalFormat("#,###.##");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    // Constants
    public static final String VND_SYMBOL = "VNĐ";
    public static final String CURRENCY_CODE = "VND";

    /**
     * Format amount to Vietnamese currency string
     */
    public static String formatVND(double amount) {
        if (amount == 0) return "0 VNĐ";
        return VND_FORMAT.format(amount) + " VNĐ";
    }

    public static String formatVND(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "0 VNĐ";
        return VND_FORMAT.format(amount) + " VNĐ";
    }

    public static String formatVND(long amount) {
        if (amount == 0) return "0 VNĐ";
        return VND_FORMAT.format(amount) + " VNĐ";
    }

    /**
     * Format amount to currency string without symbol
     */
    public static String formatAmount(double amount) {
        return VND_FORMAT.format(amount);
    }

    public static String formatAmount(BigDecimal amount) {
        if (amount == null) return "0";
        return VND_FORMAT.format(amount);
    }

    public static String formatAmount(long amount) {
        return VND_FORMAT.format(amount);
    }

    /**
     * Format amount with decimal places
     */
    public static String formatVNDWithDecimal(double amount) {
        if (amount == 0) return "0 VNĐ";
        return VND_FORMAT_WITH_DECIMAL.format(amount) + " VNĐ";
    }

    public static String formatAmountWithDecimal(double amount) {
        return VND_FORMAT_WITH_DECIMAL.format(amount);
    }

    /**
     * Parse currency string to number
     */
    public static Double parseVND(String currencyStr) {
        if (currencyStr == null || currencyStr.trim().isEmpty()) {
            return null;
        }

        try {
            // Remove currency symbols and spaces
            String cleanStr = currencyStr.replace("VNĐ", "")
                    .replace("VND", "")
                    .replace("đ", "")
                    .replace(",", "")
                    .replace(".", "")
                    .trim();

            if (cleanStr.isEmpty()) return 0.0;

            return Double.parseDouble(cleanStr);

        } catch (NumberFormatException e) {
            System.err.println("❌ Error parsing currency: " + currencyStr);
            return null;
        }
    }

    public static BigDecimal parseVNDToBigDecimal(String currencyStr) {
        Double amount = parseVND(currencyStr);
        return amount != null ? BigDecimal.valueOf(amount) : null;
    }

    /**
     * Currency calculations
     */
    public static double add(double amount1, double amount2) {
        BigDecimal bd1 = BigDecimal.valueOf(amount1);
        BigDecimal bd2 = BigDecimal.valueOf(amount2);
        return bd1.add(bd2).doubleValue();
    }

    public static double subtract(double amount1, double amount2) {
        BigDecimal bd1 = BigDecimal.valueOf(amount1);
        BigDecimal bd2 = BigDecimal.valueOf(amount2);
        return bd1.subtract(bd2).doubleValue();
    }

    public static double multiply(double amount, double multiplier) {
        BigDecimal bd1 = BigDecimal.valueOf(amount);
        BigDecimal bd2 = BigDecimal.valueOf(multiplier);
        return bd1.multiply(bd2).doubleValue();
    }

    public static double divide(double amount, double divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        BigDecimal bd1 = BigDecimal.valueOf(amount);
        BigDecimal bd2 = BigDecimal.valueOf(divisor);
        return bd1.divide(bd2, 2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Calculate percentage
     */
    public static double calculatePercentage(double amount, double percentage) {
        return multiply(amount, percentage / 100.0);
    }

    public static double addPercentage(double amount, double percentage) {
        return add(amount, calculatePercentage(amount, percentage));
    }

    public static double subtractPercentage(double amount, double percentage) {
        return subtract(amount, calculatePercentage(amount, percentage));
    }

    /**
     * Tax calculations (Vietnam VAT = 10%)
     */
    public static final double DEFAULT_VAT_RATE = 10.0; // 10%

    public static double calculateVAT(double amount) {
        return calculatePercentage(amount, DEFAULT_VAT_RATE);
    }

    public static double calculateVAT(double amount, double vatRate) {
        return calculatePercentage(amount, vatRate);
    }

    public static double addVAT(double amount) {
        return addPercentage(amount, DEFAULT_VAT_RATE);
    }

    public static double addVAT(double amount, double vatRate) {
        return addPercentage(amount, vatRate);
    }

    public static double removeVAT(double amountWithVAT) {
        return divide(amountWithVAT, (100 + DEFAULT_VAT_RATE) / 100);
    }

    public static double removeVAT(double amountWithVAT, double vatRate) {
        return divide(amountWithVAT, (100 + vatRate) / 100);
    }

    /**
     * Discount calculations
     */
    public static double calculateDiscount(double amount, double discountPercent) {
        return calculatePercentage(amount, discountPercent);
    }

    public static double applyDiscount(double amount, double discountPercent) {
        return subtractPercentage(amount, discountPercent);
    }

    public static double applyFixedDiscount(double amount, double discountAmount) {
        return Math.max(0, subtract(amount, discountAmount));
    }

    /**
     * Service charge calculations (common in restaurants)
     */
    public static final double DEFAULT_SERVICE_CHARGE_RATE = 5.0; // 5%

    public static double calculateServiceCharge(double amount) {
        return calculatePercentage(amount, DEFAULT_SERVICE_CHARGE_RATE);
    }

    public static double calculateServiceCharge(double amount, double serviceRate) {
        return calculatePercentage(amount, serviceRate);
    }

    public static double addServiceCharge(double amount) {
        return addPercentage(amount, DEFAULT_SERVICE_CHARGE_RATE);
    }

    public static double addServiceCharge(double amount, double serviceRate) {
        return addPercentage(amount, serviceRate);
    }

    /**
     * Bill calculations
     */
    public static class BillCalculation {
        private double subtotal;
        private double vatAmount;
        private double serviceChargeAmount;
        private double discountAmount;
        private double total;

        public BillCalculation(double subtotal) {
            this.subtotal = subtotal;
            this.vatAmount = 0;
            this.serviceChargeAmount = 0;
            this.discountAmount = 0;
            this.total = subtotal;
        }

        public BillCalculation withVAT(double vatRate) {
            this.vatAmount = calculateVAT(subtotal, vatRate);
            calculateTotal();
            return this;
        }

        public BillCalculation withVAT() {
            return withVAT(DEFAULT_VAT_RATE);
        }

        public BillCalculation withServiceCharge(double serviceRate) {
            this.serviceChargeAmount = calculateServiceCharge(subtotal, serviceRate);
            calculateTotal();
            return this;
        }

        public BillCalculation withServiceCharge() {
            return withServiceCharge(DEFAULT_SERVICE_CHARGE_RATE);
        }

        public BillCalculation withDiscountPercent(double discountPercent) {
            this.discountAmount = calculateDiscount(subtotal, discountPercent);
            calculateTotal();
            return this;
        }

        public BillCalculation withDiscountAmount(double discountAmount) {
            this.discountAmount = discountAmount;
            calculateTotal();
            return this;
        }

        private void calculateTotal() {
            this.total = subtotal + vatAmount + serviceChargeAmount - discountAmount;
            this.total = Math.max(0, this.total); // Ensure non-negative
        }

        // Getters
        public double getSubtotal() { return subtotal; }
        public double getVatAmount() { return vatAmount; }
        public double getServiceChargeAmount() { return serviceChargeAmount; }
        public double getDiscountAmount() { return discountAmount; }
        public double getTotal() { return total; }

        // Formatted getters
        public String getFormattedSubtotal() { return formatVND(subtotal); }
        public String getFormattedVatAmount() { return formatVND(vatAmount); }
        public String getFormattedServiceChargeAmount() { return formatVND(serviceChargeAmount); }
        public String getFormattedDiscountAmount() { return formatVND(discountAmount); }
        public String getFormattedTotal() { return formatVND(total); }

        @Override
        public String toString() {
            return String.format(
                    "Bill: Subtotal=%s, VAT=%s, Service=%s, Discount=%s, Total=%s",
                    getFormattedSubtotal(), getFormattedVatAmount(),
                    getFormattedServiceChargeAmount(), getFormattedDiscountAmount(),
                    getFormattedTotal()
            );
        }
    }

    /**
     * Quick bill calculation
     */
    public static BillCalculation calculateBill(double subtotal) {
        return new BillCalculation(subtotal);
    }

    /**
     * Round amount to nearest currency unit
     */
    public static double roundToCurrency(double amount) {
        return Math.round(amount);
    }

    public static BigDecimal roundToCurrency(BigDecimal amount) {
        if (amount == null) return null;
        return amount.setScale(0, RoundingMode.HALF_UP);
    }

    /**
     * Validate currency amount
     */
    public static boolean isValidAmount(double amount) {
        return !Double.isNaN(amount) && !Double.isInfinite(amount) && amount >= 0;
    }

    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Split bill calculation
     */
    public static double splitBill(double totalAmount, int numberOfPeople) {
        if (numberOfPeople <= 0) {
            throw new IllegalArgumentException("Number of people must be positive");
        }
        return divide(totalAmount, numberOfPeople);
    }

    public static String splitBillFormatted(double totalAmount, int numberOfPeople) {
        double amountPerPerson = splitBill(totalAmount, numberOfPeople);
        return formatVND(amountPerPerson);
    }

    /**
     * Currency conversion placeholder (for future international support)
     */
    public static double convertCurrency(double amount, String fromCurrency, String toCurrency, double exchangeRate) {
        if ("VND".equals(fromCurrency) && "VND".equals(toCurrency)) {
            return amount;
        }
        // Placeholder for future implementation
        return multiply(amount, exchangeRate);
    }

    /**
     * Format for display in tables/lists
     */
    public static String formatForTable(double amount) {
        if (amount == 0) return "-";
        return formatAmount(amount);
    }

    public static String formatForTable(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return "-";
        return formatAmount(amount);
    }
}

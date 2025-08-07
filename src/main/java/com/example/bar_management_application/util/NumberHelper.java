package com.example.bar_management_application.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberHelper {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("$#,##0.00");
    private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#0.0%");
    private static final DecimalFormat INTEGER_FORMAT = new DecimalFormat("#,##0");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.00");

    // Currency formatting
    public static String formatCurrency(double amount) {
        return CURRENCY_FORMAT.format(amount);
    }

    public static String formatCurrency(Double amount) {
        if (amount == null) return "$0.00";
        return formatCurrency(amount.doubleValue());
    }

    // Percentage formatting
    public static String formatPercentage(double percentage) {
        return PERCENTAGE_FORMAT.format(percentage / 100);
    }

    public static String formatPercentage(double numerator, double denominator) {
        if (denominator == 0) return "0.0%";
        return formatPercentage((numerator / denominator) * 100);
    }

    // Integer formatting with thousand separators
    public static String formatInteger(int number) {
        return INTEGER_FORMAT.format(number);
    }

    public static String formatInteger(Integer number) {
        if (number == null) return "0";
        return formatInteger(number.intValue());
    }

    // Decimal formatting
    public static String formatDecimal(double number) {
        return DECIMAL_FORMAT.format(number);
    }

    public static String formatDecimal(Double number) {
        if (number == null) return "0.00";
        return formatDecimal(number.doubleValue());
    }

    // Parse methods
    public static double parseCurrency(String currencyString) throws NumberFormatException {
        if (currencyString == null || currencyString.trim().isEmpty()) {
            return 0.0;
        }

        // Remove currency symbols and spaces
        String cleanString = currencyString.replaceAll("[$,\\s]", "");
        return Double.parseDouble(cleanString);
    }

    public static double parsePercentage(String percentageString) throws NumberFormatException {
        if (percentageString == null || percentageString.trim().isEmpty()) {
            return 0.0;
        }

        // Remove percentage symbol and spaces
        String cleanString = percentageString.replaceAll("[%\\s]", "");
        return Double.parseDouble(cleanString);
    }

    // Validation methods
    public static boolean isValidCurrency(String currencyString) {
        try {
            parseCurrency(currencyString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidInteger(String integerString) {
        try {
            Integer.parseInt(integerString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDecimal(String decimalString) {
        try {
            Double.parseDouble(decimalString);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Calculation helpers
    public static double calculatePercentage(double part, double whole) {
        if (whole == 0) return 0.0;
        return (part / whole) * 100;
    }

    public static double calculateTip(double amount, double tipPercentage) {
        return amount * (tipPercentage / 100);
    }

    public static double calculateTax(double amount, double taxRate) {
        return amount * (taxRate / 100);
    }

    public static double calculateDiscount(double originalPrice, double discountPercentage) {
        return originalPrice * (discountPercentage / 100);
    }

    public static double applyDiscount(double originalPrice, double discountPercentage) {
        return originalPrice - calculateDiscount(originalPrice, discountPercentage);
    }

    // Rounding methods
    public static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public static double roundToNearestCent(double value) {
        return roundToTwoDecimals(value);
    }

    public static double roundToNearestDollar(double value) {
        return Math.round(value);
    }

    // Comparison methods
    public static boolean isEqual(double a, double b) {
        return Math.abs(a - b) < 0.001; // Using epsilon for floating point comparison
    }

    public static boolean isGreaterThan(double a, double b) {
        return a - b > 0.001;
    }

    public static boolean isLessThan(double a, double b) {
        return b - a > 0.001;
    }

    // Range validation
    public static boolean isInRange(double value, double min, double max) {
        return value >= min && value <= max;
    }

    public static boolean isPriceValid(double price) {
        return price >= 0 && price <= 99999.99;
    }

    public static boolean isQuantityValid(int quantity) {
        return quantity > 0 && quantity <= 1000;
    }

    // Statistical methods
    public static double calculateAverage(double[] values) {
        if (values == null || values.length == 0) return 0.0;

        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    public static double findMax(double[] values) {
        if (values == null || values.length == 0) return 0.0;

        double max = values[0];
        for (double value : values) {
            if (value > max) {
                max = value;
            }
        }
        return max;
    }

    public static double findMin(double[] values) {
        if (values == null || values.length == 0) return 0.0;

        double min = values[0];
        for (double value : values) {
            if (value < min) {
                min = value;
            }
        }
        return min;
    }

    public static double calculateSum(double[] values) {
        if (values == null || values.length == 0) return 0.0;

        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum;
    }

    // Locale-specific formatting
    public static String formatCurrencyForLocale(double amount, Locale locale) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        return currencyFormat.format(amount);
    }

    public static String formatNumberForLocale(double number, Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        return numberFormat.format(number);
    }

    // Special formatting for display
    public static String formatCompactNumber(double number) {
        if (number < 1000) {
            return formatDecimal(number);
        } else if (number < 1000000) {
            return formatDecimal(number / 1000) + "K";
        } else if (number < 1000000000) {
            return formatDecimal(number / 1000000) + "M";
        } else {
            return formatDecimal(number / 1000000000) + "B";
        }
    }

    public static String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " min";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hr";
            } else {
                return hours + "h " + remainingMinutes + "m";
            }
        }
    }
}

package com.barmanagement.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

/**
 * Date and Time Utilities - Helper methods for date/time operations
 */
public class DateUtil {

    // Common date formats
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter DATETIME_FULL_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    public static final DateTimeFormatter DB_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));

    /**
     * Get current date/time
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalTime currentTime() {
        return LocalTime.now();
    }

    /**
     * Format LocalDateTime to various string formats
     */
    public static String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMAT) : "";
    }

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "";
    }

    public static String formatTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(TIME_FORMAT) : "";
    }

    public static String formatTime(LocalTime time) {
        return time != null ? time.format(TIME_FORMAT) : "";
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMAT) : "";
    }

    public static String formatDateTimeFull(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FULL_FORMAT) : "";
    }

    public static String formatForDatabase(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DB_DATETIME_FORMAT) : null;
    }

    public static String formatForDisplay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DISPLAY_FORMAT) : "";
    }

    public static String formatForDisplay(LocalDate date) {
        return date != null ? date.format(DISPLAY_FORMAT) : "";
    }

    /**
     * Parse string to LocalDateTime/LocalDate
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(dateTimeStr, DATETIME_FORMAT);
        } catch (DateTimeParseException e) {
            System.err.println("❌ Error parsing datetime: " + dateTimeStr);
            return null;
        }
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            System.err.println("❌ Error parsing date: " + dateStr);
            return null;
        }
    }

    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalTime.parse(timeStr, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            System.err.println("❌ Error parsing time: " + timeStr);
            return null;
        }
    }

    /**
     * Date calculations
     */
    public static LocalDateTime addDays(LocalDateTime dateTime, long days) {
        return dateTime != null ? dateTime.plusDays(days) : null;
    }

    public static LocalDateTime addHours(LocalDateTime dateTime, long hours) {
        return dateTime != null ? dateTime.plusHours(hours) : null;
    }

    public static LocalDateTime addMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime != null ? dateTime.plusMinutes(minutes) : null;
    }

    public static LocalDate addDays(LocalDate date, long days) {
        return date != null ? date.plusDays(days) : null;
    }

    public static LocalDate addMonths(LocalDate date, long months) {
        return date != null ? date.plusMonths(months) : null;
    }

    /**
     * Date comparisons
     */
    public static boolean isToday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now());
    }

    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    public static boolean isYesterday(LocalDateTime dateTime) {
        return dateTime != null && dateTime.toLocalDate().equals(LocalDate.now().minusDays(1));
    }

    public static boolean isThisWeek(LocalDateTime dateTime) {
        if (dateTime == null) return false;

        LocalDate date = dateTime.toLocalDate();
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
    }

    public static boolean isThisMonth(LocalDateTime dateTime) {
        if (dateTime == null) return false;

        LocalDate date = dateTime.toLocalDate();
        LocalDate now = LocalDate.now();

        return date.getYear() == now.getYear() && date.getMonth() == now.getMonth();
    }

    public static boolean isThisYear(LocalDateTime dateTime) {
        if (dateTime == null) return false;

        return dateTime.getYear() == LocalDate.now().getYear();
    }

    /**
     * Duration calculations
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.MINUTES.between(start, end);
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.HOURS.between(start, end);
    }

    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }

    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Format duration in human readable form
     */
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "N/A";

        long minutes = minutesBetween(start, end);

        if (minutes < 60) {
            return minutes + " phút";
        } else if (minutes < 1440) { // Less than 24 hours
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            return hours + " giờ" + (remainingMinutes > 0 ? " " + remainingMinutes + " phút" : "");
        } else {
            long days = minutes / 1440;
            long remainingHours = (minutes % 1440) / 60;
            return days + " ngày" + (remainingHours > 0 ? " " + remainingHours + " giờ" : "");
        }
    }

    public static String formatDurationFromNow(LocalDateTime dateTime) {
        return formatDuration(dateTime, LocalDateTime.now());
    }

    public static String formatDurationToNow(LocalDateTime dateTime) {
        return formatDuration(LocalDateTime.now(), dateTime);
    }

    /**
     * Get relative time description
     */
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";

        LocalDateTime now = LocalDateTime.now();
        long minutes = Math.abs(minutesBetween(dateTime, now));
        boolean isPast = dateTime.isBefore(now);

        String timeAgo = isPast ? " trước" : " nữa";

        if (minutes < 1) {
            return "Vừa xong";
        } else if (minutes < 60) {
            return minutes + " phút" + timeAgo;
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return hours + " giờ" + timeAgo;
        } else if (minutes < 10080) { // Less than 7 days
            long days = minutes / 1440;
            return days + " ngày" + timeAgo;
        } else {
            return formatDate(dateTime);
        }
    }

    /**
     * Working hours and business time calculations
     */
    public static boolean isBusinessHours(LocalDateTime dateTime) {
        if (dateTime == null) return false;

        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(LocalTime.of(8, 0)) && !time.isAfter(LocalTime.of(22, 0));
    }

    public static boolean isWeekend(LocalDateTime dateTime) {
        if (dateTime == null) return false;

        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public static boolean isWeekday(LocalDateTime dateTime) {
        return !isWeekend(dateTime);
    }

    /**
     * Get start/end of periods
     */
    public static LocalDateTime getStartOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atStartOfDay() : null;
    }

    public static LocalDateTime getEndOfDay(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toLocalDate().atTime(23, 59, 59) : null;
    }

    public static LocalDateTime getStartOfWeek(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalDate().with(DayOfWeek.MONDAY).atStartOfDay();
    }

    public static LocalDateTime getEndOfWeek(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalDate().with(DayOfWeek.SUNDAY).atTime(23, 59, 59);
    }

    public static LocalDateTime getStartOfMonth(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalDate().withDayOfMonth(1).atStartOfDay();
    }

    public static LocalDateTime getEndOfMonth(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalDate().withDayOfMonth(dateTime.toLocalDate().lengthOfMonth()).atTime(23, 59, 59);
    }

    /**
     * Age calculation
     */
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Get Vietnamese day name
     */
    public static String getVietnameseDayName(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY: return "Thứ Hai";
            case TUESDAY: return "Thứ Ba";
            case WEDNESDAY: return "Thứ Tư";
            case THURSDAY: return "Thứ Năm";
            case FRIDAY: return "Thứ Sáu";
            case SATURDAY: return "Thứ Bảy";
            case SUNDAY: return "Chủ Nhật";
            default: return "";
        }
    }

    /**
     * Get Vietnamese month name
     */
    public static String getVietnameseMonthName(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        Month month = dateTime.getMonth();
        return "Tháng " + month.getValue();
    }

    /**
     * Validate date ranges
     */
    public static boolean isValidDateRange(LocalDateTime start, LocalDateTime end) {
        return start != null && end != null && !start.isAfter(end);
    }

    /**
     * Get current timestamp for database
     */
    public static String getCurrentTimestampForDB() {
        return LocalDateTime.now().format(DB_DATETIME_FORMAT);
    }
}

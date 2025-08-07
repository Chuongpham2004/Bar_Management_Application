package com.example.bar_management_application.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;
import java.time.Month;
import java.time.Year;
import java.time.Period;
import java.time.Duration;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeHelper {

    // Standard formatters
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    private static final DateTimeFormatter CUSTOM_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final DateTimeFormatter CUSTOM_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter CUSTOM_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    private static final DateTimeFormatter DATABASE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final DateTimeFormatter FILENAME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");

    // Current date/time getters
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }

    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    public static String getCurrentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    public static LocalDate today() {
        return LocalDate.now();
    }

    public static LocalTime currentTime() {
        return LocalTime.now();
    }

    // Formatting methods
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.toLocalDate().format(DATE_FORMATTER);
    }

    public static String formatTime(LocalTime time) {
        if (time == null) return "";
        return time.format(TIME_FORMATTER);
    }

    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.toLocalTime().format(TIME_FORMATTER);
    }

    public static String formatCustomDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(CUSTOM_DATETIME_FORMATTER);
    }

    public static String formatCustomDate(LocalDate date) {
        if (date == null) return "";
        return date.format(CUSTOM_DATE_FORMATTER);
    }

    public static String formatCustomTime(LocalTime time) {
        if (time == null) return "";
        return time.format(CUSTOM_TIME_FORMATTER);
    }

    public static String formatForDatabase(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATABASE_FORMATTER);
    }

    public static String formatForFilename(LocalDateTime dateTime) {
        if (dateTime == null) dateTime = LocalDateTime.now();
        return dateTime.format(FILENAME_FORMATTER);
    }

    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_FORMATTER);
    }

    public static String formatISO(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(ISO_FORMATTER);
    }

    // Parsing methods
    public static LocalDateTime parseFromDatabase(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeString, DATABASE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDateTime parseCustomDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeString, CUSTOM_DATETIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDate parseCustomDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        try {
            return LocalDate.parse(dateString, CUSTOM_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalTime parseCustomTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) return null;
        try {
            return LocalTime.parse(timeString, CUSTOM_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    public static LocalDateTime parseISO(String isoString) {
        if (isoString == null || isoString.isEmpty()) return null;
        try {
            return LocalDateTime.parse(isoString, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // Date/Time validation
    public static boolean isValidDateTime(String dateTimeString, DateTimeFormatter formatter) {
        try {
            LocalDateTime.parse(dateTimeString, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidDate(String dateString, DateTimeFormatter formatter) {
        try {
            LocalDate.parse(dateString, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static boolean isValidTime(String timeString, DateTimeFormatter formatter) {
        try {
            LocalTime.parse(timeString, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    // Date/Time checking methods
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate().equals(LocalDate.now());
    }

    public static boolean isToday(LocalDate date) {
        if (date == null) return false;
        return date.equals(LocalDate.now());
    }

    public static boolean isYesterday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate().equals(LocalDate.now().minusDays(1));
    }

    public static boolean isTomorrow(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate().equals(LocalDate.now().plusDays(1));
    }

    public static boolean isThisWeek(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalDate date = dateTime.toLocalDate();
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
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

    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.isBefore(LocalDateTime.now());
    }

    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.isAfter(LocalDateTime.now());
    }

    public static boolean isWeekend(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    public static boolean isBusinessHour(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalTime time = dateTime.toLocalTime();
        return !time.isBefore(LocalTime.of(9, 0)) && !time.isAfter(LocalTime.of(17, 0));
    }

    // Date/Time calculation methods
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }

    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.HOURS.between(start, end);
    }

    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.MINUTES.between(start, end);
    }

    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.SECONDS.between(start, end);
    }

    // Age calculation
    public static int calculateAge(LocalDate birthDate) {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // Duration formatting
    public static String formatDuration(Duration duration) {
        if (duration == null) return "";

        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    public static String formatDurationMinutes(long minutes) {
        if (minutes < 60) {
            return minutes + " min";
        } else {
            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hr";
            } else {
                return hours + "h " + remainingMinutes + "m";
            }
        }
    }

    // Relative time formatting
    public static String getRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);

        if (duration.isNegative()) {
            // Future time
            duration = duration.abs();
            long minutes = duration.toMinutes();
            long hours = duration.toHours();
            long days = duration.toDays();

            if (days > 0) {
                return "in " + days + " day" + (days > 1 ? "s" : "");
            } else if (hours > 0) {
                return "in " + hours + " hour" + (hours > 1 ? "s" : "");
            } else if (minutes > 0) {
                return "in " + minutes + " minute" + (minutes > 1 ? "s" : "");
            } else {
                return "in a moment";
            }
        } else {
            // Past time
            long minutes = duration.toMinutes();
            long hours = duration.toHours();
            long days = duration.toDays();

            if (days > 0) {
                return days + " day" + (days > 1 ? "s" : "") + " ago";
            } else if (hours > 0) {
                return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
            } else if (minutes > 0) {
                return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
            } else {
                return "just now";
            }
        }
    }

    // Date range methods
    public static boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) return false;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public static boolean isDateTimeInRange(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (dateTime == null || startDateTime == null || endDateTime == null) return false;
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }

    // Date manipulation methods
    public static LocalDate getStartOfWeek(LocalDate date) {
        if (date == null) return null;
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    public static LocalDate getEndOfWeek(LocalDate date) {
        if (date == null) return null;
        return getStartOfWeek(date).plusDays(6);
    }

    public static LocalDate getStartOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.withDayOfMonth(1);
    }

    public static LocalDate getEndOfMonth(LocalDate date) {
        if (date == null) return null;
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static LocalDate getStartOfYear(LocalDate date) {
        if (date == null) return null;
        return date.withDayOfYear(1);
    }

    public static LocalDate getEndOfYear(LocalDate date) {
        if (date == null) return null;
        return date.withDayOfYear(date.lengthOfYear());
    }

    // Timezone methods
    public static ZonedDateTime convertToTimeZone(LocalDateTime dateTime, ZoneId targetZone) {
        if (dateTime == null || targetZone == null) return null;
        return dateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(targetZone);
    }

    public static String getCurrentTimeZone() {
        return ZoneId.systemDefault().toString();
    }

    // Business day calculations
    public static LocalDate addBusinessDays(LocalDate date, int businessDays) {
        if (date == null) return null;

        LocalDate result = date;
        int addedDays = 0;

        while (addedDays < businessDays) {
            result = result.plusDays(1);
            if (result.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    result.getDayOfWeek() != DayOfWeek.SUNDAY) {
                addedDays++;
            }
        }

        return result;
    }

    public static int countBusinessDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0;

        int businessDays = 0;
        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            if (current.getDayOfWeek() != DayOfWeek.SATURDAY &&
                    current.getDayOfWeek() != DayOfWeek.SUNDAY) {
                businessDays++;
            }
            current = current.plusDays(1);
        }

        return businessDays;
    }

    // Locale-specific formatting
    public static String formatForLocale(LocalDateTime dateTime, Locale locale) {
        if (dateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(
                FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(locale);
        return dateTime.format(formatter);
    }

    // Bar Management System specific methods
    public static boolean isBarOperatingHours(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalTime time = dateTime.toLocalTime();
        // Assuming bar operates from 11 AM to 2 AM next day
        return !time.isBefore(LocalTime.of(11, 0)) || !time.isAfter(LocalTime.of(2, 0));
    }

    public static boolean isPeakHours(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalTime time = dateTime.toLocalTime();
        // Peak hours: 7 PM to 11 PM
        return !time.isBefore(LocalTime.of(19, 0)) && !time.isAfter(LocalTime.of(23, 0));
    }

    public static String getShiftName(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        LocalTime time = dateTime.toLocalTime();

        if (!time.isBefore(LocalTime.of(11, 0)) && time.isBefore(LocalTime.of(18, 0))) {
            return "Day Shift";
        } else if (!time.isBefore(LocalTime.of(18, 0)) && time.isBefore(LocalTime.of(2, 0))) {
            return "Night Shift";
        } else {
            return "Closed";
        }
    }

    // Utility methods
    public static LocalDateTime max(LocalDateTime dt1, LocalDateTime dt2) {
        if (dt1 == null) return dt2;
        if (dt2 == null) return dt1;
        return dt1.isAfter(dt2) ? dt1 : dt2;
    }

    public static LocalDateTime min(LocalDateTime dt1, LocalDateTime dt2) {
        if (dt1 == null) return dt2;
        if (dt2 == null) return dt1;
        return dt1.isBefore(dt2) ? dt1 : dt2;
    }

    public static LocalDate max(LocalDate d1, LocalDate d2) {
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return d1.isAfter(d2) ? d1 : d2;
    }

    public static LocalDate min(LocalDate d1, LocalDate d2) {
        if (d1 == null) return d2;
        if (d2 == null) return d1;
        return d1.isBefore(d2) ? d1 : d2;
    }
}

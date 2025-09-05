package com.barmanagement.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.util.Duration;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class TimeService {
    private static final TimeService INSTANCE = new TimeService();

    private final Locale VI_VN = new Locale("vi","VN");
    private final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss", VI_VN);
    private final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", VI_VN);

    private final ReadOnlyStringWrapper timeText = new ReadOnlyStringWrapper("--:--:--");
    private final ReadOnlyStringWrapper dateText = new ReadOnlyStringWrapper("");

    private TimeService() {
        Timeline ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        ticker.setCycleCount(Timeline.INDEFINITE);
        tick();
        ticker.play();
    }

    private void tick() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        timeText.set(now.format(TIME_FMT));
        String d = now.format(DATE_FMT);
        if (!d.isEmpty()) d = Character.toUpperCase(d.charAt(0)) + d.substring(1);
        dateText.set(d);
    }

    public static TimeService get() { return INSTANCE; }

    // 🔧 Sửa kiểu trả về thành ReadOnlyStringProperty
    public ReadOnlyStringProperty timeTextProperty() { return timeText.getReadOnlyProperty(); }
    public ReadOnlyStringProperty dateTextProperty() { return dateText.getReadOnlyProperty(); }
}

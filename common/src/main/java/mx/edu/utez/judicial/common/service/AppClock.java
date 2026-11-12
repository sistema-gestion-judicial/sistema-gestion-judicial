package mx.edu.utez.judicial.common.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** Single source for "today". The demo date can be fixed in app.properties (demo.today). */
public final class AppClock {
    private AppClock() {}

    public static LocalDate today() {
        String fixed = AppConfig.get("demo.today", "");
        return fixed.isEmpty() ? LocalDate.now() : LocalDate.parse(fixed);
    }

    public static LocalDateTime now() {
        return LocalDateTime.of(today(), LocalTime.now().withNano(0));
    }
}

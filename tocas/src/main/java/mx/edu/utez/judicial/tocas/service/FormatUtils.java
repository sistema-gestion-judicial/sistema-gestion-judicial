package mx.edu.utez.judicial.tocas.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Locale;

/** Spanish date/label formatting. Exposed to the JSPs through WEB-INF/tld/format.tld. */
public final class FormatUtils {
    private static final String[] MONTHS_SHORT = {"ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "sep.", "oct.", "nov.", "dic."};
    private static final String[] MONTHS_LONG = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"};
    private static final String[] DAYS_LONG = {"lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo"};

    private FormatUtils() {}

    private static String f(String pattern, Object... args) {
        return String.format(Locale.ROOT, pattern, args);
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    public static String dateNum(LocalDate d) {
        return d == null ? "" : f("%02d/%02d/%d", d.getDayOfMonth(), d.getMonthValue(), d.getYear());
    }

    public static String dateShort(LocalDate d) {
        return d == null ? "" : f("%02d %s %d", d.getDayOfMonth(), MONTHS_SHORT[d.getMonthValue() - 1], d.getYear());
    }

    public static String dayNum(LocalDate d) {
        return d == null ? "" : f("%02d", d.getDayOfMonth());
    }

    public static String monthAbbr(LocalDate d) {
        return d == null ? "" : MONTHS_SHORT[d.getMonthValue() - 1].replace(".", "").toUpperCase(Locale.ROOT);
    }

    public static String monthYear(LocalDate d) {
        return d == null ? "" : capitalize(MONTHS_LONG[d.getMonthValue() - 1]) + " " + d.getYear();
    }

    public static String monthName(LocalDate d) {
        return d == null ? "" : MONTHS_LONG[d.getMonthValue() - 1];
    }

    public static String longDay(LocalDate d) {
        if (d == null) return "";
        return capitalize(DAYS_LONG[d.getDayOfWeek().getValue() - 1]) + " " + d.getDayOfMonth() + " de " + MONTHS_LONG[d.getMonthValue() - 1];
    }

    public static String isoMonth(LocalDate d) {
        return d == null ? "" : f("%d-%02d", d.getYear(), d.getMonthValue());
    }

    public static String time12(LocalTime t) {
        if (t == null) return "";
        int h = t.getHour();
        int h12 = h % 12 == 0 ? 12 : h % 12;
        return f("%02d:%02d %s", h12, t.getMinute(), h < 12 ? "a.m." : "p.m.");
    }

    public static String dateTime24(LocalDateTime d) {
        return d == null ? "" : dateNum(d.toLocalDate()) + " · " + f("%02d:%02d", d.getHour(), d.getMinute());
    }

    public static String dateTime12(LocalDateTime d) {
        return d == null ? "" : dateNum(d.toLocalDate()) + " · " + time12(d.toLocalTime());
    }

    public static String hearingType(String type) {
        if (type == null) return "";
        return switch (type) {
            case "FIRST" -> "1.ª ponencia";
            case "SECOND" -> "2.ª ponencia";
            case "THIRD" -> "3.ª ponencia";
            case "ADDITIONAL" -> "Sesión adicional";
            default -> type;
        };
    }

    public static String appealStatus(String status) {
        return "CONCLUDED".equals(status) ? "Concluida" : "Nueva";
    }

    public static String hearingStatus(String status) {
        return "DONE".equals(status) ? "Concluida" : "Próxima";
    }

    public static String resolutionStatus(String status) {
        return "FINALIZED".equals(status) ? "Finalizada" : "Borrador guardado";
    }

    public static String fileSize(long bytes) {
        return f("%.1f MB", bytes / 1048576.0);
    }

    public static String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        return fullName.trim().split("\\s+")[0];
    }

    public static String greeting() {
        int hour = LocalTime.now().getHour();
        if (hour < 12) return "Buenos días";
        if (hour < 19) return "Buenas tardes";
        return "Buenas noches";
    }
}

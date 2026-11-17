package mx.edu.utez.judicial.oficialia.model;

import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The folio a demand request gets when it is registered: "S-", six digits counted per year, "/" and the
 * year, for example S-000127/2026.
 */
public final class RequestFolio {

    private static final Pattern FORMAT = Pattern.compile("S-(\\d{6})/(\\d{4})");

    private RequestFolio() {
    }

    public static String of(int number, int year) {
        return String.format("S-%06d/%d", number, year);
    }

    /** The number of a folio of that year; empty for a folio of another year or with another format. */
    public static OptionalInt number(String folio, int year) {
        Matcher matcher = FORMAT.matcher(folio == null ? "" : folio);
        if (!matcher.matches() || Integer.parseInt(matcher.group(2)) != year) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(Integer.parseInt(matcher.group(1)));
    }
}

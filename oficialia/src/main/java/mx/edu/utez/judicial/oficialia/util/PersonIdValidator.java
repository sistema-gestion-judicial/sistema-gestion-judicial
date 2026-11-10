package mx.edu.utez.judicial.oficialia.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Format checks for the Mexican RFC (people and companies) and CURP.
 */
public final class PersonIdValidator {

    private static final Pattern RFC_PERSON = Pattern.compile("^[A-ZÑ&]{4}\\d{6}[A-Z0-9]{3}$");
    private static final Pattern RFC_COMPANY = Pattern.compile("^[A-ZÑ&]{3}\\d{6}[A-Z0-9]{3}$");
    private static final Pattern CURP =
            Pattern.compile("^[A-Z][AEIOUX][A-Z]{2}\\d{6}[HM][A-Z]{2}[B-DF-HJ-NP-TV-Z]{3}[A-Z0-9]\\d$");

    private PersonIdValidator() {
    }

    /** Removes spaces and dashes and converts to upper case, so "tosa-850312 ml4" becomes "TOSA850312ML4". */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[\\s-]", "").toUpperCase(Locale.ROOT);
    }

    public static boolean isValid(String value) {
        String id = normalize(value);
        return RFC_PERSON.matcher(id).matches()
                || RFC_COMPANY.matcher(id).matches()
                || CURP.matcher(id).matches();
    }
}

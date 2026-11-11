package mx.edu.utez.judicial.oficialia.util;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Format check for email addresses. It does not require any particular domain.
 */
public final class EmailValidator {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private EmailValidator() {
    }

    /** Removes surrounding spaces and converts to lower case, so every spelling finds the same account. */
    public static String normalize(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean isValid(String email) {
        return EMAIL.matcher(normalize(email)).matches();
    }
}

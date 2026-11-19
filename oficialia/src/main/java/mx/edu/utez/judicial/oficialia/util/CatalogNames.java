package mx.edu.utez.judicial.oficialia.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Compares the names of catalog records the way the database does: ignoring case, accents and extra
 * spaces, so "Distrito judicial de Cuautla" and "distrito judicial de cuáutla" are the same name.
 */
public final class CatalogNames {

    private CatalogNames() {
    }

    /** The comparison key of a name; two names are the same name when their keys are equal. */
    public static String key(String name) {
        if (name == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return withoutAccents.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public static boolean sameName(String first, String second) {
        return key(first).equals(key(second));
    }
}

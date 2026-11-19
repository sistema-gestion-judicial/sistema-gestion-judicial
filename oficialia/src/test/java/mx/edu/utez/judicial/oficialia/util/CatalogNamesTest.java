package mx.edu.utez.judicial.oficialia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CatalogNamesTest {

    @Test
    void ignoresCaseAccentsAndExtraSpaces() {
        assertTrue(CatalogNames.sameName("Distrito judicial de Cuautla", "  distrito  JUDICIAL de cuáutla "));
    }

    @Test
    void tellsDifferentNamesApart() {
        assertFalse(CatalogNames.sameName("Distrito judicial de Cuautla", "Distrito judicial de Cuernavaca"));
    }

    @Test
    void treatsNullAsEmpty() {
        assertEquals("", CatalogNames.key(null));
    }
}

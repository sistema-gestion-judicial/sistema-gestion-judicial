package mx.edu.utez.judicial.oficialia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PersonIdValidatorTest {

    @Test
    void acceptsPersonAndCompanyRfc() {
        assertTrue(PersonIdValidator.isValid("TOSA850312ML4"));
        assertTrue(PersonIdValidator.isValid("MSU010203KL9"));
    }

    @Test
    void acceptsCurp() {
        assertTrue(PersonIdValidator.isValid("TOSA850312MMSRLN09"));
    }

    @Test
    void rejectsMalformedValues() {
        assertFalse(PersonIdValidator.isValid("TOSA85"));
        assertFalse(PersonIdValidator.isValid("1234567890123"));
        assertFalse(PersonIdValidator.isValid(""));
        assertFalse(PersonIdValidator.isValid(null));
    }

    @Test
    void normalizeRemovesSpacesAndDashesAndUppercases() {
        assertEquals("TOSA850312ML4", PersonIdValidator.normalize(" tosa-850312 ml4 "));
    }
}

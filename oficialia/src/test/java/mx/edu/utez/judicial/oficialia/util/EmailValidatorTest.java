package mx.edu.utez.judicial.oficialia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EmailValidatorTest {

    @Test
    void acceptsAddressesOfAnyDomain() {
        assertTrue(EmailValidator.isValid("lmendez@utez.edu.mx"));
        assertTrue(EmailValidator.isValid(" LMendez@ejemplo.com "));
    }

    @Test
    void rejectsMalformedAddresses() {
        assertFalse(EmailValidator.isValid("lmendez"));
        assertFalse(EmailValidator.isValid("lmendez@utez"));
        assertFalse(EmailValidator.isValid("l mendez@utez.edu.mx"));
        assertFalse(EmailValidator.isValid(null));
    }

    @Test
    void normalizesSpacesAndCase() {
        assertEquals("lmendez@utez.edu.mx", EmailValidator.normalize(" LMendez@UTEZ.edu.mx "));
    }
}

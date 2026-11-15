package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PasswordResetFormTest {

    private static Map<String, String> validate(String password, String confirmation) {
        PasswordResetForm form = new PasswordResetForm();
        form.setNewPassword(password);
        form.setConfirmation(confirmation);
        return form.validate();
    }

    @Test
    void acceptsEightCharactersWithLettersAndNumbers() {
        assertTrue(validate("Oficial1", "Oficial1").isEmpty());
    }

    @Test
    void rejectsPasswordsThatBreakTheFormat() {
        String format = "La contraseña debe tener al menos 8 caracteres, con letras y números.";

        assertEquals(format, validate("Ofic1", "Ofic1").get("newPassword"));
        assertEquals(format, validate("sololetras", "sololetras").get("newPassword"));
        assertEquals(format, validate("12345678", "12345678").get("newPassword"));
    }

    @Test
    void theConfirmationMustMatch() {
        assertEquals("La confirmación no coincide con la nueva contraseña.",
            validate("Oficial1", "Oficial2").get("confirmation"));
    }

    @Test
    void emptyFieldsAreMarked() {
        Map<String, String> errors = validate(null, null);

        assertEquals("Escriba la nueva contraseña.", errors.get("newPassword"));
        assertEquals("Confirme la nueva contraseña.", errors.get("confirmation"));
    }

    @Test
    void passwordsAreNotTrimmed() {
        assertFalse(validate("Oficial1 ", "Oficial1").isEmpty());
    }
}

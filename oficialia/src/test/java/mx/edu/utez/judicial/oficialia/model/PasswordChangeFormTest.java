package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class PasswordChangeFormTest {

    private static Map<String, String> validate(String password, String confirmation) {
        PasswordChangeForm form = new PasswordChangeForm();
        form.setNewPassword(password);
        form.setConfirmation(confirmation);
        return form.validate();
    }

    @Test
    void acceptsEightCharactersWithLettersAndNumbers() {
        assertTrue(validate("abcd1234", "abcd1234").isEmpty());
    }

    @Test
    void asksForBothFieldsWhenEmpty() {
        Map<String, String> errors = validate(null, null);

        assertTrue(errors.containsKey("newPassword"));
        assertTrue(errors.containsKey("confirmation"));
    }

    @Test
    void rejectsShortPasswordsAndPasswordsWithoutLettersOrNumbers() {
        assertTrue(validate("abc123", "abc123").containsKey("newPassword"));
        assertTrue(validate("abcdefgh", "abcdefgh").containsKey("newPassword"));
        assertTrue(validate("12345678", "12345678").containsKey("newPassword"));
    }

    @Test
    void rejectsAConfirmationThatDoesNotMatch() {
        Map<String, String> errors = validate("abcd1234", "abcd12345");

        assertEquals(1, errors.size());
        assertTrue(errors.containsKey("confirmation"));
    }

    @Test
    void doesNotTrimPasswords() {
        assertTrue(validate("abcd1234 ", "abcd1234").containsKey("confirmation"));
    }
}

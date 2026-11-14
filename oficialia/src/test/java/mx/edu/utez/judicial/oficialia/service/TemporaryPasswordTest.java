package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;
import mx.edu.utez.judicial.oficialia.model.PasswordChangeForm;
import org.junit.jupiter.api.Test;

class TemporaryPasswordTest {

    @Test
    void alwaysHasLettersAndNumbersAndTheRightLength() {
        Random random = new Random(42);
        for (int i = 0; i < 200; i++) {
            String password = TemporaryPassword.generate(random);

            assertEquals(TemporaryPassword.LENGTH, password.length());
            assertTrue(password.chars().anyMatch(Character::isLetter), password);
            assertTrue(password.chars().anyMatch(Character::isDigit), password);
        }
    }

    @Test
    void passesTheRulesOfAPasswordChosenByTheUser() {
        PasswordChangeForm form = new PasswordChangeForm();
        String password = TemporaryPassword.generate();
        form.setNewPassword(password);
        form.setConfirmation(password);

        assertTrue(form.validate().isEmpty());
    }

    @Test
    void avoidsLookAlikeCharacters() {
        Random random = new Random(7);
        for (int i = 0; i < 200; i++) {
            assertTrue(TemporaryPassword.generate(random).chars().noneMatch(c -> "01OlI".indexOf(c) >= 0));
        }
    }

    @Test
    void producesADifferentPasswordEachTime() {
        assertNotEquals(TemporaryPassword.generate(), TemporaryPassword.generate());
    }
}

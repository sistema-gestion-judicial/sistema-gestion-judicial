package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class UserAccountTest {

    @Test
    void toStringDoesNotExposeThePasswordHash() {
        String hash = "600000:c2FsdA==:aGFzaA==";
        User user = new User("ana@utez.edu.mx", "Ana Ruiz", UserRole.CLERK, "Juzgado de Cuautla");

        String text = new UserAccount(user, hash, true).toString();

        assertFalse(text.contains(hash));
        assertTrue(text.contains("ana@utez.edu.mx"));
    }
}

package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class UserTest {

    private static User named(String fullName) {
        return new User("l@utez.edu.mx", fullName, UserRole.CLERK, "Juzgado de Cuautla");
    }

    @Test
    void takesTheFirstLettersOfTheFirstTwoWords() {
        assertEquals("LM", named("Laura Méndez Ruiz").getInitials());
        assertEquals("ÁR", named("  álvaro   ríos ").getInitials());
    }

    @Test
    void keepsOneLetterForASingleWordAndNoneWithoutAName() {
        assertEquals("E", named("Elena").getInitials());
        assertEquals("", named(" ").getInitials());
        assertEquals("", named(null).getInitials());
    }
}

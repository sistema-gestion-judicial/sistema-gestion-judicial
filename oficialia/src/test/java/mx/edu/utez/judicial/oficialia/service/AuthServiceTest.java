package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private static final String EMAIL = "ana@utez.edu.mx";
    private static final String PASSWORD = "test-password";
    private static final String HASH = PasswordHasher.hash(PASSWORD);

    private final AuthService service = new AuthService(new InMemoryUserDao(List.of(
            new UserAccount(new User(EMAIL, "Ana Ruiz", UserRole.CLERK, "Juzgado de Cuautla"), HASH))));

    @Test
    void acceptsTheRightPasswordIgnoringEmailCase() {
        assertEquals(EMAIL, service.authenticate(" ANA@utez.edu.mx ", PASSWORD).orElseThrow().getEmail());
    }

    @Test
    void rejectsAWrongPassword() {
        assertTrue(service.authenticate(EMAIL, "other-password").isEmpty());
    }

    @Test
    void rejectsAnUnknownEmail() {
        assertTrue(service.authenticate("nobody@utez.edu.mx", PASSWORD).isEmpty());
    }

    @Test
    void treatsANullPasswordAsInvalidCredentials() {
        assertTrue(service.authenticate(EMAIL, null).isEmpty());
    }
}

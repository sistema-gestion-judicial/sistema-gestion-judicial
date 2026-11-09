package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.model.SignInResult;
import mx.edu.utez.judicial.oficialia.model.SignInResult.Status;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

    private static final String EMAIL = "ana@utez.edu.mx";
    private static final String INACTIVE_EMAIL = "beto@utez.edu.mx";
    private static final String PASSWORD = "test-password";
    private static final String WRONG_PASSWORD = "other-password";
    private static final String HASH = PasswordHasher.hash(PASSWORD);

    private final AuthService service = new AuthService(new InMemoryUserDao(List.of(
            account(EMAIL, true),
            account(INACTIVE_EMAIL, false))));

    @Test
    void acceptsTheRightPasswordIgnoringEmailCase() {
        SignInResult result = service.signIn(" ANA@utez.edu.mx ", PASSWORD);

        assertEquals(Status.SIGNED_IN, result.status());
        assertEquals(EMAIL, result.user().getEmail());
    }

    @Test
    void rejectsAWrongPassword() {
        assertEquals(Status.INVALID_CREDENTIALS, service.signIn(EMAIL, WRONG_PASSWORD).status());
    }

    @Test
    void rejectsAnUnknownEmail() {
        assertEquals(Status.INVALID_CREDENTIALS, service.signIn("nobody@utez.edu.mx", PASSWORD).status());
    }

    @Test
    void treatsANullPasswordAsInvalidCredentials() {
        assertEquals(Status.INVALID_CREDENTIALS, service.signIn(EMAIL, null).status());
    }

    @Test
    void reportsAnInactiveAccountOnlyAfterTheRightPassword() {
        assertEquals(Status.INACTIVE_ACCOUNT, service.signIn(INACTIVE_EMAIL, PASSWORD).status());
        assertEquals(Status.INVALID_CREDENTIALS, service.signIn(INACTIVE_EMAIL, WRONG_PASSWORD).status());
    }

    private static UserAccount account(String email, boolean active) {
        return new UserAccount(new User(email, "Test User", UserRole.CLERK, "Juzgado de Cuautla"), HASH, active);
    }
}

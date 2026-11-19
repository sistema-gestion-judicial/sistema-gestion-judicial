package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.model.PasswordChangeForm;
import mx.edu.utez.judicial.oficialia.model.SignInResult.Status;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

/**
 * Replacing the temporary password of a newly registered user (RF 1.4).
 */
class AuthServicePasswordChangeTest {

    private static final String EMAIL = "ana@utez.edu.mx";
    private static final String TEMPORARY = "Temporal2026";

    private final InMemoryUserDao users = new InMemoryUserDao(List.of(new UserAccount(
        new User(EMAIL, "Ana Ruiz", UserRole.CLERK, "Juzgado de Cuautla", true),
        PasswordHasher.hash(TEMPORARY), true)));
    private final AuthService service =
        new AuthService(users, new LoginAttemptTracker(() -> Instant.parse("2026-11-09T10:00:00Z")));

    private static PasswordChangeForm form(String password, String confirmation) {
        PasswordChangeForm form = new PasswordChangeForm();
        form.setNewPassword(password);
        form.setConfirmation(confirmation);
        return form;
    }

    private User signedInUser() {
        return service.signIn(EMAIL, TEMPORARY).user();
    }

    @Test
    void signingInWithTheTemporaryPasswordMarksTheSessionUserAsNeedingAChange() {
        assertTrue(signedInUser().isMustChangePassword());
    }

    @Test
    void replacesThePasswordAndEndsTheTemporaryState() {
        Map<String, String> errors = service.changeTemporaryPassword(signedInUser(), form("Definitiva2026", "Definitiva2026"));

        assertTrue(errors.isEmpty());
        assertEquals(Status.SIGNED_IN, service.signIn(EMAIL, "Definitiva2026").status());
        assertFalse(service.signIn(EMAIL, "Definitiva2026").user().isMustChangePassword());
        assertEquals(Status.INVALID_CREDENTIALS, service.signIn(EMAIL, TEMPORARY).status());
    }

    @Test
    void keepsTheTemporaryPasswordWhenTheNewOneHasErrors() {
        Map<String, String> errors = service.changeTemporaryPassword(signedInUser(), form("short1", "short1"));

        assertTrue(errors.containsKey("newPassword"));
        assertEquals(Status.SIGNED_IN, service.signIn(EMAIL, TEMPORARY).status());
        assertTrue(signedInUser().isMustChangePassword());
    }

    @Test
    void rejectsKeepingTheTemporaryPassword() {
        Map<String, String> errors = service.changeTemporaryPassword(signedInUser(), form(TEMPORARY, TEMPORARY));

        assertEquals("La nueva contraseña debe ser distinta de la temporal.", errors.get("newPassword"));
        assertTrue(signedInUser().isMustChangePassword());
    }

    @Test
    void rejectsAConfirmationThatDoesNotMatch() {
        Map<String, String> errors = service.changeTemporaryPassword(signedInUser(), form("Definitiva2026", "Otra2026abc"));

        assertTrue(errors.containsKey("confirmation"));
    }
}

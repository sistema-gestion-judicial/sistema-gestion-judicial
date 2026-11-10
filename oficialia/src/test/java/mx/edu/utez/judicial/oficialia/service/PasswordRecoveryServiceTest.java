package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import mx.edu.utez.judicial.oficialia.dao.InMemoryRecoveryCodeDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.model.SignInResult.Status;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class PasswordRecoveryServiceTest {

    private static final String EMAIL = "ana@utez.edu.mx";
    private static final String INACTIVE_EMAIL = "beto@utez.edu.mx";
    private static final String OLD_PASSWORD = "Vieja2026";
    private static final String NEW_PASSWORD = "Nueva2026";
    private static final String OLD_HASH = PasswordHasher.hash(OLD_PASSWORD);
    private static final String CODE = "123456";
    private static final String NEXT_CODE = "654321";

    private final InMemoryUserDao users = new InMemoryUserDao(List.of(account(EMAIL, true),
        account(INACTIVE_EMAIL, false)));
    private final InMemoryRecoveryCodeDao codes = new InMemoryRecoveryCodeDao();
    private final MutableClock clock = new MutableClock();
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(clock::instant);
    private final AuthService auth = new AuthService(users, tracker);
    private final List<String> sentTo = new ArrayList<>();
    private final List<String> sentHtml = new ArrayList<>();
    private final List<String> generated = new ArrayList<>(List.of(CODE, NEXT_CODE));

    private PasswordRecoveryService service() {
        return serviceWithMailFailure(false);
    }

    private PasswordRecoveryService serviceWithMailFailure(boolean fails) {
        EmailService emailService = new EmailService((to, subject, html) -> {
            if (fails) {
                throw new ServiceUnavailableException("mail server down");
            }
            sentTo.add(to);
            sentHtml.add(html);
        });
        // The executor runs the sending right away, so the tests need no waiting
        return new PasswordRecoveryService(users, codes, emailService, tracker, clock,
            () -> generated.remove(0), Runnable::run);
    }

    @Test
    void anActiveAccountReceivesTheCodeByEmail() {
        service().requestCode("  ANA@utez.edu.mx ");

        assertEquals(List.of(EMAIL), sentTo);
        assertTrue(sentHtml.get(0).contains(CODE));
        assertTrue(sentHtml.get(0).contains("vence en 15 minutos"));
    }

    @Test
    void unknownAndInactiveEmailsGetNothingAndNoError() {
        PasswordRecoveryService service = service();

        service.requestCode("nobody@utez.edu.mx");
        service.requestCode(INACTIVE_EMAIL);
        service.requestCode(null);

        assertTrue(sentTo.isEmpty());
        assertThrows(BusinessRuleException.class, () -> service.verifyCode(INACTIVE_EMAIL, CODE));
    }

    @Test
    void aMailFailureIsNotShownToTheCaller() {
        PasswordRecoveryService service = serviceWithMailFailure(true);

        service.requestCode(EMAIL);

        service.verifyCode(EMAIL, CODE);
    }

    @Test
    void aCorrectCodeLetsTheUserChooseANewPasswordAndOnlyOnce() {
        PasswordRecoveryService service = service();
        service.requestCode(EMAIL);

        service.verifyCode(EMAIL, CODE);
        service.resetPassword(EMAIL, CODE, NEW_PASSWORD);

        assertEquals(Status.SIGNED_IN, auth.signIn(EMAIL, NEW_PASSWORD).status());
        assertEquals(Status.INVALID_CREDENTIALS, auth.signIn(EMAIL, OLD_PASSWORD).status());
        assertThrows(BusinessRuleException.class, () -> service.resetPassword(EMAIL, CODE, "Otra2026abc"));
        assertThrows(BusinessRuleException.class, () -> service.verifyCode(EMAIL, CODE));
    }

    @Test
    void thePasswordIsStoredAsAHashNeverAsText() {
        PasswordRecoveryService service = service();
        service.requestCode(EMAIL);

        service.resetPassword(EMAIL, CODE, NEW_PASSWORD);

        String stored = users.findByEmail(EMAIL).orElseThrow().passwordHash();
        assertFalse(stored.contains(NEW_PASSWORD));
        assertTrue(PasswordHasher.matches(NEW_PASSWORD, stored));
    }

    @Test
    void verifyingTheCodeDoesNotUseItUp() {
        PasswordRecoveryService service = service();
        service.requestCode(EMAIL);

        service.verifyCode(EMAIL, CODE);

        service.resetPassword(EMAIL, CODE, NEW_PASSWORD);
    }

    @Test
    void theCodeLastsFifteenMinutesAndThenTheOldPasswordStillWorks() {
        PasswordRecoveryService service = service();
        service.requestCode(EMAIL);

        clock.advanceMinutes(AuthSettings.RECOVERY_CODE_MINUTES - 1);
        service.verifyCode(EMAIL, CODE);

        clock.advanceMinutes(1);
        assertThrows(BusinessRuleException.class, () -> service.verifyCode(EMAIL, CODE));
        assertThrows(BusinessRuleException.class, () -> service.resetPassword(EMAIL, CODE, NEW_PASSWORD));
        assertEquals(Status.SIGNED_IN, auth.signIn(EMAIL, OLD_PASSWORD).status());
    }

    @Test
    void aNewRequestReplacesThePreviousCode() {
        PasswordRecoveryService service = service();
        service.requestCode(EMAIL);
        service.requestCode(EMAIL);

        assertThrows(BusinessRuleException.class, () -> service.verifyCode(EMAIL, CODE));
        service.verifyCode(EMAIL, NEXT_CODE);
    }

    @Test
    void theCodeIsCancelledAfterTooManyWrongGuesses() {
        PasswordRecoveryService service = service();
        service.requestCode(EMAIL);

        for (int i = 0; i < AuthSettings.RECOVERY_MAX_FAILED_ATTEMPTS; i++) {
            assertThrows(BusinessRuleException.class, () -> service.verifyCode(EMAIL, "000000"));
        }

        assertThrows(BusinessRuleException.class, () -> service.verifyCode(EMAIL, CODE));
    }

    @Test
    void everyFailureHasTheSameMessageSoNothingIsRevealed() {
        PasswordRecoveryService service = service();
        service.requestCode(EMAIL);

        String wrongCode = assertThrows(BusinessRuleException.class,
            () -> service.verifyCode(EMAIL, "000000")).getMessage();
        String unknownEmail = assertThrows(BusinessRuleException.class,
            () -> service.verifyCode("nobody@utez.edu.mx", "000000")).getMessage();

        assertEquals(wrongCode, unknownEmail);
    }

    @Test
    void recoveringThePasswordLiftsTheSignInLock() {
        for (int i = 0; i < AuthSettings.MAX_FAILED_ATTEMPTS; i++) {
            auth.signIn(EMAIL, "wrong");
        }
        assertEquals(Status.LOCKED, auth.signIn(EMAIL, OLD_PASSWORD).status());
        PasswordRecoveryService service = service();
        service.requestCode(EMAIL);

        service.resetPassword(EMAIL, CODE, NEW_PASSWORD);

        assertEquals(Status.SIGNED_IN, auth.signIn(EMAIL, NEW_PASSWORD).status());
    }

    @Test
    void theCodeHasSixDigits() {
        assertTrue(PasswordRecoveryService.isWellFormedCode("012345"));
        assertFalse(PasswordRecoveryService.isWellFormedCode("12345"));
        assertFalse(PasswordRecoveryService.isWellFormedCode("1234567"));
        assertFalse(PasswordRecoveryService.isWellFormedCode("12a456"));
        assertFalse(PasswordRecoveryService.isWellFormedCode(null));
    }

    private static UserAccount account(String email, boolean active) {
        return new UserAccount(new User(email, "Ana Ruiz", UserRole.CLERK, "Juzgado de Cuautla"), OLD_HASH, active);
    }

    /** A clock the test can move forward. */
    private static final class MutableClock extends Clock {

        private Instant now = Instant.parse("2026-11-10T18:00:00Z");

        void advanceMinutes(long minutes) {
            now = now.plusSeconds(minutes * 60);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}

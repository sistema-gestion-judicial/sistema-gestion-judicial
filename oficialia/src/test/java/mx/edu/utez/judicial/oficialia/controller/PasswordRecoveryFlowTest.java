package mx.edu.utez.judicial.oficialia.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.InMemoryRecoveryCodeDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.model.SignInResult.Status;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.service.AuthService;
import mx.edu.utez.judicial.oficialia.service.EmailService;
import mx.edu.utez.judicial.oficialia.service.LoginAttemptTracker;
import mx.edu.utez.judicial.oficialia.service.PasswordHasher;
import mx.edu.utez.judicial.oficialia.service.PasswordRecoveryService;
import org.junit.jupiter.api.Test;

/** Walks the three recovery pages the way a browser would, with the real service and in-memory data. */
class PasswordRecoveryFlowTest {

    private static final String EMAIL = "ana@utez.edu.mx";
    private static final String CODE = "123456";
    private static final String OLD_PASSWORD = "Vieja2026";
    private static final String NEW_PASSWORD = "Nueva2026";
    private static final String OLD_HASH = PasswordHasher.hash(OLD_PASSWORD);
    private static final Instant NOW = Instant.parse("2026-11-10T18:00:00Z");

    private final InMemoryUserDao users = new InMemoryUserDao(List.of(new UserAccount(
        new User(EMAIL, "Ana Ruiz", UserRole.CLERK, "Juzgado de Cuautla"), OLD_HASH, true)));
    private final List<String> sentTo = new ArrayList<>();
    private final Instant[] now = {NOW};
    private final LoginAttemptTracker tracker = new LoginAttemptTracker(() -> now[0]);
    private final PasswordRecoveryService service = new PasswordRecoveryService(users,
        new InMemoryRecoveryCodeDao(), new EmailService((to, subject, html) -> sentTo.add(to)), tracker,
        new MovableClock(), () -> CODE, Runnable::run);

    private final PasswordRecoveryRequestServlet requestPage = new PasswordRecoveryRequestServlet(service);
    private final PasswordRecoveryCodeServlet codePage = new PasswordRecoveryCodeServlet(service);
    private final PasswordRecoveryResetServlet resetPage = new PasswordRecoveryResetServlet(service);

    // What the browser keeps between requests
    private final Map<String, Object> session = new HashMap<>();
    private boolean sessionExists;
    // What the last request produced
    private String forwardedTo;
    private String redirectedTo;
    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();

    @Test
    void recoversThePasswordFromTheEmailToTheLogin() throws Exception {
        requestPage.doPost(request(Map.of("email", " ANA@utez.edu.mx ")), response());
        assertEquals("/ctx/password-recovery/code", redirectedTo);
        assertEquals(List.of(EMAIL), sentTo);

        codePage.doPost(request(Map.of("code", CODE)), response());
        assertEquals("/ctx/password-recovery/reset", redirectedTo);

        resetPage.doPost(request(Map.of("newPassword", NEW_PASSWORD, "confirmation", NEW_PASSWORD)), response());
        assertEquals("/ctx/login?passwordChanged", redirectedTo);

        AuthService auth = new AuthService(users, tracker);
        assertEquals(Status.SIGNED_IN, auth.signIn(EMAIL, NEW_PASSWORD).status());
        assertEquals(Status.INVALID_CREDENTIALS, auth.signIn(EMAIL, OLD_PASSWORD).status());
        assertNull(session.get("recoveryEmail"));
        assertNull(session.get("recoveryCode"));
    }

    @Test
    void anUnknownEmailGetsTheSameAnswerAndNoMessage() throws Exception {
        requestPage.doPost(request(Map.of("email", "nobody@utez.edu.mx")), response());

        assertEquals("/ctx/password-recovery/code", redirectedTo);
        assertTrue(sentTo.isEmpty());
    }

    @Test
    void aRepeatedRequestGetsTheSameAnswerButNoSecondMessage() throws Exception {
        requestPage.doPost(request(Map.of("email", EMAIL)), response());
        assertEquals("/ctx/password-recovery/code", redirectedTo);

        requestPage.doPost(request(Map.of("email", EMAIL)), response());

        assertEquals("/ctx/password-recovery/code", redirectedTo);
        assertEquals(List.of(EMAIL), sentTo);
    }

    @Test
    void aMalformedEmailMarksTheFieldAndSendsNothing() throws Exception {
        requestPage.doPost(request(Map.of("email", "sin-arroba")), response());

        assertEquals(RecoveryFlow.REQUEST_VIEW, forwardedTo);
        assertNotNull(errors().get("email"));
        assertTrue(sentTo.isEmpty());
    }

    @Test
    void aBlankEmailMarksTheField() throws Exception {
        requestPage.doPost(request(Map.of("email", "  ")), response());

        assertNotNull(errors().get("email"));
    }

    @Test
    void theCodePageNeedsAnEmailFirst() throws Exception {
        codePage.doGet(request(Map.of()), response());

        assertEquals("/ctx/password-recovery", redirectedTo);
    }

    @Test
    void aMalformedCodeMarksTheFieldWithoutCountingAsAGuess() throws Exception {
        requestPage.doPost(request(Map.of("email", EMAIL)), response());

        codePage.doPost(request(Map.of("code", "12")), response());

        assertEquals(RecoveryFlow.CODE_VIEW, forwardedTo);
        assertNotNull(errors().get("code"));
    }

    @Test
    void aWrongCodeShowsTheMessageAndKeepsTheUserOnTheCodePage() throws Exception {
        requestPage.doPost(request(Map.of("email", EMAIL)), response());

        codePage.doPost(request(Map.of("code", "000000")), response());

        assertEquals(RecoveryFlow.CODE_VIEW, forwardedTo);
        assertTrue(String.valueOf(attributes.get("errorMessage")).contains("Solicite uno nuevo"));
        assertNull(session.get("recoveryCode"));
    }

    @Test
    void theResetPageStaysClosedUntilTheCodeIsVerified() throws Exception {
        requestPage.doPost(request(Map.of("email", EMAIL)), response());

        resetPage.doGet(request(Map.of()), response());
        assertEquals("/ctx/password-recovery", redirectedTo);

        resetPage.doPost(request(Map.of("newPassword", NEW_PASSWORD, "confirmation", NEW_PASSWORD)), response());
        assertEquals("/ctx/password-recovery", redirectedTo);
        assertEquals(Status.SIGNED_IN, new AuthService(users, tracker).signIn(EMAIL, OLD_PASSWORD).status());
    }

    @Test
    void aWeakOrMismatchedPasswordMarksTheFieldAndIsNotSaved() throws Exception {
        verifiedCode();

        resetPage.doPost(request(Map.of("newPassword", "corta1", "confirmation", "corta1")), response());
        assertNotNull(errors().get("newPassword"));

        resetPage.doPost(request(Map.of("newPassword", NEW_PASSWORD, "confirmation", "Otra2026abc")), response());
        assertNotNull(errors().get("confirmation"));

        assertEquals(RecoveryFlow.RESET_VIEW, forwardedTo);
        assertEquals(Status.SIGNED_IN, new AuthService(users, tracker).signIn(EMAIL, OLD_PASSWORD).status());
    }

    @Test
    void ifTheCodeExpiresWhileTypingThePasswordTheUserGoesBackToAskForAnother() throws Exception {
        verifiedCode();
        now[0] = NOW.plusSeconds(15 * 60);

        resetPage.doPost(request(Map.of("newPassword", NEW_PASSWORD, "confirmation", NEW_PASSWORD)), response());

        assertEquals(RecoveryFlow.CODE_VIEW, forwardedTo);
        assertTrue(String.valueOf(attributes.get("errorMessage")).contains("Solicite uno nuevo"));
        assertNull(session.get("recoveryCode"));
        assertEquals(Status.SIGNED_IN, new AuthService(users, tracker).signIn(EMAIL, OLD_PASSWORD).status());
    }

    @Test
    void everyPageIsKeptOutOfTheBrowserCache() throws Exception {
        requestPage.doGet(request(Map.of()), response());
        assertEquals("no-store", headers.get("Cache-Control"));

        headers.clear();
        requestPage.doPost(request(Map.of("email", EMAIL)), response());
        codePage.doGet(request(Map.of()), response());
        assertEquals("no-store", headers.get("Cache-Control"));

        headers.clear();
        codePage.doPost(request(Map.of("code", CODE)), response());
        resetPage.doGet(request(Map.of()), response());
        assertEquals(RecoveryFlow.RESET_VIEW, forwardedTo);
        assertEquals("no-store", headers.get("Cache-Control"));
    }

    @Test
    void theViewsShowTheRulesFromTheSettings() throws Exception {
        requestPage.doGet(request(Map.of()), response());

        assertEquals(6, attributes.get("codeLength"));
        assertEquals(15, attributes.get("codeMinutes"));
        assertEquals(8, attributes.get("passwordMinLength"));
        assertEquals(60, attributes.get("cooldownSeconds"));
        assertFalse(sessionExists, "opening the first page must not create a session");
    }

    private void verifiedCode() throws Exception {
        requestPage.doPost(request(Map.of("email", EMAIL)), response());
        codePage.doPost(request(Map.of("code", CODE)), response());
        assertEquals("/ctx/password-recovery/reset", redirectedTo);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> errors() {
        return (Map<String, String>) attributes.get("errors");
    }

    private HttpServletRequest request(Map<String, String> parameters) {
        attributes.clear();
        forwardedTo = null;
        redirectedTo = null;
        HttpSession httpSession = stub(HttpSession.class, (proxy, method, args) -> switch (method.getName()) {
            case "getAttribute" -> session.get((String) args[0]);
            case "setAttribute" -> session.put((String) args[0], args[1]);
            case "removeAttribute" -> session.remove((String) args[0]);
            default -> null;
        });
        RequestDispatcher dispatcher = stub(RequestDispatcher.class, (proxy, method, args) -> null);
        return stub(HttpServletRequest.class, (proxy, method, args) -> switch (method.getName()) {
            case "getParameter" -> parameters.get((String) args[0]);
            case "setAttribute" -> attributes.put((String) args[0], args[1]);
            case "getContextPath" -> "/ctx";
            case "getSession" -> {
                boolean create = args == null || (Boolean) args[0];
                if (create) {
                    sessionExists = true;
                }
                yield sessionExists ? httpSession : null;
            }
            case "getRequestDispatcher" -> {
                forwardedTo = (String) args[0];
                yield dispatcher;
            }
            default -> null;
        });
    }

    private HttpServletResponse response() {
        return stub(HttpServletResponse.class, (proxy, method, args) -> {
            switch (method.getName()) {
                case "setHeader" -> headers.put((String) args[0], (String) args[1]);
                case "sendRedirect" -> redirectedTo = (String) args[0];
                default -> { }
            }
            return null;
        });
    }

    private static <T> T stub(Class<T> type, InvocationHandler handler) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type}, handler));
    }

    /** Tells the time of the test, which a test moves by changing the array. */
    private final class MovableClock extends Clock {

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now[0];
        }
    }
}

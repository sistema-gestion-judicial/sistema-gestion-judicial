package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.Duration;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;

/**
 * What the three password recovery pages share: the session keys that carry the flow from one page to the next,
 * the views and the way they are shown. The e-mail and the verified code live in the session, so the user does
 * not have to retype them.
 */
final class RecoveryFlow {

    static final String REQUEST_VIEW = "/WEB-INF/views/auth/recovery-request.jsp";
    static final String CODE_VIEW = "/WEB-INF/views/auth/recovery-code.jsp";
    static final String RESET_VIEW = "/WEB-INF/views/auth/recovery-reset.jsp";

    private static final String SESSION_EMAIL = "recoveryEmail";
    private static final String SESSION_CODE = "recoveryCode";

    private RecoveryFlow() {
    }

    /** Starts the flow again for an e-mail, so a code verified earlier cannot be reused with another one. */
    static void start(HttpServletRequest request, String email) {
        clear(request);
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval((int) Duration.ofMinutes(AuthSettings.SESSION_TIMEOUT_MINUTES).toSeconds());
        session.setAttribute(SESSION_EMAIL, email);
    }

    /** The e-mail the code was requested for, or null when this session has not asked for one. */
    static String email(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(SESSION_EMAIL);
    }

    /** The code that was already verified, or null when it was not. */
    static String verifiedCode(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null ? null : (String) session.getAttribute(SESSION_CODE);
    }

    static void rememberVerifiedCode(HttpServletRequest request, String code) {
        request.getSession().setAttribute(SESSION_CODE, code);
    }

    static void forgetVerifiedCode(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_CODE);
        }
    }

    /** Forgets the e-mail and the code once the flow ends. */
    static void clear(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute(SESSION_EMAIL);
            session.removeAttribute(SESSION_CODE);
        }
    }

    static void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    /** Shows a page with the values the three views print. */
    static void show(HttpServletRequest request, HttpServletResponse response, String view)
        throws ServletException, IOException {
        request.setAttribute("codeLength", AuthSettings.RECOVERY_CODE_LENGTH);
        request.setAttribute("codeMinutes", AuthSettings.RECOVERY_CODE_MINUTES);
        request.setAttribute("passwordMinLength", AuthSettings.PASSWORD_MIN_LENGTH);
        request.setAttribute("cooldownSeconds", AuthSettings.RECOVERY_REQUEST_COOLDOWN_SECONDS);
        // Same as the login: the back button must not bring back a form with a code or a password typed in it
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher(view).forward(request, response);
    }
}

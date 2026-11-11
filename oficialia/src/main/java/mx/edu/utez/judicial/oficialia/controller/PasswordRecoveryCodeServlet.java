package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.service.PasswordRecoveryService;

/**
 * Second step of the password recovery (RF 1.3): the user types the code received by e-mail. A correct and
 * valid code opens the page to type the new password.
 */
@WebServlet(PasswordRecoveryCodeServlet.PATH)
public class PasswordRecoveryCodeServlet extends HttpServlet {

    public static final String PATH = "/password-recovery/code";

    private final PasswordRecoveryService recoveryService;

    public PasswordRecoveryCodeServlet() {
        this(new PasswordRecoveryService());
    }

    PasswordRecoveryCodeServlet(PasswordRecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String email = RecoveryFlow.email(request);
        if (email == null) {
            RecoveryFlow.redirect(request, response, PasswordRecoveryRequestServlet.PATH);
            return;
        }
        showPage(request, response, email, Map.of(), null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String email = RecoveryFlow.email(request);
        if (email == null) {
            RecoveryFlow.redirect(request, response, PasswordRecoveryRequestServlet.PATH);
            return;
        }
        String code = request.getParameter("code") == null ? "" : request.getParameter("code").trim();

        if (!PasswordRecoveryService.isWellFormedCode(code)) {
            String message = "Escriba el código de " + AuthSettings.RECOVERY_CODE_LENGTH
                + " dígitos que recibió por correo.";
            showPage(request, response, email, Map.of("code", message), null);
            return;
        }

        try {
            recoveryService.verifyCode(email, code);
        } catch (BusinessRuleException e) {
            showPage(request, response, email, Map.of(), e.getMessage());
            return;
        }
        RecoveryFlow.rememberVerifiedCode(request, code);
        RecoveryFlow.redirect(request, response, PasswordRecoveryResetServlet.PATH);
    }

    /** Also used by the reset page when the code expired while the user was typing the password. */
    static void showPage(HttpServletRequest request, HttpServletResponse response, String email,
                         Map<String, String> errors, String errorMessage) throws ServletException, IOException {
        request.setAttribute("email", email);
        request.setAttribute("errors", errors);
        request.setAttribute("errorMessage", errorMessage);
        RecoveryFlow.show(request, response, RecoveryFlow.CODE_VIEW);
    }
}

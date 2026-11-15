package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.model.PasswordResetForm;
import mx.edu.utez.judicial.oficialia.service.PasswordRecoveryService;

/**
 * Last step of the password recovery (RF 1.3): the user types the new password, which is saved and sends the
 * user to the login. Only reachable after the code was verified.
 */
@WebServlet(PasswordRecoveryResetServlet.PATH)
public class PasswordRecoveryResetServlet extends HttpServlet {

    public static final String PATH = "/password-recovery/reset";

    private final PasswordRecoveryService recoveryService;

    public PasswordRecoveryResetServlet() {
        this(new PasswordRecoveryService());
    }

    PasswordRecoveryResetServlet(PasswordRecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        if (RecoveryFlow.verifiedCode(request) == null) {
            RecoveryFlow.redirect(request, response, PasswordRecoveryRequestServlet.PATH);
            return;
        }
        RecoveryFlow.show(request, response, RecoveryFlow.RESET_VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String email = RecoveryFlow.email(request);
        String code = RecoveryFlow.verifiedCode(request);
        if (email == null || code == null) {
            RecoveryFlow.redirect(request, response, PasswordRecoveryRequestServlet.PATH);
            return;
        }

        PasswordResetForm form = new PasswordResetForm();
        form.setNewPassword(request.getParameter("newPassword"));
        form.setConfirmation(request.getParameter("confirmation"));
        Map<String, String> errors = form.validate();
        if (!errors.isEmpty()) {
            request.setAttribute("errors", errors);
            RecoveryFlow.show(request, response, RecoveryFlow.RESET_VIEW);
            return;
        }

        try {
            recoveryService.resetPassword(email, code, form.getNewPassword());
        } catch (BusinessRuleException e) {
            // The code expired while the user was typing: back to the code page to ask for a new one
            RecoveryFlow.forgetVerifiedCode(request);
            PasswordRecoveryCodeServlet.showPage(request, response, email, Map.of(), e.getMessage());
            return;
        }

        RecoveryFlow.clear(request);
        response.sendRedirect(request.getContextPath() + "/login?passwordChanged");
    }
}

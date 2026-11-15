package mx.edu.utez.judicial.oficialia.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.service.PasswordRecoveryService;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * First step of the password recovery (RF 1.3): the user types the e-mail of the account and receives a code.
 * The answer is the same for every well-formed e-mail, registered or not.
 */
@WebServlet(PasswordRecoveryRequestServlet.PATH)
public class PasswordRecoveryRequestServlet extends HttpServlet {

    public static final String PATH = "/password-recovery";

    private final PasswordRecoveryService recoveryService;

    public PasswordRecoveryRequestServlet() {
        this(new PasswordRecoveryService());
    }

    PasswordRecoveryRequestServlet(PasswordRecoveryService recoveryService) {
        this.recoveryService = recoveryService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        RecoveryFlow.show(request, response, RecoveryFlow.REQUEST_VIEW);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String email = request.getParameter("email");

        if (email == null || email.isBlank()) {
            showError(request, response, email, "Escriba su correo electrónico.");
            return;
        }
        if (!EmailValidator.isValid(email)) {
            showError(request, response, email, "Escriba un correo con formato válido.");
            return;
        }

        recoveryService.requestCode(email);

        RecoveryFlow.start(request, EmailValidator.normalize(email));
        RecoveryFlow.redirect(request, response, PasswordRecoveryCodeServlet.PATH);
    }

    private void showError(HttpServletRequest request, HttpServletResponse response, String email,
                           String message) throws ServletException, IOException {
        request.setAttribute("errors", Map.of("email", message));
        request.setAttribute("email", email);
        RecoveryFlow.show(request, response, RecoveryFlow.REQUEST_VIEW);
    }
}

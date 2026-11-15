package mx.edu.utez.judicial.oficialia.service;

import mx.edu.utez.judicial.oficialia.client.MailClient;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;
import mx.edu.utez.judicial.oficialia.util.EmailContentBuilder;

/**
 * Sends the e-mails of the system. Chooses the subject and the text of each message; the transport is the
 * {@link MailClient}.
 */
public class EmailService {

    private final MailClient mailClient;

    public EmailService() {
        this(MailClient.create());
    }

    public EmailService(MailClient mailClient) {
        this.mailClient = mailClient;
    }

    /**
     * Sends the verification code to recover a password (RF 1.3).
     *
     * @throws ServiceUnavailableException when the message cannot be sent
     */
    public void sendPasswordRecoveryCode(String toEmail, String fullName, String code, int validMinutes) {
        mailClient.send(toEmail, "Código de verificación - SCD-2026",
            EmailContentBuilder.buildPasswordRecoveryEmail(fullName, code, validMinutes));
    }
}

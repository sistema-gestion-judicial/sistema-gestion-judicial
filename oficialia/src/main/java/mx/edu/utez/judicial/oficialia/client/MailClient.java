package mx.edu.utez.judicial.oficialia.client;

import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;

/**
 * Sends e-mail through the mail server. Only transports the message; the text is built elsewhere.
 */
@FunctionalInterface
public interface MailClient {

    /**
     * Sends a message whose body is HTML.
     *
     * @throws ServiceUnavailableException when the message cannot be sent
     */
    void send(String toEmail, String subject, String htmlBody);

    /**
     * The SMTP client when a mail server is configured. Without one, the client that only writes the message in
     * the log when that was asked for (development), and otherwise one that refuses to send anything.
     */
    static MailClient create() {
        MailSettings settings = MailSettings.load();
        if (settings.isConfigured()) {
            return new SmtpMailClient(settings);
        }
        if (settings.isLogOnly()) {
            return new SampleMailClient();
        }
        return (toEmail, subject, htmlBody) -> {
            throw new ServiceUnavailableException("No mail server is configured: set OFICIALIA_MAIL_HOST");
        };
    }
}

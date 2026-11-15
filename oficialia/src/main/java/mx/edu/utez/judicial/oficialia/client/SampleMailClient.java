package mx.edu.utez.judicial.oficialia.client;

import java.util.logging.Logger;

/**
 * DEVELOPMENT ONLY. Writes the message in the log instead of sending it, so a developer can read the password
 * recovery code there. It is used only when OFICIALIA_MAIL_LOG_ONLY=true and no mail server is configured: the
 * log shows the whole message, codes included, so it must never be on in production.
 */
public class SampleMailClient implements MailClient {

    private static final Logger LOG = Logger.getLogger(SampleMailClient.class.getName());

    @Override
    public void send(String toEmail, String subject, String htmlBody) {
        String text = htmlBody.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        LOG.warning("Mail log-only mode, this message was NOT sent. To: " + toEmail + " | Subject: " + subject
            + " | Text: " + text);
    }
}

package mx.edu.utez.judicial.oficialia.client;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import mx.edu.utez.judicial.oficialia.exception.ServiceUnavailableException;

/**
 * Sends the messages with Jakarta Mail through the SMTP server of {@link MailSettings}.
 */
public class SmtpMailClient implements MailClient {

    private static final Logger LOG = Logger.getLogger(SmtpMailClient.class.getName());

    private final MailSettings settings;
    private final Session session;

    public SmtpMailClient(MailSettings settings) {
        this.settings = settings;
        this.session = buildSession(settings);
    }

    @Override
    public void send(String toEmail, String subject, String htmlBody) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(settings.getFrom(), settings.getFromName(), "UTF-8"));
            // Strict parsing: exactly one address, so a typed value cannot add more recipients
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, true));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlBody, "text/html; charset=UTF-8");
            Transport.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            LOG.log(Level.WARNING, "The mail server did not accept the message", e);
            throw new ServiceUnavailableException("The mail server did not accept the message", e);
        } catch (RuntimeException e) {
            // Some providers block until the timeout and the failure arrives wrapped in an unchecked exception
            if (hasTimeoutCause(e)) {
                LOG.log(Level.WARNING, "The mail server did not answer in time", e);
                throw new ServiceUnavailableException("The mail server did not answer in time", e);
            }
            throw e;
        }
    }

    private static Session buildSession(MailSettings settings) {
        Properties props = new Properties();
        props.put("mail.smtp.host", settings.getHost());
        props.put("mail.smtp.port", settings.getPort());
        props.put("mail.smtp.auth", String.valueOf(settings.isAuth()));
        props.put("mail.smtp.starttls.enable", String.valueOf(settings.isStartTls()));
        props.put("mail.smtp.connectiontimeout", String.valueOf(settings.getConnectionTimeoutMillis()));
        props.put("mail.smtp.timeout", String.valueOf(settings.getReadTimeoutMillis()));
        props.put("mail.smtp.writetimeout", String.valueOf(settings.getWriteTimeoutMillis()));
        if (!settings.isAuth()) {
            return Session.getInstance(props);
        }
        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(settings.getUsername(), settings.getPassword());
            }
        });
    }

    private static boolean hasTimeoutCause(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }
}

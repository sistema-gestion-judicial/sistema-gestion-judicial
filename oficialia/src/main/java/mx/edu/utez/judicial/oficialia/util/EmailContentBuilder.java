package mx.edu.utez.judicial.oficialia.util;

/**
 * Builds the HTML of the e-mails. Only assembles text: it neither sends anything nor holds business rules.
 * Colors are written inline (taken from morelos-theme.css) because e-mail clients ignore style sheets.
 */
public final class EmailContentBuilder {

    private static final String SYSTEM = "SCD-2026";
    private static final String AREA = "Oficialía de Partes";

    private EmailContentBuilder() {
    }

    /**
     * Message with the verification code to recover a password (RF 1.3).
     *
     * @param fullName     name of the user, may be empty
     * @param code         verification code
     * @param validMinutes minutes before the code expires
     */
    public static String buildPasswordRecoveryEmail(String fullName, String code, int validMinutes) {
        return wrap("Recuperar contraseña",
            "<p>" + greeting(fullName) + "</p>"
                + "<p>Recibimos una solicitud para recuperar la contraseña de su cuenta del Sistema de Control "
                + "de Demandas.</p>"
                + "<p>Su código de verificación es:</p>"
                + "<p style=\"text-align:center;margin:24px 0;\"><span style=\"display:inline-block;"
                + "background:#e4e9e0;color:#2e3b2b;font-family:'Courier New',monospace;font-size:32px;"
                + "font-weight:bold;letter-spacing:8px;padding:12px 24px;border-radius:8px;\">"
                + escapeHtml(code) + "</span></p>"
                + "<p>El código vence en " + validMinutes + " minutos y solo puede usarse una vez. "
                + "No lo comparta con nadie.</p>"
                + "<p>Si usted no solicitó este cambio, ignore este mensaje: su contraseña actual sigue "
                + "siendo válida.</p>");
    }

    private static String greeting(String fullName) {
        String name = fullName == null || fullName.isBlank() ? "usuario(a)" : escapeHtml(fullName.trim());
        return "Estimado(a) " + name + ":";
    }

    /** Escapes dynamic text so it can be placed safely inside the HTML. */
    static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }

    private static String wrap(String title, String body) {
        return "<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
            + "<title>" + title + "</title></head>"
            + "<body style=\"font-family:Roboto,Arial,Helvetica,sans-serif;color:#1f2326;"
            + "background:#ebeade;padding:24px;\">"
            + "<div style=\"max-width:520px;margin:0 auto;background:#ffffff;border-radius:12px;"
            + "padding:32px;\">"
            + "<h2 style=\"font-family:Merriweather,Georgia,serif;color:#2e3b2b;margin:0;\">" + SYSTEM + "</h2>"
            + "<p style=\"color:#55585a;margin:4px 0 0;\">" + AREA + "</p>"
            + "<hr style=\"border:none;border-top:3px solid #c69c6d;margin:16px 0 24px;\">"
            + "<h3 style=\"margin-top:0;\">" + title + "</h3>"
            + body
            + "<hr style=\"border:none;border-top:1px solid #d9d6d2;margin:24px 0;\">"
            + "<p style=\"font-size:12px;color:#55585a;\">Este es un mensaje automático del Sistema de "
            + "Control de Demandas. Por favor no responda a este correo.</p>"
            + "</div></body></html>";
    }
}

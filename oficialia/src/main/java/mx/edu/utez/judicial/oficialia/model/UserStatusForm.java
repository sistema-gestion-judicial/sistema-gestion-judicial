package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The data sent to activate or deactivate a user of the court (RF 1.7): the new status, which is required,
 * and the reason for the change, which is optional and is kept in the audit trail.
 */
public class UserStatusForm {

    /** The size of audit_log.reason (V007). */
    public static final int MAX_REASON_LENGTH = 500;

    static final String ACTIVE = "active";
    static final String INACTIVE = "inactive";

    private String status = "";
    private String reason = "";

    public String getStatus() {
        return status;
    }

    /** "active" or "inactive"; anything else is reported by {@link #validate()}. */
    public void setStatus(String status) {
        this.status = status == null ? "" : status.trim();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? "" : reason.trim();
    }

    /** True to activate the account, false to deactivate it. Only meaningful when the form is valid. */
    public boolean isActivating() {
        return ACTIVE.equals(status);
    }

    /** The reason as stored: null when none was typed. */
    public String getReasonOrNull() {
        return reason.isEmpty() ? null : reason;
    }

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (!ACTIVE.equals(status) && !INACTIVE.equals(status)) {
            errors.put("status", "Elija el nuevo estado de la cuenta.");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            errors.put("reason", "El motivo no puede pasar de " + MAX_REASON_LENGTH + " caracteres.");
        }
        return errors;
    }
}

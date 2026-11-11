package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * What the clerk chose when ruling a demand request (RF 3.5).
 */
public class RulingForm {

    public static final int MIN_REASON_LENGTH = 15;
    public static final int MAX_REASON_LENGTH = 500;

    private RequestStatus decision;
    private String reason = "";

    public RequestStatus getDecision() {
        return decision;
    }

    /** Only admissible or inadmissible are accepted; anything else leaves the decision empty. */
    public void setDecision(String value) {
        if ("ADMISSIBLE".equals(value)) {
            decision = RequestStatus.ADMISSIBLE;
        } else if ("INADMISSIBLE".equals(value)) {
            decision = RequestStatus.INADMISSIBLE;
        } else {
            decision = null;
        }
    }

    public String getDecisionCode() {
        return decision == null ? "" : decision.getCode();
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason == null ? "" : reason.trim();
    }

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (decision == null) {
            errors.put("decision", "Elija si la solicitud es procedente o no procedente.");
        } else if (decision == RequestStatus.INADMISSIBLE) {
            if (reason.length() < MIN_REASON_LENGTH) {
                errors.put("reason", "Escriba el motivo del descarte, con al menos " + MIN_REASON_LENGTH
                        + " caracteres.");
            } else if (reason.length() > MAX_REASON_LENGTH) {
                errors.put("reason", "El motivo no puede pasar de " + MAX_REASON_LENGTH + " caracteres.");
            }
        }
        return errors;
    }
}

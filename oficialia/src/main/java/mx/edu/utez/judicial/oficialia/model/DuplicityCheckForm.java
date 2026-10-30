package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.util.PersonIdValidator;

/**
 * Step 1 of the registration: the minimum data needed to check duplicity across courts.
 */
public class DuplicityCheckForm {

    private String matter = "";
    private String plaintiffType = "INDIVIDUAL";
    private String plaintiffName = "";
    private String plaintiffId = "";
    private boolean plaintiffAnonymous;
    private String defendantType = "INDIVIDUAL";
    private String defendantName = "";
    private String defendantId = "";
    private boolean defendantUnknown;

    /**
     * Validates the form and returns the error message of each invalid field, keyed by field name.
     */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (matter.isBlank()) {
            errors.put("matter", "Seleccione la materia de la demanda.");
        }
        boolean hasPlaintiff = !plaintiffAnonymous;
        boolean hasDefendant = !defendantUnknown;
        if (!hasPlaintiff && !hasDefendant) {
            errors.put("parties", "Capture al menos una de las partes con su nombre y su RFC o CURP.");
            return errors;
        }
        if (hasPlaintiff) {
            validateParty("plaintiff", plaintiffName, plaintiffId, errors);
        }
        if (hasDefendant) {
            validateParty("defendant", defendantName, defendantId, errors);
        }
        return errors;
    }

    private static void validateParty(String prefix, String name, String personId, Map<String, String> errors) {
        if (name.isBlank()) {
            errors.put(prefix + "Name", "Capture el nombre o la razón social.");
        }
        if (personId.isBlank()) {
            errors.put(prefix + "Id", "Capture el RFC o la CURP.");
        } else if (!PersonIdValidator.isValid(personId)) {
            errors.put(prefix + "Id", "El RFC o la CURP no tienen un formato válido.");
        }
    }

    public String getMatter() {
        return matter;
    }

    public void setMatter(String matter) {
        this.matter = clean(matter);
    }

    public String getPlaintiffType() {
        return plaintiffType;
    }

    public void setPlaintiffType(String plaintiffType) {
        this.plaintiffType = clean(plaintiffType).isEmpty() ? "INDIVIDUAL" : clean(plaintiffType);
    }

    public String getPlaintiffName() {
        return plaintiffName;
    }

    public void setPlaintiffName(String plaintiffName) {
        this.plaintiffName = clean(plaintiffName);
    }

    public String getPlaintiffId() {
        return plaintiffId;
    }

    public void setPlaintiffId(String plaintiffId) {
        this.plaintiffId = PersonIdValidator.normalize(plaintiffId);
    }

    public boolean isPlaintiffAnonymous() {
        return plaintiffAnonymous;
    }

    public void setPlaintiffAnonymous(boolean plaintiffAnonymous) {
        this.plaintiffAnonymous = plaintiffAnonymous;
    }

    public String getDefendantType() {
        return defendantType;
    }

    public void setDefendantType(String defendantType) {
        this.defendantType = clean(defendantType).isEmpty() ? "INDIVIDUAL" : clean(defendantType);
    }

    public String getDefendantName() {
        return defendantName;
    }

    public void setDefendantName(String defendantName) {
        this.defendantName = clean(defendantName);
    }

    public String getDefendantId() {
        return defendantId;
    }

    public void setDefendantId(String defendantId) {
        this.defendantId = PersonIdValidator.normalize(defendantId);
    }

    public boolean isDefendantUnknown() {
        return defendantUnknown;
    }

    public void setDefendantUnknown(boolean defendantUnknown) {
        this.defendantUnknown = defendantUnknown;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

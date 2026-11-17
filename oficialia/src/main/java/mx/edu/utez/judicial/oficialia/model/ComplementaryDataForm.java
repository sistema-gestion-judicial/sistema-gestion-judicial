package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Step 2 of the registration (DFR 3.1): the legal representative, how the demand was presented and the
 * observations.
 */
public class ComplementaryDataForm {

    public static final int MAX_REPRESENTATIVE_LENGTH = 150;
    public static final int MAX_OBSERVATIONS_LENGTH = 500;

    private String legalRepresentative = "";
    private String presentationType = "";
    private String observations = "";

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate(boolean plaintiffIdentified, boolean defendantIdentified) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (legalRepresentative.isEmpty()) {
            errors.put("legalRepresentative", "Capture el nombre del representante legal o el nombre legal del grupo.");
        } else if (legalRepresentative.length() > MAX_REPRESENTATIVE_LENGTH) {
            errors.put("legalRepresentative", tooLong(MAX_REPRESENTATIVE_LENGTH));
        }
        if (getPresentationTypeValue() == null) {
            errors.put("presentationType", "Seleccione si la demanda es individual o colectiva.");
        }
        if (observations.length() > MAX_OBSERVATIONS_LENGTH) {
            errors.put("observations", tooLong(MAX_OBSERVATIONS_LENGTH));
        }
        return errors;
    }

    private static String tooLong(int maxLength) {
        return "No puede pasar de " + maxLength + " caracteres.";
    }

    public String getLegalRepresentative() {
        return legalRepresentative;
    }

    public void setLegalRepresentative(String legalRepresentative) {
        this.legalRepresentative = clean(legalRepresentative);
    }

    public String getPresentationType() {
        return presentationType;
    }

    public void setPresentationType(String presentationType) {
        this.presentationType = clean(presentationType);
    }

    /** The presentation type chosen, or null when none or an unknown one was sent. */
    public PresentationType getPresentationTypeValue() {
        for (PresentationType type : PresentationType.values()) {
            if (type.name().equals(presentationType)) {
                return type;
            }
        }
        return null;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations == null ? "" : observations.trim();
    }

    /** The observations to store, or null when none were written. */
    public String observationsOrNull() {
        return observations.isEmpty() ? null : observations;
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}

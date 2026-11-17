package mx.edu.utez.judicial.oficialia.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Step 2 of the registration (DFR 3.1): the address and contact of the parties, the legal representative,
 * how the demand was presented, the observations and the annexes. The plaintiff's address and contact are
 * required unless the plaintiff is anonymous; the defendant's are captured only when they are known.
 */
public class ComplementaryDataForm {

    public static final int MAX_STREET_LENGTH = 150;
    public static final int MAX_NEIGHBORHOOD_LENGTH = 100;
    public static final int MAX_MUNICIPALITY_LENGTH = 80;
    public static final int MAX_EMAIL_LENGTH = 150;
    public static final int MAX_REPRESENTATIVE_LENGTH = 150;
    public static final int MAX_OBSERVATIONS_LENGTH = 500;
    public static final int MAX_ANNEX_MB = 10;
    public static final int MAX_TOTAL_ANNEX_MB = 40;
    private static final int PHONE_DIGITS = 10;

    private final ContactFields plaintiff = new ContactFields();
    private final ContactFields defendant = new ContactFields();
    private String legalRepresentative = "";
    private String presentationType = "";
    private String observations = "";
    private final List<Annex> annexes = new ArrayList<>();

    /**
     * Field name to message, in Spanish; empty when the form is valid. The fields of a party that was not
     * identified in step 1 are not checked.
     */
    public Map<String, String> validate(boolean plaintiffIdentified, boolean defendantIdentified) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (plaintiffIdentified) {
            plaintiff.validate("plaintiff", true, errors);
        }
        if (defendantIdentified) {
            defendant.validate("defendant", false, errors);
        }
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
        validateAnnexes(errors);
        return errors;
    }

    private void validateAnnexes(Map<String, String> errors) {
        if (annexes.isEmpty()) {
            errors.put("annexes", "Adjunte al menos un anexo.");
            return;
        }
        int totalKb = 0;
        for (Annex annex : annexes) {
            if (annex.getSizeKb() > MAX_ANNEX_MB * 1024) {
                errors.put("annexes", "El anexo «" + annex.getFileName() + "» pasa de " + MAX_ANNEX_MB + " MB.");
                return;
            }
            totalKb += annex.getSizeKb();
        }
        if (totalKb > MAX_TOTAL_ANNEX_MB * 1024) {
            errors.put("annexes", "Los anexos juntos pasan de " + MAX_TOTAL_ANNEX_MB + " MB.");
        }
    }

    private static String tooLong(int maxLength) {
        return "No puede pasar de " + maxLength + " caracteres.";
    }

    /** The address of the plaintiff, or null when none was captured. */
    public PartyAddress plaintiffAddress() {
        return plaintiff.address();
    }

    /** The address of the defendant, or null when it is not known. */
    public PartyAddress defendantAddress() {
        return defendant.address();
    }

    public ContactFields getPlaintiff() {
        return plaintiff;
    }

    public ContactFields getDefendant() {
        return defendant;
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

    public List<Annex> getAnnexes() {
        return List.copyOf(annexes);
    }

    public void addAnnex(Annex annex) {
        annexes.add(annex);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    /**
     * Address, phone and email of one party, as typed. A class instead of a record so the JSP pages can read
     * the values back after an error.
     */
    public static class ContactFields {

        private String street = "";
        private String neighborhood = "";
        private String postalCode = "";
        private String municipality = "";
        private String phone = "";
        private String email = "";

        void validate(String prefix, boolean required, Map<String, String> errors) {
            boolean anyAddress = !street.isEmpty() || !neighborhood.isEmpty() || !postalCode.isEmpty()
                || !municipality.isEmpty();
            if (required || anyAddress) {
                text(errors, prefix + "Street", street, MAX_STREET_LENGTH, "la calle y el número");
                text(errors, prefix + "Neighborhood", neighborhood, MAX_NEIGHBORHOOD_LENGTH, "la colonia");
                if (postalCode.isEmpty()) {
                    errors.put(prefix + "PostalCode", "Escriba el código postal.");
                } else if (!postalCode.matches("[0-9]{5}")) {
                    errors.put(prefix + "PostalCode", "El código postal debe tener 5 dígitos.");
                }
                text(errors, prefix + "Municipality", municipality, MAX_MUNICIPALITY_LENGTH, "el municipio");
            }
            if (phone.isEmpty()) {
                if (required) {
                    errors.put(prefix + "Phone", "Escriba el teléfono.");
                }
            } else if (!phone.matches("[0-9 ()+-]+") || getPhoneDigits().length() != PHONE_DIGITS) {
                errors.put(prefix + "Phone", "Escriba un teléfono de " + PHONE_DIGITS + " dígitos.");
            }
            if (email.isEmpty()) {
                if (required) {
                    errors.put(prefix + "Email", "Escriba el correo electrónico.");
                }
            } else if (email.length() > MAX_EMAIL_LENGTH || !EmailValidator.isValid(email)) {
                errors.put(prefix + "Email", "Escriba un correo con formato válido.");
            }
        }

        private static void text(Map<String, String> errors, String field, String value, int maxLength,
                                 String label) {
            if (value.isEmpty()) {
                errors.put(field, "Escriba " + label + ".");
            } else if (value.length() > maxLength) {
                errors.put(field, tooLong(maxLength));
            }
        }

        PartyAddress address() {
            return street.isEmpty() ? null : new PartyAddress(street, neighborhood, postalCode, municipality);
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = clean(street);
        }

        public String getNeighborhood() {
            return neighborhood;
        }

        public void setNeighborhood(String neighborhood) {
            this.neighborhood = clean(neighborhood);
        }

        public String getPostalCode() {
            return postalCode;
        }

        public void setPostalCode(String postalCode) {
            this.postalCode = clean(postalCode);
        }

        public String getMunicipality() {
            return municipality;
        }

        public void setMunicipality(String municipality) {
            this.municipality = clean(municipality);
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = clean(phone);
        }

        /** The phone as stored: only its digits, or null when none was typed. */
        public String getPhoneDigits() {
            String digits = phone.replaceAll("\\D", "");
            return digits.isEmpty() ? null : digits;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = EmailValidator.normalize(email);
        }

        /** The email to store, or null when none was typed. */
        public String emailOrNull() {
            return email.isEmpty() ? null : email;
        }
    }
}

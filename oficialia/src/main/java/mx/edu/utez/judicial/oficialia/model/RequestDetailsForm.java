package mx.edu.utez.judicial.oficialia.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * Step 2 of registering a demand request (DFR 3.1): how to reach each identified party, the legal
 * representative, how the lawsuit was filed and its annexes. Only the parties identified in step 1 are asked
 * for: an anonymous plaintiff or an unknown defendant has no contact data.
 */
public class RequestDetailsForm {

    public static final int MAX_ADDRESS_LENGTH = 255;
    public static final int MAX_EMAIL_LENGTH = 150;
    public static final int MAX_REPRESENTATIVE_LENGTH = 150;
    public static final int MAX_ANNEXES = 10;
    public static final long MAX_ANNEX_BYTES = 10L * 1024 * 1024;
    private static final int PHONE_DIGITS = 10;

    private String plaintiffAddress = "";
    private String plaintiffPhone = "";
    private String plaintiffEmail = "";
    private String defendantAddress = "";
    private String defendantPhone = "";
    private String defendantEmail = "";
    private String legalRepresentative = "";
    private String filingType = "";
    private final List<AnnexUpload> annexes = new ArrayList<>();

    /** A file chosen as annex: only its name and size are kept, as the annexes table stores them. */
    public record AnnexUpload(String fileName, long sizeBytes, String contentType) {
    }

    /**
     * Field name to message, in Spanish; empty when it can be saved. The plaintiff needs an address and a phone;
     * the defendant needs an address so it can be summoned. E-mails are optional.
     */
    public Map<String, String> validate(boolean plaintiffIdentified, boolean defendantIdentified) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (plaintiffIdentified) {
            requireAddress(errors, "plaintiffAddress", plaintiffAddress);
            if (plaintiffPhone.isEmpty()) {
                errors.put("plaintiffPhone", "Escriba el teléfono del demandante.");
            } else {
                checkPhone(errors, "plaintiffPhone", plaintiffPhone);
            }
            checkEmail(errors, "plaintiffEmail", plaintiffEmail);
        }
        if (defendantIdentified) {
            requireAddress(errors, "defendantAddress", defendantAddress);
            if (!defendantPhone.isEmpty()) {
                checkPhone(errors, "defendantPhone", defendantPhone);
            }
            checkEmail(errors, "defendantEmail", defendantEmail);
        }
        if (legalRepresentative.length() > MAX_REPRESENTATIVE_LENGTH) {
            errors.put("legalRepresentative", "No puede pasar de " + MAX_REPRESENTATIVE_LENGTH + " caracteres.");
        }
        if (getFilingTypeValue() == null) {
            errors.put("filingType", "Seleccione el tipo de presentación.");
        }
        if (annexes.isEmpty()) {
            errors.put("annexes", "Adjunte al menos un anexo en PDF, como el escrito inicial de demanda.");
        } else if (annexes.size() > MAX_ANNEXES) {
            errors.put("annexes", "Adjunte como máximo " + MAX_ANNEXES + " archivos.");
        } else if (annexes.stream().anyMatch(annex -> !isPdf(annex))) {
            errors.put("annexes", "Los anexos deben ser archivos PDF.");
        } else if (annexes.stream().anyMatch(annex -> annex.sizeBytes() > MAX_ANNEX_BYTES)) {
            errors.put("annexes", "Cada anexo puede pesar como máximo 10 MB.");
        }
        return errors;
    }

    /** The annexes as stored: file name and size in KB, at least 1. */
    public List<Annex> toAnnexes() {
        return annexes.stream()
            .map(annex -> new Annex(annex.fileName(), (int) Math.max(1, Math.ceil(annex.sizeBytes() / 1024.0))))
            .toList();
    }

    private static boolean isPdf(AnnexUpload annex) {
        return annex.fileName().toLowerCase(Locale.ROOT).endsWith(".pdf")
            && (annex.contentType() == null || annex.contentType().equals("application/pdf")
                || annex.contentType().equals("application/octet-stream"));
    }

    private static void requireAddress(Map<String, String> errors, String field, String value) {
        if (value.isEmpty()) {
            errors.put(field, "Escriba el domicilio.");
        } else if (value.length() > MAX_ADDRESS_LENGTH) {
            errors.put(field, "El domicilio no puede pasar de " + MAX_ADDRESS_LENGTH + " caracteres.");
        }
    }

    private static void checkPhone(Map<String, String> errors, String field, String value) {
        if (!value.matches("[0-9 ()+-]+") || value.replaceAll("\\D", "").length() != PHONE_DIGITS) {
            errors.put(field, "Escriba un teléfono de " + PHONE_DIGITS + " dígitos.");
        }
    }

    private static void checkEmail(Map<String, String> errors, String field, String value) {
        if (!value.isEmpty() && (!EmailValidator.isValid(value) || value.length() > MAX_EMAIL_LENGTH)) {
            errors.put(field, "Escriba un correo con formato válido.");
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }

    private static String orNull(String value) {
        return value.isEmpty() ? null : value;
    }

    public String getPlaintiffAddress() {
        return plaintiffAddress;
    }

    public void setPlaintiffAddress(String plaintiffAddress) {
        this.plaintiffAddress = clean(plaintiffAddress);
    }

    public String getPlaintiffPhone() {
        return plaintiffPhone;
    }

    public void setPlaintiffPhone(String plaintiffPhone) {
        this.plaintiffPhone = clean(plaintiffPhone);
    }

    public String getPlaintiffEmail() {
        return plaintiffEmail;
    }

    public void setPlaintiffEmail(String plaintiffEmail) {
        this.plaintiffEmail = EmailValidator.normalize(plaintiffEmail);
    }

    public String getDefendantAddress() {
        return defendantAddress;
    }

    public void setDefendantAddress(String defendantAddress) {
        this.defendantAddress = clean(defendantAddress);
    }

    public String getDefendantPhone() {
        return defendantPhone;
    }

    public void setDefendantPhone(String defendantPhone) {
        this.defendantPhone = clean(defendantPhone);
    }

    public String getDefendantEmail() {
        return defendantEmail;
    }

    public void setDefendantEmail(String defendantEmail) {
        this.defendantEmail = EmailValidator.normalize(defendantEmail);
    }

    public String getLegalRepresentative() {
        return legalRepresentative;
    }

    public String getLegalRepresentativeOrNull() {
        return orNull(legalRepresentative);
    }

    public void setLegalRepresentative(String legalRepresentative) {
        this.legalRepresentative = clean(legalRepresentative);
    }

    public String getFilingType() {
        return filingType;
    }

    /** Null when nothing or an unknown value was chosen. */
    public FilingType getFilingTypeValue() {
        for (FilingType type : FilingType.values()) {
            if (type.name().equals(filingType)) {
                return type;
            }
        }
        return null;
    }

    public void setFilingType(String filingType) {
        this.filingType = filingType == null ? "" : filingType.trim();
    }

    public List<AnnexUpload> getAnnexes() {
        return annexes;
    }

    public void addAnnex(AnnexUpload annex) {
        annexes.add(annex);
    }

    /** The plaintiff's contact data, stored with only the digits of the phone. */
    public Party plaintiff(Party identified) {
        return withContact(identified, plaintiffAddress, plaintiffPhone, plaintiffEmail);
    }

    public Party defendant(Party identified) {
        return withContact(identified, defendantAddress, defendantPhone, defendantEmail);
    }

    private static Party withContact(Party party, String address, String phone, String email) {
        if (!party.isIdentified()) {
            return party;
        }
        String digits = phone.replaceAll("\\D", "");
        return new Party(party.getRole(), party.getName(), party.getPersonType(), party.getPersonId(),
            digits.isEmpty() ? null : digits, orNull(email), orNull(address));
    }
}

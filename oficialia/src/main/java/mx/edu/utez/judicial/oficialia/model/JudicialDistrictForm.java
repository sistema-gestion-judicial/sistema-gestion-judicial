package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The data typed to register a judicial district (DFR 2.1). Whether the code or the name already belong
 * to another district is checked by the service, which can look at the catalog.
 */
public class JudicialDistrictForm {

    public static final int MIN_CODE_LENGTH = 2;
    public static final int MAX_CODE_LENGTH = 10;
    public static final int MAX_NAME_LENGTH = 120;
    public static final int MAX_SEAT_LENGTH = 80;

    private static final Pattern CODE = Pattern.compile("[A-Z0-9]{" + MIN_CODE_LENGTH + "," + MAX_CODE_LENGTH + "}");

    private String code = "";
    private String name = "";
    private String seatMunicipality = "";

    /** A form that starts with the current values of the district. */
    public static JudicialDistrictForm of(JudicialDistrict district) {
        JudicialDistrictForm form = new JudicialDistrictForm();
        form.setCode(district.getCode());
        form.setName(district.getName());
        form.setSeatMunicipality(district.getSeatMunicipality());
        return form;
    }

    /** The code is kept in upper case, like the ones already in the catalog (CVA, YAU, JOJ, CUA). */
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = clean(name);
    }

    public String getSeatMunicipality() {
        return seatMunicipality;
    }

    public void setSeatMunicipality(String seatMunicipality) {
        this.seatMunicipality = clean(seatMunicipality);
    }

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (code.isEmpty()) {
            errors.put("code", "Escriba la clave del distrito judicial.");
        } else if (!CODE.matcher(code).matches()) {
            errors.put("code", "La clave debe tener de " + MIN_CODE_LENGTH + " a " + MAX_CODE_LENGTH
                + " caracteres, solo letras y números, sin espacios.");
        }
        errors.putAll(validateWithoutCode());
        return errors;
    }

    /** Same as {@link #validate()} for an update, where the code is locked and is not checked (DFR 2.3). */
    public Map<String, String> validateWithoutCode() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (name.isEmpty()) {
            errors.put("name", "Escriba el nombre del distrito judicial.");
        } else if (name.length() > MAX_NAME_LENGTH) {
            errors.put("name", "El nombre no puede pasar de " + MAX_NAME_LENGTH + " caracteres.");
        }
        if (seatMunicipality.isEmpty()) {
            errors.put("seatMunicipality", "Escriba el municipio sede.");
        } else if (seatMunicipality.length() > MAX_SEAT_LENGTH) {
            errors.put("seatMunicipality", "El municipio no puede pasar de " + MAX_SEAT_LENGTH + " caracteres.");
        }
        return errors;
    }

    /** Trims and collapses inner spaces, so "Distrito  de Cuautla " and "Distrito de Cuautla" are the same name. */
    private static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}

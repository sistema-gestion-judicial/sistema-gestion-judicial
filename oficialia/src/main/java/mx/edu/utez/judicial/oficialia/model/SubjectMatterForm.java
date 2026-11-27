package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * The data typed to register or update a subject matter (DFR 2.5, 2.7). Whether the code or the name
 * already belong to another subject matter is checked by the service, which can look at the catalog.
 */
public class SubjectMatterForm {

    public static final int MIN_CODE_LENGTH = 2;
    public static final int MAX_CODE_LENGTH = 10;
    public static final int MAX_NAME_LENGTH = 60;
    public static final int MAX_DESCRIPTION_LENGTH = 255;

    private static final Pattern CODE = Pattern.compile("[A-Z0-9]{" + MIN_CODE_LENGTH + "," + MAX_CODE_LENGTH + "}");

    private String code = "";
    private String name = "";
    private String description = "";

    /** A form that starts with the current values of the subject matter. */
    public static SubjectMatterForm of(SubjectMatter matter) {
        SubjectMatterForm form = new SubjectMatterForm();
        form.setCode(matter.getCode());
        form.setName(matter.getName());
        form.setDescription(matter.getDescription());
        return form;
    }

    /** The code is kept in upper case, like the ones already in the catalog (CIV, FAM, MER, PEN). */
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

    /** Optional; empty when not typed. */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? "" : description.trim();
    }

    /** The description to store: null instead of an empty text. */
    public String getDescriptionOrNull() {
        return description.isEmpty() ? null : description;
    }

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (code.isEmpty()) {
            errors.put("code", "Escriba la clave de la materia.");
        } else if (!CODE.matcher(code).matches()) {
            errors.put("code", "La clave debe tener de " + MIN_CODE_LENGTH + " a " + MAX_CODE_LENGTH
                + " caracteres, solo letras y números, sin espacios.");
        }
        errors.putAll(validateWithoutCode());
        return errors;
    }

    /** Same as {@link #validate()} for an update, where the code is locked and is not checked (DFR 2.7). */
    public Map<String, String> validateWithoutCode() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (name.isEmpty()) {
            errors.put("name", "Escriba el nombre de la materia.");
        } else if (name.length() > MAX_NAME_LENGTH) {
            errors.put("name", "El nombre no puede pasar de " + MAX_NAME_LENGTH + " caracteres.");
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            errors.put("description", "La descripción no puede pasar de " + MAX_DESCRIPTION_LENGTH + " caracteres.");
        }
        return errors;
    }

    /** Trims and collapses inner spaces, so "Civil " and "Civil" are the same name. */
    private static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}

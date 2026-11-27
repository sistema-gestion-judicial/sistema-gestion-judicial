package mx.edu.utez.judicial.oficialia.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * The data typed to register a court and its administrator (DFR 2.9). Whether the code, the number, the name
 * or the e-mail are already taken, and whether the district and the subject matters exist and are active, is
 * checked by the service, which can look at the catalogs.
 */
public class CourtRegistrationForm {

    public static final int CODE_DIGITS = 2;
    public static final int MAX_NUMBER = 255;
    public static final int MAX_NAME_LENGTH = 120;
    public static final int MAX_STREET_LENGTH = 150;
    public static final int MAX_NEIGHBORHOOD_LENGTH = 100;
    public static final int MAX_MUNICIPALITY_LENGTH = 80;
    public static final int MAX_PERSON_NAME_LENGTH = 80;
    public static final int MAX_EMAIL_LENGTH = 150;
    private static final int POSTAL_CODE_DIGITS = 5;
    private static final int PHONE_DIGITS = 10;

    private String code = "";
    private String number = "";
    private String name = "";
    private String districtCode = "";
    private String street = "";
    private String neighborhood = "";
    private String postalCode = "";
    private String municipality = "";
    private String phone = "";
    private List<String> subjectMatterCodes = List.of();
    private String adminFirstName = "";
    private String adminLastName = "";
    private String adminEmail = "";
    // Kept as typed: spaces can be part of a password. It is never shown again, not even after an error.
    private String adminPassword = "";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = clean(code);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = clean(number);
    }

    /** The number as a value, or -1 when what was typed is not a whole number from 1 to {@link #MAX_NUMBER}. */
    public int getNumberValue() {
        if (!number.matches("[0-9]{1,3}")) {
            return -1;
        }
        int value = Integer.parseInt(number);
        return value >= 1 && value <= MAX_NUMBER ? value : -1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = clean(name);
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = clean(districtCode);
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

    public List<String> getSubjectMatterCodes() {
        return subjectMatterCodes;
    }

    /** The codes of the checked subject matters; blanks and repeated codes are dropped. */
    public void setSubjectMatterCodes(String[] codes) {
        this.subjectMatterCodes = codes == null ? List.of() : Arrays.stream(codes)
            .map(CourtRegistrationForm::clean)
            .filter(value -> !value.isEmpty())
            .distinct()
            .toList();
    }

    public String getAdminFirstName() {
        return adminFirstName;
    }

    public void setAdminFirstName(String adminFirstName) {
        this.adminFirstName = clean(adminFirstName);
    }

    public String getAdminLastName() {
        return adminLastName;
    }

    public void setAdminLastName(String adminLastName) {
        this.adminLastName = clean(adminLastName);
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = EmailValidator.normalize(adminEmail);
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword == null ? "" : adminPassword;
    }

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (code.isEmpty()) {
            errors.put("code", "Escriba la clave del juzgado.");
        } else if (!code.matches("[0-9]{" + CODE_DIGITS + "}")) {
            errors.put("code", "La clave debe tener " + CODE_DIGITS + " dígitos, por ejemplo 08.");
        }
        if (number.isEmpty()) {
            errors.put("number", "Escriba el número del juzgado.");
        } else if (getNumberValue() < 0) {
            errors.put("number", "El número debe ser un entero del 1 al " + MAX_NUMBER + ".");
        }
        requireText(errors, "name", name, "el nombre del juzgado", MAX_NAME_LENGTH);
        if (districtCode.isEmpty()) {
            errors.put("districtCode", "Elija el distrito judicial.");
        }
        requireText(errors, "street", street, "la calle y el número", MAX_STREET_LENGTH);
        requireText(errors, "neighborhood", neighborhood, "la colonia", MAX_NEIGHBORHOOD_LENGTH);
        if (postalCode.isEmpty()) {
            errors.put("postalCode", "Escriba el código postal.");
        } else if (!postalCode.matches("[0-9]{" + POSTAL_CODE_DIGITS + "}")) {
            errors.put("postalCode", "El código postal debe tener " + POSTAL_CODE_DIGITS + " dígitos.");
        }
        requireText(errors, "municipality", municipality, "el municipio", MAX_MUNICIPALITY_LENGTH);
        // Only symbols, such as "---", leave no digits at all
        String digits = getPhoneDigits();
        if (!phone.isEmpty() && (!phone.matches("[0-9 ()+-]+") || digits == null || digits.length() != PHONE_DIGITS)) {
            errors.put("phone", "Escriba un teléfono de " + PHONE_DIGITS + " dígitos.");
        }
        if (subjectMatterCodes.isEmpty()) {
            errors.put("subjectMatterCodes", "Elija al menos una materia.");
        }
        requireText(errors, "adminFirstName", adminFirstName, "el nombre del administrador",
            MAX_PERSON_NAME_LENGTH);
        requireText(errors, "adminLastName", adminLastName, "los apellidos del administrador",
            MAX_PERSON_NAME_LENGTH);
        if (adminEmail.isEmpty()) {
            errors.put("adminEmail", "Escriba el correo electrónico del administrador.");
        } else if (!EmailValidator.isValid(adminEmail) || adminEmail.length() > MAX_EMAIL_LENGTH) {
            errors.put("adminEmail", "Escriba un correo con formato válido.");
        }
        if (adminPassword.isEmpty()) {
            errors.put("adminPassword", "Escriba la contraseña temporal del administrador.");
        } else if (!meetsPasswordFormat(adminPassword)) {
            errors.put("adminPassword", "La contraseña debe tener al menos " + AuthSettings.PASSWORD_MIN_LENGTH
                + " caracteres, con letras y números.");
        }
        return errors;
    }

    private static void requireText(Map<String, String> errors, String field, String value, String label,
                                    int maxLength) {
        if (value.isEmpty()) {
            errors.put(field, "Escriba " + label + ".");
        } else if (value.length() > maxLength) {
            errors.put(field, "No puede pasar de " + maxLength + " caracteres.");
        }
    }

    /** The same format the user must follow when they replace the temporary password. */
    private static boolean meetsPasswordFormat(String password) {
        return password.length() >= AuthSettings.PASSWORD_MIN_LENGTH
            && password.chars().anyMatch(Character::isLetter)
            && password.chars().anyMatch(Character::isDigit);
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

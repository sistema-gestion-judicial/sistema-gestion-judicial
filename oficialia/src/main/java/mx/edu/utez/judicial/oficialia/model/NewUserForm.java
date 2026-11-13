package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.util.EmailValidator;

/**
 * The data typed to register a user (RF 1.4). Whether the e-mail is already registered is checked by the
 * service, which can look at the stored accounts.
 */
public class NewUserForm {

    public static final int MAX_NAME_LENGTH = 80;
    public static final int MAX_EMAIL_LENGTH = 150;
    private static final int PHONE_DIGITS = 10;

    private String firstName = "";
    private String lastName = "";
    private String email = "";
    private String phone = "";

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = clean(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = clean(lastName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = EmailValidator.normalize(email);
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

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        validateName(errors, "firstName", firstName, "el nombre");
        validateName(errors, "lastName", lastName, "los apellidos");
        if (email.isEmpty()) {
            errors.put("email", "Escriba el correo electrónico.");
        } else if (!EmailValidator.isValid(email) || email.length() > MAX_EMAIL_LENGTH) {
            errors.put("email", "Escriba un correo con formato válido.");
        }
        if (!phone.isEmpty() && (!phone.matches("[0-9 ()+-]+") || getPhoneDigits().length() != PHONE_DIGITS)) {
            errors.put("phone", "Escriba un teléfono de " + PHONE_DIGITS + " dígitos.");
        }
        return errors;
    }

    private static void validateName(Map<String, String> errors, String field, String value, String label) {
        if (value.isEmpty()) {
            errors.put(field, "Escriba " + label + ".");
        } else if (value.length() > MAX_NAME_LENGTH) {
            errors.put(field, "No puede pasar de " + MAX_NAME_LENGTH + " caracteres.");
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

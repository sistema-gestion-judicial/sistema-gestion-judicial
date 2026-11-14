package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The password a user chooses to replace the temporary one (RF 1.4). Passwords are never trimmed.
 */
public class PasswordChangeForm {

    public static final int MIN_LENGTH = 8;

    private String newPassword = "";
    private String confirmation = "";

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword == null ? "" : newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation == null ? "" : confirmation;
    }

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        if (newPassword.isEmpty()) {
            errors.put("newPassword", "Escriba la nueva contraseña.");
        } else if (!meetsFormat(newPassword)) {
            errors.put("newPassword", "La contraseña debe tener al menos " + MIN_LENGTH
                + " caracteres, con letras y números.");
        }
        if (confirmation.isEmpty()) {
            errors.put("confirmation", "Confirme la nueva contraseña.");
        } else if (!confirmation.equals(newPassword)) {
            errors.put("confirmation", "La confirmación no coincide con la nueva contraseña.");
        }
        return errors;
    }

    private static boolean meetsFormat(String password) {
        return password.length() >= MIN_LENGTH
            && password.chars().anyMatch(Character::isLetter)
            && password.chars().anyMatch(Character::isDigit);
    }
}

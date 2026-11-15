package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.config.AuthSettings;

/**
 * The new password typed when recovering an account (RF 1.3). Passwords are never trimmed.
 */
public class PasswordResetForm {

    private String newPassword = "";
    private String confirmation = "";

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword == null ? "" : newPassword;
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
            errors.put("newPassword", "La contraseña debe tener al menos " + AuthSettings.PASSWORD_MIN_LENGTH
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
        return password.length() >= AuthSettings.PASSWORD_MIN_LENGTH
            && password.chars().anyMatch(Character::isLetter)
            && password.chars().anyMatch(Character::isDigit);
    }
}

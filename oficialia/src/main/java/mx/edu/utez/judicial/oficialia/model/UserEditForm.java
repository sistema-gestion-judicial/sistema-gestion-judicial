package mx.edu.utez.judicial.oficialia.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The data typed to correct a user of the court (DFR 1.6): the same fields as a registration plus the
 * profile. There is no password field: the password is only changed by its owner.
 */
public class UserEditForm extends NewUserForm {

    /** The profiles an Administrador de Juzgado can give inside their court. */
    public static final List<UserRole> COURT_ROLES = List.of(UserRole.CLERK, UserRole.COURT_ADMIN);

    private UserRole role;

    /** A form that starts with the current values of the user. */
    public static UserEditForm of(UserProfile profile) {
        UserEditForm form = new UserEditForm();
        form.setFirstName(profile.getFirstName());
        form.setLastName(profile.getLastName());
        form.setEmail(profile.getEmail());
        form.setPhone(profile.getPhone());
        form.role = profile.getRole();
        return form;
    }

    /** Null when nothing or an unknown profile was chosen. */
    public UserRole getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = COURT_ROLES.stream().filter(candidate -> candidate.name().equals(role)).findFirst().orElse(null);
    }

    @Override
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>(super.validate());
        if (role == null) {
            errors.put("role", "Seleccione el perfil del usuario.");
        }
        return errors;
    }
}

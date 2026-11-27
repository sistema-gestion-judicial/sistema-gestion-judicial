package mx.edu.utez.judicial.oficialia.model;

import java.util.List;
import java.util.Map;

/**
 * The data typed to correct a user of the court (RF 1.6): the same fields and rules as the registration
 * plus the profile. It has no password field. Whether the e-mail belongs to another user is checked by the
 * service, which can look at the stored accounts.
 */
public class UserEditForm extends NewUserForm {

    /** The profiles a court user can have; the judicial body administrator belongs to no court. */
    public static final List<UserRole> COURT_ROLES = List.of(UserRole.CLERK, UserRole.COURT_ADMIN);

    private String role = "";

    /** The form filled with the stored data, so it opens with the current values. */
    public static UserEditForm of(UserDetails user) {
        UserEditForm form = new UserEditForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setEmail(user.getEmail());
        form.setPhone(user.getPhone());
        form.setRole(user.getRole().name());
        return form;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role == null ? "" : role.trim();
    }

    /** The chosen profile, or null when it is missing or is not a profile of a court user. */
    public UserRole getRoleValue() {
        return COURT_ROLES.stream().filter(candidate -> candidate.name().equals(role)).findFirst().orElse(null);
    }

    @Override
    public Map<String, String> validate() {
        Map<String, String> errors = super.validate();
        if (getRoleValue() == null) {
            errors.put("role", "Elija el perfil.");
        }
        return errors;
    }

    /** The values to store for the user with that id. */
    public UserUpdate toUpdate(long id) {
        return new UserUpdate(id, getFirstName(), getLastName(), getEmail(), getPhoneDigits(), getRoleValue());
    }
}

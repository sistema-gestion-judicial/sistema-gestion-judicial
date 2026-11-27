package mx.edu.utez.judicial.audiencias.security;

import java.io.Serial;
import java.io.Serializable;
import mx.edu.utez.judicial.audiencias.model.user.Role;

/**
 * What the session keeps about the person who logged in. It never holds the password or its hash.
 * Plain getters (not a record) so the JSP expression language can read them.
 */
public final class SessionUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String fullName;
    private final Role role;

    public SessionUser(int id, String fullName, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAdmin() {
        return role == Role.ADMINISTRATOR;
    }

    public String getRoleLabel() {
        return isAdmin() ? "Administrador" : "Abogado de audiencia";
    }

    /** Up to two initials, for the avatar: "Ricardo Salgado Pérez" gives "RS". */
    public String getInitials() {
        if (fullName == null || fullName.isBlank()) {
            return "?";
        }
        StringBuilder initials = new StringBuilder();
        for (String word : fullName.trim().split("\\s+")) {
            initials.appendCodePoint(Character.toUpperCase(word.codePointAt(0)));
            if (initials.length() == 2) {
                break;
            }
        }
        return initials.toString();
    }
}

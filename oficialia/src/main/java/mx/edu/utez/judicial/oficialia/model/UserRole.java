package mx.edu.utez.judicial.oficialia.model;

/**
 * Profile of a user. It decides which actions the user can take; labels are shown in Spanish.
 */
public enum UserRole {

    CLERK("Oficial de Partes"),
    COURT_ADMIN("Administrador de Juzgado");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

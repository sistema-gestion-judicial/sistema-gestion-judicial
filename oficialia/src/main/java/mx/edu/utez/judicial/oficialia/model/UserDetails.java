package mx.edu.utez.judicial.oficialia.model;

/**
 * The stored data of one user that the Administrador de Juzgado can correct (RF 1.6). The password hash is
 * left out on purpose: this option never shows nor changes it.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class UserDetails {

    private final long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final UserRole role;
    private final String courtName;
    private final boolean active;

    public UserDetails(long id, String firstName, String lastName, String email, String phone, UserRole role,
                       String courtName, boolean active) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.courtName = courtName;
        this.active = active;
    }

    public long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getEmail() {
        return email;
    }

    /** Null when the user has no phone number. */
    public String getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public String getCourtName() {
        return courtName;
    }

    public boolean isActive() {
        return active;
    }
}

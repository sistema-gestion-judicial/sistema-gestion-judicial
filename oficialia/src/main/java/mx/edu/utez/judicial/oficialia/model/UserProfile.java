package mx.edu.utez.judicial.oficialia.model;

/**
 * The stored data of a user account that an Administrador de Juzgado can see and correct (DFR 1.6). Never
 * carries the password. Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only
 * reads getX() methods.
 */
public class UserProfile {

    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final UserRole role;
    private final String courtName;
    private final boolean active;

    public UserProfile(String firstName, String lastName, String email, String phone, UserRole role,
                       String courtName, boolean active) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.courtName = courtName;
        this.active = active;
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

    /** Only digits, or null when the user has no phone number. */
    public String getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    /** Null for the Administrador del órgano judicial, who has no court. */
    public String getCourtName() {
        return courtName;
    }

    public boolean isActive() {
        return active;
    }
}

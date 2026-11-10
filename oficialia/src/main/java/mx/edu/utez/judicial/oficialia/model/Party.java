package mx.edu.utez.judicial.oficialia.model;

/**
 * Plaintiff or defendant of a demand request. A plaintiff can be anonymous and a defendant unknown;
 * in that case every field except the role is empty.
 */
public class Party {

    private final PartyRole role;
    private final String name;
    private final String personType;
    private final String personId;
    private final String phone;
    private final String email;

    public Party(PartyRole role, String name, String personType, String personId, String phone, String email) {
        this.role = role;
        this.name = name;
        this.personType = personType;
        this.personId = personId;
        this.phone = phone;
        this.email = email;
    }

    /** A plaintiff registered as anonymous or a defendant registered as unknown. */
    public static Party unidentified(PartyRole role) {
        return new Party(role, null, null, null, null, null);
    }

    public PartyRole getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public boolean isIdentified() {
        return name != null;
    }

    /** Name to show; an unidentified plaintiff is anonymous and an unidentified defendant is unknown. */
    public String getDisplayName() {
        if (name != null) {
            return name;
        }
        return role == PartyRole.PLAINTIFF ? "Anónimo" : "Desconocido";
    }

    public String getPersonType() {
        return personType;
    }

    public String getPersonId() {
        return personId;
    }

    /** RFC for a company or a person with one, CURP otherwise. */
    public String getPersonIdLabel() {
        return personId != null && personId.length() == 18 ? "CURP" : "RFC";
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}

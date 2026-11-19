package mx.edu.utez.judicial.oficialia.model;

/**
 * Model for a DISTRITO JUDICIAL (DFR 2.1). Its code is the stable key the courts and the API use and never
 * changes; the name and the seat can be updated (DFR 2.3).
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class JudicialDistrict {

    private final String code;
    private final String name;
    private final String seatMunicipality;
    private final boolean active;

    public JudicialDistrict(String code, String name, String seatMunicipality, boolean active) {
        this.code = code;
        this.name = name;
        this.seatMunicipality = seatMunicipality;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSeatMunicipality() {
        return seatMunicipality;
    }

    public boolean isActive() {
        return active;
    }
}

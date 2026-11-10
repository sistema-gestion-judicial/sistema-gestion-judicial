package mx.edu.utez.judicial.oficialia.model;

/**
 * Model for a JUZGADO: Its two-digit code is the last part of every case file number.
 */
public class Court {

    private final String code;
    private final String name;
    private final String judicialDistrict;
    private final boolean active;

    public Court(String code, String name, String judicialDistrict, boolean active) {
        this.code = code;
        this.name = name;
        this.judicialDistrict = judicialDistrict;
        this.active = active;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getJudicialDistrict() {
        return judicialDistrict;
    }

    public boolean isActive() {
        return active;
    }
}

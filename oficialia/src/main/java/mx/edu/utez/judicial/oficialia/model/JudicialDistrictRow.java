package mx.edu.utez.judicial.oficialia.model;

/**
 * A row of the judicial districts catalog (DFR 2.2): the district and how many courts belong to it.
 * Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1) only reads getX() methods.
 */
public class JudicialDistrictRow {

    private final JudicialDistrict district;
    private final int courtCount;

    public JudicialDistrictRow(JudicialDistrict district, int courtCount) {
        this.district = district;
        this.courtCount = courtCount;
    }

    public String getCode() {
        return district.getCode();
    }

    public String getName() {
        return district.getName();
    }

    public String getSeatMunicipality() {
        return district.getSeatMunicipality();
    }

    public boolean isActive() {
        return district.isActive();
    }

    public int getCourtCount() {
        return courtCount;
    }
}

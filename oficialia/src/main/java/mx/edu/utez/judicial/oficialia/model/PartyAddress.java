package mx.edu.utez.judicial.oficialia.model;

/**
 * Address of a party of a demand request (DFR 3.1): street and number, neighborhood, postal code and
 * municipality. A class instead of a record so the JSP pages can read it.
 */
public class PartyAddress {

    private final String street;
    private final String neighborhood;
    private final String postalCode;
    private final String municipality;

    public PartyAddress(String street, String neighborhood, String postalCode, String municipality) {
        this.street = street;
        this.neighborhood = neighborhood;
        this.postalCode = postalCode;
        this.municipality = municipality;
    }

    public String getStreet() {
        return street;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getMunicipality() {
        return municipality;
    }

    /** The address in one line, for example "Calle Hidalgo 12, Centro, C.P. 62740, Cuautla". */
    public String getFullText() {
        return street + ", " + neighborhood + ", C.P. " + postalCode + ", " + municipality;
    }
}

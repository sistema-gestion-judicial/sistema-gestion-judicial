package mx.edu.utez.judicial.oficialia.model;

/**
 * Where a court is (DFR 2.9). Every part can be null for the courts registered before V003, which only
 * had a name and a district.
 */
public record CourtAddress(String street, String neighborhood, String postalCode, String municipality,
                           String phone) {

    public static final CourtAddress EMPTY = new CourtAddress(null, null, null, null, null);
}

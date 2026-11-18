package mx.edu.utez.judicial.oficialia.model;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The data typed to update a court (DFR 2.11). The code and the number are not part of it: they cannot be
 * edited. Whether the name is taken or the district and the subject matters exist is checked by the service,
 * which can look at the catalog.
 */
public class CourtForm {

    public static final int MAX_NAME_LENGTH = 120;
    public static final int MAX_STREET_LENGTH = 150;
    public static final int MAX_NEIGHBORHOOD_LENGTH = 100;
    public static final int MAX_MUNICIPALITY_LENGTH = 80;
    private static final int PHONE_DIGITS = 10;

    private String name = "";
    private String districtCode = "";
    private String street = "";
    private String neighborhood = "";
    private String postalCode = "";
    private String municipality = "";
    private String phone = "";
    private List<String> subjectMatterCodes = List.of();

    /** A form that starts with the current values of the court. */
    public static CourtForm of(Court court) {
        CourtForm form = new CourtForm();
        CourtAddress address = court.getAddress();
        form.setName(court.getName());
        form.setDistrictCode(court.getDistrictCode());
        form.setStreet(address.street());
        form.setNeighborhood(address.neighborhood());
        form.setPostalCode(address.postalCode());
        form.setMunicipality(address.municipality());
        form.setPhone(address.phone());
        form.setSubjectMatterCodes(court.getSubjectMatterCodes().toArray(String[]::new));
        return form;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = clean(name);
    }

    public String getDistrictCode() {
        return districtCode;
    }

    public void setDistrictCode(String districtCode) {
        this.districtCode = clean(districtCode);
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = clean(street);
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = clean(neighborhood);
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = clean(postalCode);
    }

    public String getMunicipality() {
        return municipality;
    }

    public void setMunicipality(String municipality) {
        this.municipality = clean(municipality);
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = clean(phone);
    }

    /** The phone as stored: only its digits, or null when none was typed. */
    public String getPhoneDigits() {
        String digits = phone.replaceAll("\\D", "");
        return digits.isEmpty() ? null : digits;
    }

    public List<String> getSubjectMatterCodes() {
        return subjectMatterCodes;
    }

    /** The codes of the checked subject matters; null (nothing checked) is the same as empty. */
    public void setSubjectMatterCodes(String[] codes) {
        this.subjectMatterCodes = codes == null ? List.of()
            : Arrays.stream(codes).map(CourtForm::clean).filter(code -> !code.isEmpty()).distinct().toList();
    }

    /** Field name to message, in Spanish; empty when the form is valid. */
    public Map<String, String> validate() {
        Map<String, String> errors = new LinkedHashMap<>();
        required(errors, "name", name, MAX_NAME_LENGTH, "el nombre del juzgado");
        if (districtCode.isEmpty()) {
            errors.put("districtCode", "Elija el distrito judicial.");
        }
        required(errors, "street", street, MAX_STREET_LENGTH, "la calle y el número");
        required(errors, "neighborhood", neighborhood, MAX_NEIGHBORHOOD_LENGTH, "la colonia");
        if (postalCode.isEmpty()) {
            errors.put("postalCode", "Escriba el código postal.");
        } else if (!postalCode.matches("[0-9]{5}")) {
            errors.put("postalCode", "El código postal debe tener 5 dígitos.");
        }
        required(errors, "municipality", municipality, MAX_MUNICIPALITY_LENGTH, "el municipio");
        if (!phone.isEmpty() && (!phone.matches("[0-9 ()+-]+") || getPhoneDigits().length() != PHONE_DIGITS)) {
            errors.put("phone", "Escriba un teléfono de " + PHONE_DIGITS + " dígitos.");
        }
        if (subjectMatterCodes.isEmpty()) {
            errors.put("subjectMatterCodes", "Elija al menos una materia que atiende el juzgado.");
        }
        return errors;
    }

    private static void required(Map<String, String> errors, String field, String value, int maxLength,
                                 String label) {
        if (value.isEmpty()) {
            errors.put(field, "Escriba " + label + ".");
        } else if (value.length() > maxLength) {
            errors.put(field, "No puede pasar de " + maxLength + " caracteres.");
        }
    }

    /** Trims and collapses inner spaces; null becomes empty. */
    private static String clean(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}

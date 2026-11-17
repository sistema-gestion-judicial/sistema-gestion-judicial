package mx.edu.utez.judicial.oficialia.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A row of the courts catalog (DFR 2.10): the court with its address in one line and the names of the
 * subject matters it handles. Uses getters instead of a record because Expression Language 5.0 (Tomcat 10.1)
 * only reads getX() methods.
 */
public class CourtRow {

    private final Court court;
    private final List<String> subjectMatterNames;

    public CourtRow(Court court, List<String> subjectMatterNames) {
        this.court = court;
        this.subjectMatterNames = subjectMatterNames;
    }

    public String getCode() {
        return court.getCode();
    }

    public int getNumber() {
        return court.getNumber();
    }

    public String getName() {
        return court.getName();
    }

    public String getJudicialDistrict() {
        return court.getJudicialDistrict();
    }

    /** Street, neighborhood, postal code and municipality, skipping the parts the court does not have. */
    public String getFullAddress() {
        CourtAddress address = court.getAddress() == null ? CourtAddress.EMPTY : court.getAddress();
        return Stream.of(address.street(),
                address.neighborhood() == null ? null : "Col. " + address.neighborhood(),
                address.postalCode() == null ? null : "C.P. " + address.postalCode(),
                address.municipality())
            .filter(Objects::nonNull)
            .filter(part -> !part.isBlank())
            .collect(Collectors.joining(", "));
    }

    public String getPhone() {
        return court.getAddress() == null ? null : court.getAddress().phone();
    }

    public String getSubjectMatters() {
        return String.join(", ", subjectMatterNames);
    }

    public boolean isActive() {
        return court.isActive();
    }
}

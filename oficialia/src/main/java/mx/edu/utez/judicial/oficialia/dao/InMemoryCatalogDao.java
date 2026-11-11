package mx.edu.utez.judicial.oficialia.dao;

import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;

/**
 * Same catalogs as the V001, V003 and V004 scripts leave in the database, for running without one.
 */
public class InMemoryCatalogDao implements CatalogDao {

    private static final List<String> ALL_MATTERS = List.of("CIV", "FAM", "MER", "PEN");

    private static final List<Court> COURTS = List.of(
        court("01", "Juzgado de Cuernavaca", "CVA", "Distrito judicial de Cuernavaca", "Cuernavaca"),
        court("05", "Juzgado de Yautepec", "YAU", "Distrito judicial de Yautepec", "Yautepec"),
        court("07", "Juzgado de Jojutla", "JOJ", "Distrito judicial de Jojutla", "Jojutla"),
        court("12", "Juzgado de Cuautla", "CUA", "Distrito judicial de Cuautla", "Cuautla"));

    private static final List<SubjectMatter> SUBJECT_MATTERS = List.of(
        new SubjectMatter(1, "CIV", "Civil", null, true), new SubjectMatter(2, "FAM", "Familiar", null, true),
        new SubjectMatter(3, "MER", "Mercantil", null, true), new SubjectMatter(4, "PEN", "Penal", null, true));

    @Override
    public List<Court> findCourts() {
        return COURTS;
    }

    @Override
    public Optional<Court> findCourt(String code) {
        return COURTS.stream().filter(court -> court.getCode().equals(code)).findFirst();
    }

    @Override
    public List<SubjectMatter> findSubjectMatters() {
        return SUBJECT_MATTERS;
    }

    private static Court court(String code, String name, String districtCode, String districtName,
                               String municipality) {
        CourtAddress address = new CourtAddress(null, null, null, municipality, null);
        return new Court(code, Integer.parseInt(code), name, districtCode, districtName, address, ALL_MATTERS, true);
    }
}

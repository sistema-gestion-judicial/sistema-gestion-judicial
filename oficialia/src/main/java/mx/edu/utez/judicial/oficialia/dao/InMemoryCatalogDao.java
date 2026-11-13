package mx.edu.utez.judicial.oficialia.dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Same catalogs as the V001, V003 and V004 scripts leave in the database, for running without one.
 * Changes live only as long as the server runs.
 */
public class InMemoryCatalogDao implements CatalogDao {

    private static final List<String> ALL_MATTERS = List.of("CIV", "FAM", "MER", "PEN");

    private static final List<SubjectMatter> SUBJECT_MATTERS = List.of(
        new SubjectMatter(1, "CIV", "Civil", null, true), new SubjectMatter(2, "FAM", "Familiar", null, true),
        new SubjectMatter(3, "MER", "Mercantil", null, true), new SubjectMatter(4, "PEN", "Penal", null, true));

    private List<JudicialDistrict> districts = List.of(
        new JudicialDistrict("CVA", "Distrito judicial de Cuernavaca", "Cuernavaca", true),
        new JudicialDistrict("YAU", "Distrito judicial de Yautepec", "Yautepec", true),
        new JudicialDistrict("JOJ", "Distrito judicial de Jojutla", "Jojutla", true),
        new JudicialDistrict("CUA", "Distrito judicial de Cuautla", "Cuautla", true));

    private final List<Court> courts = List.of(
        court("01", "Juzgado de Cuernavaca", "CVA", "Distrito judicial de Cuernavaca", "Cuernavaca"),
        court("05", "Juzgado de Yautepec", "YAU", "Distrito judicial de Yautepec", "Yautepec"),
        court("07", "Juzgado de Jojutla", "JOJ", "Distrito judicial de Jojutla", "Jojutla"),
        court("12", "Juzgado de Cuautla", "CUA", "Distrito judicial de Cuautla", "Cuautla"));

    @Override
    public synchronized List<JudicialDistrict> findJudicialDistricts() {
        return districts.stream()
            .sorted(Comparator.comparing(district -> CatalogNames.key(district.getName())))
            .toList();
    }

    @Override
    public synchronized Optional<JudicialDistrict> findJudicialDistrict(String code) {
        return districts.stream().filter(district -> district.getCode().equals(code)).findFirst();
    }

    @Override
    public synchronized void insertJudicialDistrict(JudicialDistrict district) {
        if (districts.stream().anyMatch(other -> other.getCode().equalsIgnoreCase(district.getCode()))) {
            throw new DuplicateCodeException(district.getCode(), null);
        }
        if (districts.stream().anyMatch(other -> CatalogNames.sameName(other.getName(), district.getName()))) {
            throw new DuplicateNameException(district.getName(), null);
        }
        List<JudicialDistrict> updated = new ArrayList<>(districts);
        updated.add(district);
        districts = List.copyOf(updated);
    }

    @Override
    public List<Court> findCourts() {
        return courts;
    }

    @Override
    public Optional<Court> findCourt(String code) {
        return courts.stream().filter(court -> court.getCode().equals(code)).findFirst();
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

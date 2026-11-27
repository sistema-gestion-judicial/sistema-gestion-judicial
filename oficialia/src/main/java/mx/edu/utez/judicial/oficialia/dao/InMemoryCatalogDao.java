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

    private List<SubjectMatter> subjectMatters = List.of(
        new SubjectMatter(1, "CIV", "Civil", null, true), new SubjectMatter(2, "FAM", "Familiar", null, true),
        new SubjectMatter(3, "MER", "Mercantil", null, true), new SubjectMatter(4, "PEN", "Penal", null, true));

    private List<JudicialDistrict> districts = List.of(
        new JudicialDistrict("CVA", "Distrito judicial de Cuernavaca", "Cuernavaca", true),
        new JudicialDistrict("YAU", "Distrito judicial de Yautepec", "Yautepec", true),
        new JudicialDistrict("JOJ", "Distrito judicial de Jojutla", "Jojutla", true),
        new JudicialDistrict("CUA", "Distrito judicial de Cuautla", "Cuautla", true));

    private List<Court> courts = List.of(
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
    public synchronized void updateJudicialDistrict(String code, String name, String seatMunicipality) {
        if (districts.stream().anyMatch(other -> !other.getCode().equals(code)
                && CatalogNames.sameName(other.getName(), name))) {
            throw new DuplicateNameException(name, null);
        }
        districts = districts.stream()
            .map(district -> district.getCode().equals(code)
                ? new JudicialDistrict(code, name, seatMunicipality, district.isActive()) : district)
            .toList();
        // Courts keep the name of their district as a copy, so they are rebuilt with the new one
        courts = courts.stream()
            .map(court -> court.getDistrictCode().equals(code) ? withDistrictName(court, name) : court)
            .toList();
    }

    @Override
    public synchronized List<Court> findCourts() {
        return courts;
    }

    @Override
    public synchronized Optional<Court> findCourt(String code) {
        return courts.stream().filter(court -> court.getCode().equals(code)).findFirst();
    }

    @Override
    public synchronized List<SubjectMatter> findSubjectMatters() {
        return subjectMatters.stream()
            .sorted(Comparator.comparing(matter -> CatalogNames.key(matter.getName())))
            .toList();
    }

    @Override
    public synchronized Optional<SubjectMatter> findSubjectMatter(String code) {
        return subjectMatters.stream().filter(matter -> matter.getCode().equals(code)).findFirst();
    }

    @Override
    public synchronized void insertSubjectMatter(SubjectMatter matter) {
        if (subjectMatters.stream().anyMatch(other -> other.getCode().equalsIgnoreCase(matter.getCode()))) {
            throw new DuplicateCodeException(matter.getCode(), null);
        }
        if (subjectMatters.stream().anyMatch(other -> CatalogNames.sameName(other.getName(), matter.getName()))) {
            throw new DuplicateNameException(matter.getName(), null);
        }
        int id = subjectMatters.stream().mapToInt(SubjectMatter::getId).max().orElse(0) + 1;
        List<SubjectMatter> updated = new ArrayList<>(subjectMatters);
        updated.add(new SubjectMatter(id, matter.getCode(), matter.getName(), matter.getDescription(),
            matter.isActive()));
        subjectMatters = List.copyOf(updated);
    }

    @Override
    public synchronized void updateSubjectMatter(String code, String name, String description) {
        if (subjectMatters.stream().anyMatch(other -> !other.getCode().equals(code)
                && CatalogNames.sameName(other.getName(), name))) {
            throw new DuplicateNameException(name, null);
        }
        subjectMatters = subjectMatters.stream()
            .map(matter -> matter.getCode().equals(code)
                ? new SubjectMatter(matter.getId(), code, name, description, matter.isActive()) : matter)
            .toList();
    }

    @Override
    public synchronized void updateSubjectMatterStatus(String code, boolean active) {
        subjectMatters = subjectMatters.stream()
            .map(matter -> matter.getCode().equals(code) ? new SubjectMatter(matter.getId(), code,
                matter.getName(), matter.getDescription(), active) : matter)
            .toList();
    }

    private static Court withDistrictName(Court court, String districtName) {
        return new Court(court.getCode(), court.getNumber(), court.getName(), court.getDistrictCode(), districtName,
            court.getAddress(), court.getSubjectMatterCodes(), court.isActive());
    }

    private static Court court(String code, String name, String districtCode, String districtName,
                               String municipality) {
        CourtAddress address = new CourtAddress(null, null, null, municipality, null);
        return new Court(code, Integer.parseInt(code), name, districtCode, districtName, address, ALL_MATTERS, true);
    }
}

package mx.edu.utez.judicial.oficialia.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import mx.edu.utez.judicial.oficialia.dao.CatalogDao;
import mx.edu.utez.judicial.oficialia.dao.OficialiaDaos;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtRow;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.util.CatalogNames;

/**
 * Lets every profile look up the courts catalog (DFR 2.10), for example to find the court named in a
 * duplicity warning. An Oficial de Partes only sees the active courts.
 */
public class CourtService {

    public static final int PAGE_SIZE = 10;

    private final CatalogDao catalogDao;

    public CourtService() {
        this(OficialiaDaos.catalogs());
    }

    public CourtService(CatalogDao catalogDao) {
        this.catalogDao = catalogDao;
    }

    /** The Oficial de Partes works only with active courts, so they neither see the inactive ones nor filter them. */
    public static boolean canSeeInactive(User user) {
        return user != null && user.getRole() != UserRole.CLERK;
    }

    /**
     * The page of the catalog that matches the filters, ordered by name. The text is looked for in the code,
     * the number, the name, the district and the municipality, ignoring case and accents. Null filters do not
     * filter; the status filter is ignored for an Oficial de Partes, who only gets active courts.
     *
     * @throws AccessDeniedException when nobody is signed in
     */
    public ResultPage<CourtRow> search(User user, String text, String districtCode, String subjectMatterCode,
                                       Boolean active, int page) {
        if (user == null) {
            throw new AccessDeniedException("Inicie sesión para consultar los juzgados.");
        }
        Boolean status = canSeeInactive(user) ? active : Boolean.TRUE;
        String key = CatalogNames.key(text);
        Map<String, String> matterNames = catalogDao.findSubjectMatters().stream()
            .collect(Collectors.toMap(SubjectMatter::getCode, SubjectMatter::getName, (first, second) -> first));
        List<CourtRow> rows = catalogDao.findCourts().stream()
            .filter(court -> status == null || court.isActive() == status)
            .filter(court -> isBlank(districtCode) || districtCode.equals(court.getDistrictCode()))
            .filter(court -> isBlank(subjectMatterCode) || court.getSubjectMatterCodes().contains(subjectMatterCode))
            .filter(court -> key.isEmpty() || matches(court, key))
            .sorted(Comparator.comparing(court -> CatalogNames.key(court.getName())))
            .map(court -> new CourtRow(court, court.getSubjectMatterCodes().stream()
                .map(code -> matterNames.getOrDefault(code, code)).toList()))
            .toList();
        return ResultPage.of(rows, page, PAGE_SIZE);
    }

    /** The districts offered in the district filter, ordered by name. */
    public List<JudicialDistrict> districts() {
        return catalogDao.findJudicialDistricts();
    }

    /** The subject matters offered in the subject matter filter, ordered by name. */
    public List<SubjectMatter> subjectMatters() {
        return catalogDao.findSubjectMatters().stream()
            .sorted(Comparator.comparing((SubjectMatter matter) -> CatalogNames.key(matter.getName())))
            .toList();
    }

    private static boolean matches(Court court, String key) {
        String municipality = court.getAddress() == null ? null : court.getAddress().municipality();
        return Stream.of(court.getCode(), String.valueOf(court.getNumber()), court.getName(),
                court.getJudicialDistrict(), municipality)
            .anyMatch(value -> CatalogNames.key(value).contains(key));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

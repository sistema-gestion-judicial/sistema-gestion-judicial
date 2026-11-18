package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import mx.edu.utez.judicial.common.db.Database;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs only against the test database described in {@link TestDatabase}.
 */
class JdbcCatalogDaoTest {

    private Database database;
    private JdbcCatalogDao dao;

    @BeforeEach
    void freshSchema() throws Exception {
        database = TestDatabase.migrated();
        dao = new JdbcCatalogDao(database);
    }

    @Test
    void readsCourtsWithTheirDistrictNumberAndMatters() {
        List<Court> courts = dao.findCourts();

        assertEquals(List.of("Juzgado de Cuautla", "Juzgado de Cuernavaca", "Juzgado de Jojutla",
            "Juzgado de Yautepec"), courts.stream().map(Court::getName).toList());
        Court cuernavaca = dao.findCourt("01").orElseThrow();
        assertEquals(1, cuernavaca.getNumber());
        assertEquals("CVA", cuernavaca.getDistrictCode());
        assertEquals("Cuernavaca", cuernavaca.getAddress().municipality());
        assertEquals(List.of("CIV", "FAM", "MER", "PEN"), cuernavaca.getSubjectMatterCodes());
        assertTrue(dao.findCourt("99").isEmpty());
    }

    @Test
    void givesEverySubjectMatterItsStableCode() {
        List<SubjectMatter> matters = dao.findSubjectMatters();

        assertEquals(List.of("CIV", "FAM", "MER", "PEN"), matters.stream().map(SubjectMatter::getCode).toList());
        assertEquals(List.of("Civil", "Familiar", "Mercantil", "Penal"),
            matters.stream().map(SubjectMatter::getName).toList());
    }

    @Test
    void readsTheJudicialDistrictsOrderedByName() {
        assertEquals(List.of("CUA", "CVA", "JOJ", "YAU"),
            dao.findJudicialDistricts().stream().map(JudicialDistrict::getCode).toList());
        JudicialDistrict cuautla = dao.findJudicialDistrict("CUA").orElseThrow();
        assertEquals("Distrito judicial de Cuautla", cuautla.getName());
        assertEquals("Cuautla", cuautla.getSeatMunicipality());
        assertTrue(cuautla.isActive());
        assertTrue(dao.findJudicialDistrict("ZZZ").isEmpty());
    }

    @Test
    void savesANewDistrictAsActive() {
        dao.insertJudicialDistrict(new JudicialDistrict("TEP", "Distrito judicial de Tetecala", "Tetecala", true));

        JudicialDistrict saved = dao.findJudicialDistrict("TEP").orElseThrow();
        assertEquals("Tetecala", saved.getSeatMunicipality());
        assertTrue(saved.isActive());
        assertEquals(5, dao.findJudicialDistricts().size());
    }

    @Test
    void tellsWhetherTheCodeOrTheNameWasRepeated() {
        assertThrows(DuplicateCodeException.class, () -> dao.insertJudicialDistrict(
            new JudicialDistrict("CVA", "Un nombre distinto", "Cuernavaca", true)));
        assertThrows(DuplicateNameException.class, () -> dao.insertJudicialDistrict(
            new JudicialDistrict("NEW", "distrito judicial de cuáutla", "Cuautla", true)));
        assertEquals(4, dao.findJudicialDistricts().size());
    }

    @Test
    void updatesTheDistrictAndTheNameThatItsCourtsShow() {
        dao.updateJudicialDistrict("CUA", "Distrito judicial Oriente", "Cuautla de Morelos");

        JudicialDistrict updated = dao.findJudicialDistrict("CUA").orElseThrow();
        assertEquals("Distrito judicial Oriente", updated.getName());
        assertEquals("Cuautla de Morelos", updated.getSeatMunicipality());
        assertTrue(updated.isActive());
        assertEquals("Distrito judicial Oriente", dao.findCourt("12").orElseThrow().getJudicialDistrict());
    }

    @Test
    void keepsEverythingWhenTheNewNameBelongsToAnotherDistrict() {
        assertThrows(DuplicateNameException.class,
            () -> dao.updateJudicialDistrict("CUA", "distrito judicial de cuernavaca", "Otra sede"));

        JudicialDistrict unchanged = dao.findJudicialDistrict("CUA").orElseThrow();
        assertEquals("Distrito judicial de Cuautla", unchanged.getName());
        assertEquals("Cuautla", unchanged.getSeatMunicipality());
    }

    private static Court newCourt(String code, int number, String name, List<String> matters) {
        return new Court(code, number, name, "CVA", "Distrito judicial de Cuernavaca",
            new CourtAddress("Calle Hidalgo 12", "Centro", "62000", "Tetecala", "7771234567"), matters, true);
    }

    private static NewUserAccount administrator(String email, String courtCode) {
        return new NewUserAccount("Laura", "Méndez", email, UserRole.COURT_ADMIN, courtCode, "hash", true, true);
    }

    private int count(String sql, String value) throws Exception {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet rows = statement.executeQuery()) {
                rows.next();
                return rows.getInt(1);
            }
        }
    }

    @Test
    void insertsACourtWithItsAddressMattersAndAdministrator() throws Exception {
        dao.insertCourt(newCourt("20", 20, "Juzgado de Tetecala", List.of("CIV", "FAM")),
            administrator("laura@utez.edu.mx", "20"));

        Court saved = dao.findCourt("20").orElseThrow();
        assertEquals(20, saved.getNumber());
        assertEquals("CVA", saved.getDistrictCode());
        assertEquals("Distrito judicial de Cuernavaca", saved.getJudicialDistrict());
        assertEquals("62000", saved.getAddress().postalCode());
        assertEquals("7771234567", saved.getAddress().phone());
        assertEquals(List.of("CIV", "FAM"), saved.getSubjectMatterCodes());
        assertTrue(saved.isActive());
        assertEquals(1, count("SELECT COUNT(*) FROM users WHERE court_code = ? AND role = 'COURT_ADMIN' "
            + "AND must_change_password = TRUE", "20"));
    }

    @Test
    void tellsWhichUniqueValueWasRepeated() {
        assertThrows(DuplicateCodeException.class,
            () -> dao.insertCourt(newCourt("01", 40, "Otro", List.of("CIV")), administrator("a@utez.edu.mx", "01")));
        assertThrows(DuplicateNumberException.class,
            () -> dao.insertCourt(newCourt("40", 12, "Otro", List.of("CIV")), administrator("b@utez.edu.mx", "40")));
        assertThrows(DuplicateNameException.class, () -> dao.insertCourt(
            newCourt("40", 40, "Juzgado de Cuautla", List.of("CIV")), administrator("c@utez.edu.mx", "40")));
        assertEquals(4, dao.findCourts().size());
    }

    @Test
    void aRepeatedAdministratorEmailRollsEverythingBack() throws Exception {
        dao.insertCourt(newCourt("20", 20, "Juzgado de Tetecala", List.of("CIV")),
            administrator("laura@utez.edu.mx", "20"));

        assertThrows(DuplicateEmailException.class, () -> dao.insertCourt(
            newCourt("21", 21, "Juzgado de Miacatlán", List.of("CIV", "PEN")),
            administrator("laura@utez.edu.mx", "21")));

        assertTrue(dao.findCourt("21").isEmpty());
        assertEquals(0, count("SELECT COUNT(*) FROM court_subject_matters WHERE court_code = ?", "21"));
    }

    @Test
    void anUnknownSubjectMatterRollsEverythingBack() throws Exception {
        assertThrows(DataAccessException.class, () -> dao.insertCourt(
            newCourt("22", 22, "Juzgado de Puente de Ixtla", List.of("CIV", "XXX")),
            administrator("pedro@utez.edu.mx", "22")));

        assertTrue(dao.findCourt("22").isEmpty());
        assertEquals(0, count("SELECT COUNT(*) FROM users WHERE court_code = ?", "22"));
    }
}

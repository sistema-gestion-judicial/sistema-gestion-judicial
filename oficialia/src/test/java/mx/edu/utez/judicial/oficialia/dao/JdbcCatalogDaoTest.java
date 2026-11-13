package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Runs only against the test database described in {@link TestDatabase}.
 */
class JdbcCatalogDaoTest {

    private JdbcCatalogDao dao;

    @BeforeEach
    void freshSchema() throws Exception {
        dao = new JdbcCatalogDao(TestDatabase.migrated());
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
}

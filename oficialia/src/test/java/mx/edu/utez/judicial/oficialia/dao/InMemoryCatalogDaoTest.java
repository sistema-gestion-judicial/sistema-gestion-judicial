package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class InMemoryCatalogDaoTest {

    private final InMemoryUserDao users = new InMemoryUserDao(List.<UserAccount>of(), code -> code);
    private final InMemoryCatalogDao dao = new InMemoryCatalogDao(() -> users);

    private static Court newCourt(String code, int number, String name) {
        return new Court(code, number, name, "CVA", "Distrito judicial de Cuernavaca",
            new CourtAddress("Calle Hidalgo 12", "Centro", "62000", "Tetecala", null), List.of("CIV"), true);
    }

    private static NewUserAccount administrator(String email, String courtCode) {
        return new NewUserAccount("Laura", "Méndez", email, UserRole.COURT_ADMIN, courtCode, "hash", true, true);
    }

    @Test
    void startsWithTheFourDistrictsOfTheSampleCatalog() {
        assertEquals(List.of("CUA", "CVA", "JOJ", "YAU"),
            dao.findJudicialDistricts().stream().map(JudicialDistrict::getCode).toList());
    }

    @Test
    void insertsADistrictThatCanBeFoundByItsCode() {
        dao.insertJudicialDistrict(new JudicialDistrict("TEP", "Distrito judicial de Tetecala", "Tetecala", true));

        assertEquals("Tetecala", dao.findJudicialDistrict("TEP").orElseThrow().getSeatMunicipality());
        assertEquals(5, dao.findJudicialDistricts().size());
    }

    @Test
    void refusesARepeatedCodeOrName() {
        assertThrows(DuplicateCodeException.class, () -> dao.insertJudicialDistrict(
            new JudicialDistrict("cva", "Otro nombre", "Cuernavaca", true)));
        assertThrows(DuplicateNameException.class, () -> dao.insertJudicialDistrict(
            new JudicialDistrict("NEW", "Distrito judicial de Cuernavaca", "Cuernavaca", true)));
        assertTrue(dao.findJudicialDistrict("NEW").isEmpty());
    }

    @Test
    void updatesTheNameAndTheSeatOfADistrictAndRenamesItsCourts() {
        dao.updateJudicialDistrict("CUA", "Distrito judicial Oriente", "Cuautla de Morelos");

        JudicialDistrict updated = dao.findJudicialDistrict("CUA").orElseThrow();
        assertEquals("Distrito judicial Oriente", updated.getName());
        assertEquals("Cuautla de Morelos", updated.getSeatMunicipality());
        assertTrue(updated.isActive());
        assertEquals("Distrito judicial Oriente", dao.findCourt("12").orElseThrow().getJudicialDistrict());
        assertEquals("Distrito judicial de Cuernavaca", dao.findCourt("01").orElseThrow().getJudicialDistrict());
    }

    @Test
    void refusesToRenameADistrictWithTheNameOfAnotherButAllowsItsOwn() {
        assertThrows(DuplicateNameException.class,
            () -> dao.updateJudicialDistrict("CUA", "distrito judicial de cuernavaca", "Cuautla"));

        dao.updateJudicialDistrict("CUA", "DISTRITO JUDICIAL DE CUAUTLA", "Cuautla");
        assertEquals("DISTRITO JUDICIAL DE CUAUTLA", dao.findJudicialDistrict("CUA").orElseThrow().getName());
    }

    @Test
    void updatingAnUnknownDistrictDoesNothing() {
        dao.updateJudicialDistrict("ZZZ", "Nombre", "Sede");

        assertTrue(dao.findJudicialDistrict("ZZZ").isEmpty());
        assertEquals(4, dao.findJudicialDistricts().size());
    }

    @Test
    void insertsACourtAndItsAdministratorTogether() {
        dao.insertCourt(newCourt("20", 20, "Juzgado de Tetecala"), administrator("laura@utez.edu.mx", "20"));

        assertEquals(20, dao.findCourt("20").orElseThrow().getNumber());
        assertEquals(5, dao.findCourts().size());
        UserAccount account = users.findByEmail("laura@utez.edu.mx").orElseThrow();
        assertEquals("Juzgado de Tetecala", account.user().getCourtName());
        assertEquals(UserRole.COURT_ADMIN, account.user().getRole());
    }

    @Test
    void refusesARepeatedCodeNumberOrNameAndKeepsTheCatalogAsItWas() {
        assertThrows(DuplicateCodeException.class,
            () -> dao.insertCourt(newCourt("01", 40, "Otro"), administrator("a@utez.edu.mx", "01")));
        assertThrows(DuplicateNumberException.class,
            () -> dao.insertCourt(newCourt("40", 12, "Otro"), administrator("b@utez.edu.mx", "40")));
        assertThrows(DuplicateNameException.class,
            () -> dao.insertCourt(newCourt("40", 40, "JUZGADO DE CUÁUTLA"), administrator("c@utez.edu.mx", "40")));

        assertEquals(4, dao.findCourts().size());
        assertTrue(users.findByEmail("a@utez.edu.mx").isEmpty());
        assertTrue(users.findByEmail("b@utez.edu.mx").isEmpty());
        assertTrue(users.findByEmail("c@utez.edu.mx").isEmpty());
    }

    @Test
    void aRepeatedAdministratorEmailLeavesNoCourtBehind() {
        dao.insertCourt(newCourt("20", 20, "Juzgado de Tetecala"), administrator("laura@utez.edu.mx", "20"));

        assertThrows(DuplicateEmailException.class,
            () -> dao.insertCourt(newCourt("21", 21, "Juzgado de Miacatlán"), administrator("LAURA@utez.edu.mx", "21")));

        assertTrue(dao.findCourt("21").isEmpty());
        assertEquals(5, dao.findCourts().size());
    }
}

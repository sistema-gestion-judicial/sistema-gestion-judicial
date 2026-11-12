package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class CourtServiceTest {

    private final User admin = new User("o@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null);
    private final InMemoryCatalogDao catalog = new InMemoryCatalogDao();
    private final CourtService service = new CourtService(catalog);

    private static CourtForm form(String name, String districtCode, String... matters) {
        CourtForm form = new CourtForm();
        form.setName(name);
        form.setDistrictCode(districtCode);
        form.setStreet("Av. Reforma 12");
        form.setNeighborhood("Centro");
        form.setPostalCode("62740");
        form.setMunicipality("Cuautla");
        form.setPhone("735 352 1234");
        form.setSubjectMatterCodes(matters);
        return form;
    }

    @Test
    void updatesNameDistrictAddressAndMattersAndTheCatalogShowsThem() {
        var saving = service.update(admin, "12", form("Juzgado Primero de Cuautla", "CVA", "CIV", "PEN"));

        assertTrue(saving.isSaved());
        Court updated = catalog.findCourt("12").orElseThrow();
        assertEquals("Juzgado Primero de Cuautla", updated.getName());
        assertEquals("CVA", updated.getDistrictCode());
        assertEquals("Distrito judicial de Cuernavaca", updated.getJudicialDistrict());
        assertEquals("Av. Reforma 12", updated.getAddress().street());
        assertEquals("62740", updated.getAddress().postalCode());
        assertEquals("7353521234", updated.getAddress().phone());
        assertEquals(List.of("CIV", "PEN"), updated.getSubjectMatterCodes());
        assertTrue(updated.isActive());
    }

    @Test
    void keepsTheCodeAndTheNumberLocked() {
        service.update(admin, "12", form("Juzgado Primero de Cuautla", "CUA", "CIV"));

        Court updated = catalog.findCourt("12").orElseThrow();
        assertEquals("12", updated.getCode());
        assertEquals(12, updated.getNumber());
        assertEquals(4, catalog.findCourts().size());
    }

    @Test
    void otherCourtsAreNotTouched() {
        Court before = catalog.findCourt("01").orElseThrow();

        service.update(admin, "12", form("Juzgado Primero de Cuautla", "CUA", "CIV"));

        Court after = catalog.findCourt("01").orElseThrow();
        assertEquals(before.getName(), after.getName());
        assertEquals(before.getSubjectMatterCodes(), after.getSubjectMatterCodes());
    }

    @Test
    void savesNothingWithoutMatters() {
        var saving = service.update(admin, "12", form("Juzgado Primero de Cuautla", "CUA"));

        assertFalse(saving.isSaved());
        assertTrue(saving.errors().containsKey("subjectMatterCodes"));
        assertEquals("Juzgado de Cuautla", catalog.findCourt("12").orElseThrow().getName());
        assertEquals(List.of("CIV", "FAM", "MER", "PEN"), catalog.findCourt("12").orElseThrow().getSubjectMatterCodes());
    }

    @Test
    void savesNothingWhenARequiredFieldIsMissing() {
        CourtForm incomplete = form("Juzgado Primero de Cuautla", "CUA", "CIV");
        incomplete.setStreet("");

        var saving = service.update(admin, "12", incomplete);

        assertFalse(saving.isSaved());
        assertEquals(List.of("street"), List.copyOf(saving.errors().keySet()));
        assertEquals("Juzgado de Cuautla", catalog.findCourt("12").orElseThrow().getName());
    }

    @Test
    void rejectsANameOfAnotherCourtIgnoringCaseAndAccents() {
        var saving = service.update(admin, "12", form("JUZGADO DE CUERNAVACA", "CUA", "CIV"));

        assertFalse(saving.isSaved());
        assertEquals(Map.of("name", "Este nombre ya pertenece a otro juzgado."), saving.errors());
    }

    @Test
    void allowsKeepingItsOwnName() {
        assertTrue(service.update(admin, "12", form("juzgado de cuautla", "CUA", "CIV")).isSaved());
    }

    @Test
    void rejectsADistrictOrAMatterThatDoesNotExist() {
        var unknownDistrict = service.update(admin, "12", form("Juzgado de Cuautla", "ZZZ", "CIV"));
        var unknownMatter = service.update(admin, "12", form("Juzgado de Cuautla", "CUA", "CIV", "XXX"));

        assertEquals("Elija un distrito judicial de la lista.", unknownDistrict.errors().get("districtCode"));
        assertEquals("Elija solo materias de la lista.", unknownMatter.errors().get("subjectMatterCodes"));
        assertEquals(List.of("CIV", "FAM", "MER", "PEN"), catalog.findCourt("12").orElseThrow().getSubjectMatterCodes());
    }

    @Test
    void reportsAnUnknownCourt() {
        assertThrows(NotFoundException.class, () -> service.get(admin, "99"));
        assertThrows(NotFoundException.class,
            () -> service.update(admin, "99", form("Juzgado nuevo", "CUA", "CIV")));
    }

    @Test
    void refusesEveryOtherProfile() {
        User courtAdmin = new User("a@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN, "Juzgado de Cuautla");
        User clerk = new User("c@utez.edu.mx", "Laura Méndez", UserRole.CLERK, "Juzgado de Cuautla");

        for (User other : List.of(courtAdmin, clerk)) {
            assertThrows(AccessDeniedException.class, () -> service.list(other));
            assertThrows(AccessDeniedException.class, () -> service.get(other, "12"));
            assertThrows(AccessDeniedException.class, () -> service.districts(other));
            assertThrows(AccessDeniedException.class, () -> service.subjectMatters(other));
            assertThrows(AccessDeniedException.class,
                () -> service.update(other, "12", form("Otro nombre", "CUA", "CIV")));
        }
        assertEquals("Juzgado de Cuautla", catalog.findCourt("12").orElseThrow().getName());
    }
}

package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrictForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class JudicialDistrictServiceTest {

    private final User admin = new User("o@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null);
    private final InMemoryCatalogDao catalog = new InMemoryCatalogDao();
    private final JudicialDistrictService service = new JudicialDistrictService(catalog);

    private static JudicialDistrictForm form(String code, String name, String seat) {
        JudicialDistrictForm form = new JudicialDistrictForm();
        form.setCode(code);
        form.setName(name);
        form.setSeatMunicipality(seat);
        return form;
    }

    @Test
    void savesTheDistrictAsActiveAndAddsItToTheCatalog() {
        var registration = service.register(admin, form("tep", "Distrito judicial de Tetecala", "Tetecala"));

        assertTrue(registration.isSaved());
        JudicialDistrict saved = catalog.findJudicialDistrict("TEP").orElseThrow();
        assertTrue(saved.isActive());
        assertEquals("Distrito judicial de Tetecala", saved.getName());
        assertEquals("Tetecala", saved.getSeatMunicipality());
        assertTrue(service.list(admin).stream().anyMatch(district -> district.getCode().equals("TEP")));
    }

    @Test
    void rejectsACodeThatAlreadyExistsIgnoringCase() {
        var registration = service.register(admin, form("cva", "Otro nombre", "Cuernavaca"));

        assertFalse(registration.isSaved());
        assertEquals(Map.of("code", "Esta clave ya existe."), registration.errors());
        assertEquals(4, catalog.findJudicialDistricts().size());
    }

    @Test
    void rejectsANameThatAlreadyExistsIgnoringCaseAndAccents() {
        var registration = service.register(admin, form("NEW", "DISTRITO JUDICIAL DE CUÁUTLA", "Cuautla"));

        assertFalse(registration.isSaved());
        assertEquals("Este nombre ya pertenece a otro distrito judicial.", registration.errors().get("name"));
        assertTrue(catalog.findJudicialDistrict("NEW").isEmpty());
    }

    @Test
    void reportsBothDuplicatesAtOnce() {
        var registration = service.register(admin, form("CUA", "Distrito judicial de Cuautla", "Cuautla"));

        assertEquals(List.of("code", "name"), List.copyOf(registration.errors().keySet()));
    }

    @Test
    void savesNothingWhenARequiredFieldIsMissing() {
        var registration = service.register(admin, form("TEP", "", "Tetecala"));

        assertFalse(registration.isSaved());
        assertTrue(registration.errors().containsKey("name"));
        assertTrue(catalog.findJudicialDistrict("TEP").isEmpty());
    }

    @Test
    void listsTheDistrictsOrderedByName() {
        List<String> names = service.list(admin).stream().map(JudicialDistrict::getName).toList();

        assertEquals(names.stream().sorted().toList(), names);
    }

    @Test
    void refusesEveryOtherProfile() {
        User courtAdmin = new User("a@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN, "Juzgado de Cuautla");
        User clerk = new User("c@utez.edu.mx", "Laura Méndez", UserRole.CLERK, "Juzgado de Cuautla");

        for (User other : List.of(courtAdmin, clerk)) {
            assertThrows(AccessDeniedException.class,
                () -> service.register(other, form("TEP", "Distrito judicial de Tetecala", "Tetecala")));
            assertThrows(AccessDeniedException.class, () -> service.list(other));
            assertThrows(AccessDeniedException.class, () -> service.requireAccess(other));
        }
        assertTrue(catalog.findJudicialDistrict("TEP").isEmpty());
    }
}

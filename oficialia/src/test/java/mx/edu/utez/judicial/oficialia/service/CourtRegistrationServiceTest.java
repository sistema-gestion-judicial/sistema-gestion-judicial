package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtRegistrationForm;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class CourtRegistrationServiceTest {

    private final User admin = new User("o@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null);
    private final InMemoryUserDao users = new InMemoryUserDao(List.<UserAccount>of(), code -> code);
    private final InMemoryCatalogDao catalog = new InMemoryCatalogDao(() -> users);
    private final CourtRegistrationService service = new CourtRegistrationService(catalog, users);

    private static CourtRegistrationForm form() {
        CourtRegistrationForm form = new CourtRegistrationForm();
        form.setCode("20");
        form.setNumber("20");
        form.setName("Juzgado de Tetecala");
        form.setDistrictCode("CVA");
        form.setStreet("Calle Hidalgo 12");
        form.setNeighborhood("Centro");
        form.setPostalCode("62000");
        form.setMunicipality("Tetecala");
        form.setPhone("777 123 4567");
        form.setSubjectMatterCodes(new String[] {"CIV", "FAM"});
        form.setAdminFirstName("Laura");
        form.setAdminLastName("Méndez");
        form.setAdminEmail("laura.mendez@utez.edu.mx");
        form.setAdminPassword("Temporal2026");
        return form;
    }

    @Test
    void savesTheCourtAsActiveWithItsDistrictAddressAndMatters() {
        var registration = service.register(admin, form());

        assertTrue(registration.isSaved());
        Court saved = catalog.findCourt("20").orElseThrow();
        assertTrue(saved.isActive());
        assertEquals(20, saved.getNumber());
        assertEquals("Juzgado de Tetecala", saved.getName());
        assertEquals("CVA", saved.getDistrictCode());
        assertEquals("Distrito judicial de Cuernavaca", saved.getJudicialDistrict());
        assertEquals("Calle Hidalgo 12", saved.getAddress().street());
        assertEquals("Centro", saved.getAddress().neighborhood());
        assertEquals("62000", saved.getAddress().postalCode());
        assertEquals("Tetecala", saved.getAddress().municipality());
        assertEquals("7771234567", saved.getAddress().phone());
        assertEquals(List.of("CIV", "FAM"), saved.getSubjectMatterCodes());
    }

    @Test
    void createsTheAdministratorWhoMustReplaceTheTemporaryPassword() {
        service.register(admin, form());

        UserAccount account = users.findByEmail("laura.mendez@utez.edu.mx").orElseThrow();
        assertTrue(account.active());
        assertEquals(UserRole.COURT_ADMIN, account.user().getRole());
        assertEquals("Laura Méndez", account.user().getFullName());
        assertEquals("Juzgado de Tetecala", account.user().getCourtName());
        assertTrue(account.user().isMustChangePassword());
        assertTrue(PasswordHasher.matches("Temporal2026", account.passwordHash()));
        assertFalse(account.passwordHash().contains("Temporal2026"));
    }

    @Test
    void theAdministratorCanThenRegisterTheirOwnClerks() {
        service.register(admin, form());
        UserAccount account = users.findByEmail("laura.mendez@utez.edu.mx").orElseThrow();
        UserService userService = new UserService(users, catalog);

        var registration = userService.register(account.user(), clerkForm());

        assertTrue(registration.isSaved());
    }

    private static mx.edu.utez.judicial.oficialia.model.NewUserForm clerkForm() {
        var clerk = new mx.edu.utez.judicial.oficialia.model.NewUserForm();
        clerk.setFirstName("Pedro");
        clerk.setLastName("Soto");
        clerk.setEmail("pedro.soto@utez.edu.mx");
        return clerk;
    }

    @Test
    void rejectsAClaveThatAlreadyExists() {
        CourtRegistrationForm form = form();
        form.setCode("01");

        var registration = service.register(admin, form);

        assertFalse(registration.isSaved());
        assertEquals(Map.of("code", "Esta clave ya pertenece a otro juzgado."), registration.errors());
        assertEquals(4, catalog.findCourts().size());
        assertTrue(users.findByEmail("laura.mendez@utez.edu.mx").isEmpty());
    }

    @Test
    void rejectsANumberThatAlreadyExists() {
        CourtRegistrationForm form = form();
        form.setNumber("12");

        var registration = service.register(admin, form);

        assertEquals(Map.of("number", "Este número ya pertenece a otro juzgado."), registration.errors());
        assertEquals(4, catalog.findCourts().size());
    }

    @Test
    void rejectsANameThatAlreadyExistsIgnoringCaseAndAccents() {
        CourtRegistrationForm form = form();
        form.setName("JUZGADO DE CUÁUTLA");

        var registration = service.register(admin, form);

        assertEquals("Este nombre ya pertenece a otro juzgado.", registration.errors().get("name"));
        assertEquals(4, catalog.findCourts().size());
    }

    @Test
    void rejectsAnEmailThatIsAlreadyRegisteredAndLeavesNoCourtBehind() {
        users.insert(new NewUserAccount("Otra", "Persona", "laura.mendez@utez.edu.mx", UserRole.CLERK, "01",
            PasswordHasher.hash("Clave12345"), false, true));

        var registration = service.register(admin, form());

        assertFalse(registration.isSaved());
        assertEquals("Este correo ya está registrado.", registration.errors().get("adminEmail"));
        assertTrue(catalog.findCourt("20").isEmpty());
    }

    @Test
    void doesNotSaveWhenThePostalCodeOrTheMattersAreWrong() {
        CourtRegistrationForm form = form();
        form.setPostalCode("620");
        form.setSubjectMatterCodes(new String[] {});

        var registration = service.register(admin, form);

        assertFalse(registration.isSaved());
        assertTrue(registration.errors().containsKey("postalCode"));
        assertTrue(registration.errors().containsKey("subjectMatterCodes"));
        assertTrue(catalog.findCourt("20").isEmpty());
    }

    @Test
    void rejectsADistrictThatDoesNotExist() {
        CourtRegistrationForm form = form();
        form.setDistrictCode("ZZZ");

        assertEquals("Elija un distrito judicial activo.", service.register(admin, form).errors().get("districtCode"));
    }

    @Test
    void rejectsASubjectMatterThatIsNotInTheCatalog() {
        CourtRegistrationForm form = form();
        form.setSubjectMatterCodes(new String[] {"CIV", "XXX"});

        assertEquals("Elija solo materias activas.", service.register(admin, form).errors().get("subjectMatterCodes"));
    }

    @Test
    void offersOnlyActiveDistrictsAndMatters() {
        catalog.insertJudicialDistrict(
            new mx.edu.utez.judicial.oficialia.model.JudicialDistrict("OFF", "Distrito apagado", "Nada", false));

        assertEquals(4, service.activeDistricts(admin).size());
        assertTrue(service.activeDistricts(admin).stream().noneMatch(district -> district.getCode().equals("OFF")));
        assertEquals(4, service.activeSubjectMatters(admin).size());
    }

    @Test
    void rejectsAnInactiveDistrictEvenIfItsCodeIsSent() {
        catalog.insertJudicialDistrict(
            new mx.edu.utez.judicial.oficialia.model.JudicialDistrict("OFF", "Distrito apagado", "Nada", false));
        CourtRegistrationForm form = form();
        form.setDistrictCode("OFF");

        assertEquals("Elija un distrito judicial activo.", service.register(admin, form).errors().get("districtCode"));
    }

    @Test
    void onlyTheJudicialBodyAdministratorRegistersCourts() {
        User courtAdmin = new User("a@utez.edu.mx", "Ana Paz", UserRole.COURT_ADMIN, "Juzgado de Cuautla");
        User clerk = new User("c@utez.edu.mx", "Carlos Gil", UserRole.CLERK, "Juzgado de Cuautla");

        assertThrows(AccessDeniedException.class, () -> service.register(courtAdmin, form()));
        assertThrows(AccessDeniedException.class, () -> service.register(clerk, form()));
        assertThrows(AccessDeniedException.class, () -> service.register(null, form()));
        assertThrows(AccessDeniedException.class, () -> service.activeDistricts(clerk));
        assertThrows(AccessDeniedException.class, () -> service.requireAccess(courtAdmin));
        assertNotNull(catalog.findCourt("01"));
        assertTrue(catalog.findCourt("20").isEmpty());
    }
}

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
import mx.edu.utez.judicial.oficialia.exception.BusinessRuleException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.NewUserForm;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserEditForm;
import mx.edu.utez.judicial.oficialia.model.UserProfile;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private static final String COURT = "Juzgado de Cuautla";

    private final User courtAdmin = new User("admin@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN, COURT);
    private InMemoryUserDao users;
    private UserService service;

    @BeforeEach
    void freshUsers() {
        users = new InMemoryUserDao(List.of(
            new UserAccount(courtAdmin, PasswordHasher.hash("irrelevant1"), true),
            new UserAccount(new User("otra@utez.edu.mx", "Rosa Vega", UserRole.CLERK, "Juzgado de Jojutla"),
                PasswordHasher.hash("irrelevant1"), true)));
        service = new UserService(users, new InMemoryCatalogDao());
    }

    private static NewUserForm form(String email) {
        NewUserForm form = new NewUserForm();
        form.setFirstName("Ana");
        form.setLastName("Ruiz");
        form.setEmail(email);
        form.setPhone("777 123 4567");
        return form;
    }

    @Test
    void savesAnActiveClerkOfTheAdministratorsCourt() {
        UserService.Registration registration = service.register(courtAdmin, form("ana@utez.edu.mx"));

        assertTrue(registration.isSaved());
        UserAccount saved = users.findByEmail("ana@utez.edu.mx").orElseThrow();
        assertTrue(saved.active());
        assertEquals(UserRole.CLERK, saved.user().getRole());
        assertEquals(COURT, saved.user().getCourtName());
        assertEquals("Ana Ruiz", saved.user().getFullName());
    }

    @Test
    void sendsTheNewUserToChangeTheTemporaryPasswordOnTheFirstSignIn() {
        UserService.Registration registration = service.register(courtAdmin, form("ana@utez.edu.mx"));

        UserAccount saved = users.findByEmail("ana@utez.edu.mx").orElseThrow();
        assertTrue(saved.user().isMustChangePassword());
        assertTrue(PasswordHasher.matches(registration.user().getTemporaryPassword(), saved.passwordHash()));
    }

    @Test
    void neverStoresTheTemporaryPasswordInPlainText() {
        UserService.Registration registration = service.register(courtAdmin, form("ana@utez.edu.mx"));

        assertFalse(users.findByEmail("ana@utez.edu.mx").orElseThrow().passwordHash()
            .contains(registration.user().getTemporaryPassword()));
        assertFalse(registration.user().toString().contains(registration.user().getTemporaryPassword()));
    }

    @Test
    void addsTheUserToTheListingOfTheCourt() {
        service.register(courtAdmin, form("ana@utez.edu.mx"));

        List<UserSummary> listing = service.listForCourt(courtAdmin);

        assertEquals(List.of("Ana Ruiz", "Jorge Salas"), listing.stream().map(UserSummary::getFullName).toList());
        UserSummary ana = listing.get(0);
        assertEquals("7771234567", ana.getPhone());
        assertTrue(ana.isActive());
    }

    @Test
    void doesNotListTheUsersOfOtherCourts() {
        List<String> emails = service.listForCourt(courtAdmin).stream().map(UserSummary::getEmail).toList();

        assertEquals(List.of("admin@utez.edu.mx"), emails);
    }

    @Test
    void rejectsAnEmailThatIsAlreadyRegisteredIgnoringCase() {
        UserService.Registration registration = service.register(courtAdmin, form(" ADMIN@utez.edu.mx "));

        assertFalse(registration.isSaved());
        assertEquals(Map.of("email", "Este correo ya está registrado."), registration.errors());
        assertEquals(1, users.findByCourt(COURT).size());
    }

    @Test
    void savesNothingWhenARequiredFieldIsMissing() {
        NewUserForm incomplete = form("ana@utez.edu.mx");
        incomplete.setLastName("");

        UserService.Registration registration = service.register(courtAdmin, incomplete);

        assertFalse(registration.isSaved());
        assertNotNull(registration.errors().get("lastName"));
        assertTrue(users.findByEmail("ana@utez.edu.mx").isEmpty());
    }

    @Test
    void savesNothingWhenTheEmailIsNotValid() {
        UserService.Registration registration = service.register(courtAdmin, form("not-an-email"));

        assertFalse(registration.isSaved());
        assertEquals("Escriba un correo con formato válido.", registration.errors().get("email"));
    }

    @Test
    void onlyTheCourtAdministratorRegistersAndListsUsers() {
        User clerk = new User("c@utez.edu.mx", "Clara Soto", UserRole.CLERK, COURT);
        User judicialBodyAdmin = new User("o@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null);

        assertThrows(AccessDeniedException.class, () -> service.register(clerk, form("x@utez.edu.mx")));
        assertThrows(AccessDeniedException.class, () -> service.register(judicialBodyAdmin, form("x@utez.edu.mx")));
        assertThrows(AccessDeniedException.class, () -> service.listForCourt(clerk));
        assertTrue(users.findByEmail("x@utez.edu.mx").isEmpty());
    }

    private static UserEditForm editForm(String firstName, String email, String role) {
        UserEditForm form = new UserEditForm();
        form.setFirstName(firstName);
        form.setLastName("Ruiz");
        form.setEmail(email);
        form.setPhone("");
        form.setRole(role);
        return form;
    }

    @Test
    void searchesOnlyTheUsersOfTheCourtByNameOrEmailAndStatus() {
        service.register(courtAdmin, form("ana@utez.edu.mx"));
        service.changeStatus(courtAdmin, "ana@utez.edu.mx", false);

        assertEquals(List.of("ana@utez.edu.mx"), emails(service.search(courtAdmin, "RUIZ", null, 1)));
        assertEquals(List.of("ana@utez.edu.mx"), emails(service.search(courtAdmin, "", false, 1)));
        assertEquals(List.of("admin@utez.edu.mx"), emails(service.search(courtAdmin, null, true, 1)));
        // Rosa belongs to another court, so typing her e-mail finds nothing
        assertTrue(service.search(courtAdmin, "otra@utez.edu.mx", null, 1).getItems().isEmpty());
    }

    @Test
    void correctsAUserOfTheCourtKeepingTheCourt() {
        service.register(courtAdmin, form("ana@utez.edu.mx"));

        UserService.Update update = service.update(courtAdmin, "ana@utez.edu.mx",
            editForm("Ana Lucía", "alucia@utez.edu.mx", "COURT_ADMIN"));

        assertTrue(update.isSaved());
        UserProfile saved = users.findProfile("alucia@utez.edu.mx").orElseThrow();
        assertEquals("Ana Lucía Ruiz", saved.getFullName());
        assertEquals(UserRole.COURT_ADMIN, saved.getRole());
        assertEquals(COURT, saved.getCourtName());
        assertEquals(null, saved.getPhone());
    }

    @Test
    void refusesAnEmailOfAnotherUserOrAnInvalidOne() {
        service.register(courtAdmin, form("ana@utez.edu.mx"));

        assertEquals(UserService.EMAIL_TAKEN, service.update(courtAdmin, "ana@utez.edu.mx",
            editForm("Ana", "OTRA@utez.edu.mx", "CLERK")).errors().get("email"));
        assertTrue(service.update(courtAdmin, "ana@utez.edu.mx", editForm("Ana", "no-es-correo", "CLERK"))
            .errors().containsKey("email"));
        assertEquals("ana@utez.edu.mx", users.findProfile("ana@utez.edu.mx").orElseThrow().getEmail());
    }

    @Test
    void anAdministratorCannotChangeTheirOwnProfileOrDeactivateThemselves() {
        UserService.Update update = service.update(courtAdmin, "admin@utez.edu.mx",
            editForm("Jorge", "admin@utez.edu.mx", "CLERK"));

        assertEquals(UserService.OWN_ROLE, update.errors().get("role"));
        assertThrows(BusinessRuleException.class, () -> service.changeStatus(courtAdmin, "admin@utez.edu.mx", false));
        assertTrue(users.findByEmail("admin@utez.edu.mx").orElseThrow().active());
    }

    @Test
    void usersOfOtherCourtsAreNotFound() {
        assertThrows(NotFoundException.class, () -> service.get(courtAdmin, "otra@utez.edu.mx"));
        assertThrows(NotFoundException.class, () -> service.changeStatus(courtAdmin, "otra@utez.edu.mx", false));
        assertTrue(users.findByEmail("otra@utez.edu.mx").orElseThrow().active());
    }

    @Test
    void deactivatesAndActivatesAUserOfTheCourt() {
        service.register(courtAdmin, form("ana@utez.edu.mx"));

        service.changeStatus(courtAdmin, "ana@utez.edu.mx", false);
        assertFalse(users.findByEmail("ana@utez.edu.mx").orElseThrow().active());

        service.changeStatus(courtAdmin, "ana@utez.edu.mx", true);
        assertTrue(users.findByEmail("ana@utez.edu.mx").orElseThrow().active());
    }

    private static List<String> emails(ResultPage<UserSummary> page) {
        return page.getItems().stream().map(UserSummary::getEmail).toList();
    }
}

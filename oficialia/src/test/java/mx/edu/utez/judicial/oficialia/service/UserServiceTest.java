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
import mx.edu.utez.judicial.oficialia.model.NewUserForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
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
}

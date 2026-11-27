package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.dao.InMemoryUserDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.NewUserForm;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserDetails;
import mx.edu.utez.judicial.oficialia.model.UserEditForm;
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

    // RF 1.6: update a user

    private String idIn(String court, String email) {
        return String.valueOf(users.findByCourt(court).stream()
            .filter(user -> user.getEmail().equals(email)).findFirst().orElseThrow().getId());
    }

    private String registeredClerkId() {
        service.register(courtAdmin, form("ana@utez.edu.mx"));
        return idIn(COURT, "ana@utez.edu.mx");
    }

    private UserEditForm editFormOf(String id) {
        return UserEditForm.of(service.getForEdit(courtAdmin, id));
    }

    @Test
    void opensTheEditFormWithTheCurrentDataOfTheUser() {
        UserDetails ana = service.getForEdit(courtAdmin, registeredClerkId());

        UserEditForm form = UserEditForm.of(ana);
        assertEquals("Ana", form.getFirstName());
        assertEquals("Ruiz", form.getLastName());
        assertEquals("ana@utez.edu.mx", form.getEmail());
        assertEquals("7771234567", form.getPhone());
        assertEquals(UserRole.CLERK, form.getRoleValue());
    }

    @Test
    void correctsTheNameEmailPhoneAndProfileAndTheListingShowsThem() {
        String id = registeredClerkId();
        UserEditForm form = editFormOf(id);
        form.setFirstName("Ana María");
        form.setEmail("Ana.Maria@utez.edu.mx");
        form.setPhone("");
        form.setRole("COURT_ADMIN");

        UserService.Correction correction = service.update(courtAdmin, id, form);

        assertTrue(correction.isSaved());
        assertFalse(correction.self());
        UserSummary listed = service.listForCourt(courtAdmin).stream()
            .filter(user -> String.valueOf(user.getId()).equals(id)).findFirst().orElseThrow();
        assertEquals("Ana María Ruiz", listed.getFullName());
        assertEquals("ana.maria@utez.edu.mx", listed.getEmail());
        assertNull(listed.getPhone());
        assertEquals(UserRole.COURT_ADMIN, listed.getRole());
        assertTrue(users.findByEmail("ana@utez.edu.mx").isEmpty());
    }

    @Test
    void keepsThePasswordTheCourtAndTheStatusOfTheCorrectedUser() {
        String id = registeredClerkId();
        UserAccount before = users.findByEmail("ana@utez.edu.mx").orElseThrow();
        UserEditForm form = editFormOf(id);
        form.setEmail("ana.nueva@utez.edu.mx");

        service.update(courtAdmin, id, form);

        UserAccount after = users.findByEmail("ana.nueva@utez.edu.mx").orElseThrow();
        assertEquals(before.passwordHash(), after.passwordHash());
        assertEquals(before.active(), after.active());
        assertTrue(after.user().isMustChangePassword());
        assertEquals(COURT, after.user().getCourtName());
    }

    @Test
    void rejectsAnEmailThatBelongsToAnotherUserEvenOfAnotherCourt() {
        String id = registeredClerkId();
        UserEditForm form = editFormOf(id);
        form.setEmail(" OTRA@utez.edu.mx ");

        UserService.Correction correction = service.update(courtAdmin, id, form);

        assertFalse(correction.isSaved());
        assertEquals(Map.of("email", "Este correo ya pertenece a otro usuario."), correction.errors());
        assertTrue(users.findByEmail("ana@utez.edu.mx").isPresent());
    }

    @Test
    void keepingTheSameEmailIsNotADuplicate() {
        String id = registeredClerkId();
        UserEditForm form = editFormOf(id);
        form.setEmail("ANA@utez.edu.mx");
        form.setLastName("Ruiz Soto");

        assertTrue(service.update(courtAdmin, id, form).isSaved());
        assertEquals("Ana Ruiz Soto", users.findByEmail("ana@utez.edu.mx").orElseThrow().user().getFullName());
    }

    @Test
    void savesNothingWhenTheEmailIsNotValidOrAFieldIsMissing() {
        String id = registeredClerkId();
        UserEditForm form = editFormOf(id);
        form.setEmail("ana@");
        form.setFirstName("");

        UserService.Correction correction = service.update(courtAdmin, id, form);

        assertFalse(correction.isSaved());
        assertEquals("Escriba un correo con formato válido.", correction.errors().get("email"));
        assertNotNull(correction.errors().get("firstName"));
        assertEquals("Ana Ruiz", users.findByEmail("ana@utez.edu.mx").orElseThrow().user().getFullName());
    }

    @Test
    void theAdministratorCannotChangeTheirOwnProfile() {
        String id = idIn(COURT, "admin@utez.edu.mx");
        UserEditForm form = editFormOf(id);
        form.setRole("CLERK");

        UserService.Correction correction = service.update(courtAdmin, id, form);

        assertEquals(Map.of("role", "No puede cambiar su propio perfil."), correction.errors());
        assertEquals(UserRole.COURT_ADMIN, users.findByEmail("admin@utez.edu.mx").orElseThrow().user().getRole());
    }

    @Test
    void theAdministratorCanCorrectTheirOwnNameAndIsToldToRefreshTheSession() {
        String id = idIn(COURT, "admin@utez.edu.mx");
        UserEditForm form = editFormOf(id);
        form.setFirstName("Jorge Luis");

        UserService.Correction correction = service.update(courtAdmin, id, form);

        assertTrue(correction.isSaved());
        assertTrue(correction.self());
        assertEquals("Jorge Luis Salas", correction.user().getFullName());
    }

    @Test
    void doesNotOpenNorUpdateUsersOfOtherCourts() {
        String otherId = idIn("Juzgado de Jojutla", "otra@utez.edu.mx");
        UserEditForm form = new UserEditForm();
        form.setFirstName("Cambiado");
        form.setLastName("Cambiado");
        form.setEmail("cambiado@utez.edu.mx");
        form.setRole("CLERK");

        assertThrows(NotFoundException.class, () -> service.getForEdit(courtAdmin, otherId));
        assertThrows(NotFoundException.class, () -> service.update(courtAdmin, otherId, form));
        assertEquals("Rosa Vega", users.findByEmail("otra@utez.edu.mx").orElseThrow().user().getFullName());
    }

    @Test
    void reportsAnUnknownOrMalformedIdAsNotFound() {
        assertThrows(NotFoundException.class, () -> service.getForEdit(courtAdmin, "999"));
        assertThrows(NotFoundException.class, () -> service.getForEdit(courtAdmin, "abc"));
        assertThrows(NotFoundException.class, () -> service.getForEdit(courtAdmin, null));
    }

    @Test
    void onlyTheCourtAdministratorUpdatesUsers() {
        String id = registeredClerkId();
        User clerk = new User("c@utez.edu.mx", "Clara Soto", UserRole.CLERK, COURT);
        UserEditForm form = editFormOf(id);
        form.setFirstName("Cambiado");

        assertThrows(AccessDeniedException.class, () -> service.getForEdit(clerk, id));
        assertThrows(AccessDeniedException.class, () -> service.update(clerk, id, form));
        assertEquals("Ana Ruiz", users.findByEmail("ana@utez.edu.mx").orElseThrow().user().getFullName());
    }
}

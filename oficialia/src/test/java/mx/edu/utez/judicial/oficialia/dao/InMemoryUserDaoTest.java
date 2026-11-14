package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.edu.utez.judicial.oficialia.model.NewUserAccount;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import mx.edu.utez.judicial.oficialia.model.UserSummary;
import org.junit.jupiter.api.Test;

class InMemoryUserDaoTest {

    private static final String HASH = "600000:c2FsdA==:aGFzaA==";
    private static final User ANA = new User("ana@utez.edu.mx", "Ana Ruiz", UserRole.CLERK, "Juzgado de Cuautla");

    private final InMemoryUserDao dao = new InMemoryUserDao(List.of(new UserAccount(ANA, "old-hash", true)),
        code -> "Juzgado " + code);

    private static NewUserAccount clerk(String email, String phone) {
        return new NewUserAccount("Ana", "Ruiz", email, phone, UserRole.CLERK, "12", HASH, true, true);
    }

    @Test
    void updatesOnlyThePasswordHashIgnoringEmailCase() {
        dao.updatePasswordHash(" ANA@utez.edu.mx ", "new-hash");

        UserAccount account = dao.findByEmail("ana@utez.edu.mx").orElseThrow();
        assertEquals("new-hash", account.passwordHash());
        assertEquals(ANA, account.user());
        assertTrue(account.active());
    }

    @Test
    void updatingAnUnknownEmailDoesNothing() {
        dao.updatePasswordHash("nobody@utez.edu.mx", "new-hash");

        assertTrue(dao.findByEmail("nobody@utez.edu.mx").isEmpty());
        assertEquals("old-hash", dao.findByEmail("ana@utez.edu.mx").orElseThrow().passwordHash());
    }

    @Test
    void insertsAnAccountWithTheNameOfItsCourtAndTheTemporaryPasswordFlag() {
        dao.insert(clerk("Beto@Utez.edu.mx", "7771234567"));

        UserAccount account = dao.findByEmail("beto@utez.edu.mx").orElseThrow();

        assertEquals("Juzgado 12", account.user().getCourtName());
        assertTrue(account.user().isMustChangePassword());
        assertEquals(HASH, account.passwordHash());
    }

    @Test
    void refusesARepeatedEmailIgnoringCase() {
        assertThrows(DuplicateEmailException.class, () -> dao.insert(clerk(" ANA@utez.edu.mx", null)));
    }

    @Test
    void listsTheUsersOfACourtByName() {
        dao.insert(new NewUserAccount("Zoe", "Mar", "zoe@utez.edu.mx", UserRole.CLERK, "12", HASH, true, true));
        dao.insert(new NewUserAccount("Beto", "Paz", "beto@utez.edu.mx", "7771234567", UserRole.CLERK, "12", HASH,
            true, true));
        dao.insert(new NewUserAccount("Luz", "Paz", "luz@utez.edu.mx", UserRole.CLERK, "07", HASH, true, true));

        List<UserSummary> users = dao.findByCourt("Juzgado 12");

        assertEquals(List.of("Beto Paz", "Zoe Mar"), users.stream().map(UserSummary::getFullName).toList());
        assertEquals("7771234567", users.get(0).getPhone());
        assertTrue(dao.findByCourt("Juzgado 99").isEmpty());
        assertTrue(dao.findByCourt(null).isEmpty());
    }

    @Test
    void replacingThePasswordEndsTheTemporaryPasswordState() {
        dao.insert(clerk("beto@utez.edu.mx", null));

        dao.updatePasswordHash("BETO@utez.edu.mx", "new-hash");

        UserAccount account = dao.findByEmail("beto@utez.edu.mx").orElseThrow();
        assertEquals("new-hash", account.passwordHash());
        assertFalse(account.user().isMustChangePassword());
    }
}

package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserAccount;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class InMemoryUserDaoTest {

    private static final User ANA = new User("ana@utez.edu.mx", "Ana Ruiz", UserRole.CLERK, "Juzgado de Cuautla");

    private final InMemoryUserDao dao = new InMemoryUserDao(List.of(new UserAccount(ANA, "old-hash", true)));

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
}

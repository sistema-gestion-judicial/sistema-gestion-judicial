package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UserEditFormTest {

    private static final UserDetails ANA = new UserDetails(7, "Ana", "Ruiz Soto", "ana@utez.edu.mx", "7771234567",
        UserRole.CLERK, "Juzgado de Cuautla", true);

    @Test
    void opensWithTheCurrentDataOfTheUser() {
        UserEditForm form = UserEditForm.of(ANA);

        assertEquals("Ana", form.getFirstName());
        assertEquals("Ruiz Soto", form.getLastName());
        assertEquals("ana@utez.edu.mx", form.getEmail());
        assertEquals("7771234567", form.getPhone());
        assertEquals(UserRole.CLERK, form.getRoleValue());
        assertTrue(form.validate().isEmpty());
    }

    @Test
    void opensAUserWithoutPhoneWithAnEmptyPhone() {
        UserDetails withoutPhone = new UserDetails(7, "Ana", "Ruiz", "ana@utez.edu.mx", null, UserRole.CLERK,
            "Juzgado de Cuautla", true);

        assertEquals("", UserEditForm.of(withoutPhone).getPhone());
        assertNull(UserEditForm.of(withoutPhone).toUpdate(7).phone());
    }

    @Test
    void hasNoPasswordField() {
        assertFalse(Arrays.stream(UserEditForm.class.getMethods())
            .anyMatch(method -> method.getName().toLowerCase().contains("password")));
    }

    @Test
    void acceptsOnlyTheProfilesOfACourt() {
        UserEditForm form = UserEditForm.of(ANA);

        form.setRole("COURT_ADMIN");
        assertEquals(UserRole.COURT_ADMIN, form.getRoleValue());

        form.setRole("JUDICIAL_BODY_ADMIN");
        assertEquals("Elija el perfil.", form.validate().get("role"));

        form.setRole(null);
        assertEquals("Elija el perfil.", form.validate().get("role"));
    }

    @Test
    void keepsTheRulesOfTheRegistration() {
        UserEditForm form = UserEditForm.of(ANA);
        form.setEmail("not-an-email");
        form.setFirstName(" ");

        assertEquals("Escriba un correo con formato válido.", form.validate().get("email"));
        assertEquals("Escriba el nombre.", form.validate().get("firstName"));
    }

    @Test
    void buildsTheUpdateWithTheNormalizedValues() {
        UserEditForm form = UserEditForm.of(ANA);
        form.setEmail("  Ana.Nueva@UTEZ.edu.mx ");
        form.setPhone("(777) 765-4321");
        form.setRole("COURT_ADMIN");

        UserUpdate update = form.toUpdate(7);

        assertEquals(new UserUpdate(7, "Ana", "Ruiz Soto", "ana.nueva@utez.edu.mx", "7777654321",
            UserRole.COURT_ADMIN), update);
    }
}

package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class NewUserFormTest {

    private static NewUserForm validForm() {
        NewUserForm form = new NewUserForm();
        form.setFirstName("Ana");
        form.setLastName("Ruiz Soto");
        form.setEmail("ana.ruiz@utez.edu.mx");
        return form;
    }

    @Test
    void acceptsTheRequiredFieldsWithoutPhone() {
        assertTrue(validForm().validate().isEmpty());
    }

    @Test
    void marksEveryMissingRequiredField() {
        Map<String, String> errors = new NewUserForm().validate();

        assertEquals(3, errors.size());
        assertTrue(errors.containsKey("firstName"));
        assertTrue(errors.containsKey("lastName"));
        assertTrue(errors.containsKey("email"));
    }

    @Test
    void treatsBlankTextAsMissing() {
        NewUserForm form = validForm();
        form.setFirstName("   ");
        form.setEmail(null);

        Map<String, String> errors = form.validate();

        assertEquals(Map.of("firstName", "Escriba el nombre.", "email", "Escriba el correo electrónico."), errors);
    }

    @Test
    void rejectsAnEmailWithoutAValidFormat() {
        NewUserForm form = validForm();
        form.setEmail("ana.ruiz@utez");

        assertEquals("Escriba un correo con formato válido.", form.validate().get("email"));
    }

    @Test
    void storesTheEmailTrimmedAndInLowerCase() {
        NewUserForm form = validForm();
        form.setEmail("  Ana.Ruiz@UTEZ.edu.mx ");

        assertEquals("ana.ruiz@utez.edu.mx", form.getEmail());
    }

    @Test
    void limitsTheLengthOfNamesAndEmail() {
        NewUserForm form = validForm();
        form.setFirstName("a".repeat(NewUserForm.MAX_NAME_LENGTH + 1));
        form.setEmail("a".repeat(NewUserForm.MAX_EMAIL_LENGTH) + "@utez.edu.mx");

        Map<String, String> errors = form.validate();

        assertTrue(errors.containsKey("firstName"));
        assertTrue(errors.containsKey("email"));
    }

    @Test
    void acceptsAPhoneWithSeparatorsAndKeepsOnlyItsDigits() {
        NewUserForm form = validForm();
        form.setPhone("(777) 123-4567");

        assertTrue(form.validate().isEmpty());
        assertEquals("7771234567", form.getPhoneDigits());
    }

    @Test
    void rejectsAPhoneThatIsNotTenDigits() {
        NewUserForm form = validForm();
        form.setPhone("12345");
        assertTrue(form.validate().containsKey("phone"));

        form.setPhone("77712345ab");
        assertTrue(form.validate().containsKey("phone"));
    }

    @Test
    void hasNoPhoneDigitsWhenNoneWasTyped() {
        assertNull(validForm().getPhoneDigits());
    }
}

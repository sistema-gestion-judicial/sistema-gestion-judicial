package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CourtRegistrationFormTest {

    private static CourtRegistrationForm validForm() {
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
        form.setAdminEmail("Laura.Mendez@utez.edu.mx");
        form.setAdminPassword("Temporal2026");
        return form;
    }

    @Test
    void aCompleteFormHasNoErrors() {
        assertTrue(validForm().validate().isEmpty());
    }

    @Test
    void thePhoneIsTheOnlyOptionalField() {
        CourtRegistrationForm form = validForm();
        form.setPhone("");

        assertTrue(form.validate().isEmpty());
        assertNull(form.getPhoneDigits());
    }

    @Test
    void everyRequiredFieldIsMarkedWhenTheFormIsEmpty() {
        var errors = new CourtRegistrationForm().validate();

        assertEquals(List.of("code", "number", "name", "districtCode", "street", "neighborhood", "postalCode",
            "municipality", "subjectMatterCodes", "adminFirstName", "adminLastName", "adminEmail", "adminPassword"),
            List.copyOf(errors.keySet()));
    }

    @Test
    void theCodeHasExactlyTwoDigits() {
        for (String code : new String[] {"1", "123", "A1", "1 2"}) {
            CourtRegistrationForm form = validForm();
            form.setCode(code);
            assertTrue(form.validate().containsKey("code"), code);
        }
        CourtRegistrationForm form = validForm();
        form.setCode("08");
        assertTrue(form.validate().isEmpty());
    }

    @Test
    void theNumberIsAWholeNumberFromOneTo255() {
        for (String number : new String[] {"0", "256", "-1", "1.5", "abc", "0007x"}) {
            CourtRegistrationForm form = validForm();
            form.setNumber(number);
            assertTrue(form.validate().containsKey("number"), number);
        }
        CourtRegistrationForm form = validForm();
        form.setNumber("255");
        assertTrue(form.validate().isEmpty());
        assertEquals(255, form.getNumberValue());
    }

    @Test
    void thePostalCodeHasFiveDigits() {
        for (String postalCode : new String[] {"6200", "620000", "6200a"}) {
            CourtRegistrationForm form = validForm();
            form.setPostalCode(postalCode);
            assertEquals("El código postal debe tener 5 dígitos.", form.validate().get("postalCode"), postalCode);
        }
    }

    @Test
    void atLeastOneSubjectMatterMustBeChosen() {
        CourtRegistrationForm form = validForm();
        form.setSubjectMatterCodes(new String[] {});
        assertEquals("Elija al menos una materia.", form.validate().get("subjectMatterCodes"));

        form.setSubjectMatterCodes(null);
        assertTrue(form.validate().containsKey("subjectMatterCodes"));

        form.setSubjectMatterCodes(new String[] {" ", ""});
        assertTrue(form.validate().containsKey("subjectMatterCodes"));
    }

    @Test
    void repeatedSubjectMatterCodesCountOnce() {
        CourtRegistrationForm form = validForm();
        form.setSubjectMatterCodes(new String[] {"CIV", " CIV ", "PEN"});

        assertEquals(List.of("CIV", "PEN"), form.getSubjectMatterCodes());
    }

    @Test
    void thePhoneNeedsTenDigitsWhenItIsTyped() {
        CourtRegistrationForm form = validForm();
        form.setPhone("12345");
        assertEquals("Escriba un teléfono de 10 dígitos.", form.validate().get("phone"));

        form.setPhone("777abc4567");
        assertTrue(form.validate().containsKey("phone"));

        form.setPhone("(777) 123-4567");
        assertTrue(form.validate().isEmpty());
        assertEquals("7771234567", form.getPhoneDigits());
    }

    @Test
    void theAdministratorEmailMustBeValidAndIsStoredNormalized() {
        CourtRegistrationForm form = validForm();
        form.setAdminEmail("no-es-un-correo");
        assertEquals("Escriba un correo con formato válido.", form.validate().get("adminEmail"));

        form.setAdminEmail("  Laura.Mendez@UTEZ.edu.mx ");
        assertTrue(form.validate().isEmpty());
        assertEquals("laura.mendez@utez.edu.mx", form.getAdminEmail());
    }

    @Test
    void theTemporaryPasswordNeedsEightCharactersWithLettersAndNumbers() {
        for (String password : new String[] {"corta1A", "sololetrasaqui", "123456789012"}) {
            CourtRegistrationForm form = validForm();
            form.setAdminPassword(password);
            assertTrue(form.validate().containsKey("adminPassword"), password);
        }
    }

    @Test
    void textFieldsOverTheirLimitAreRefused() {
        CourtRegistrationForm form = validForm();
        form.setName("J".repeat(CourtRegistrationForm.MAX_NAME_LENGTH + 1));
        form.setStreet("C".repeat(CourtRegistrationForm.MAX_STREET_LENGTH + 1));

        var errors = form.validate();
        assertEquals("No puede pasar de 120 caracteres.", errors.get("name"));
        assertEquals("No puede pasar de 150 caracteres.", errors.get("street"));
    }
}

package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CourtFormTest {

    private static CourtForm validForm() {
        CourtForm form = new CourtForm();
        form.setName("Juzgado Primero Civil de Cuautla");
        form.setDistrictCode("CUA");
        form.setStreet("Av. Reforma 12");
        form.setNeighborhood("Centro");
        form.setPostalCode("62740");
        form.setMunicipality("Cuautla");
        form.setSubjectMatterCodes(new String[] {"CIV", "FAM"});
        return form;
    }

    @Test
    void acceptsACompleteFormWithoutPhone() {
        assertTrue(validForm().validate().isEmpty());
    }

    @Test
    void marksEveryMissingRequiredField() {
        Map<String, String> errors = new CourtForm().validate();

        assertEquals(List.of("name", "districtCode", "street", "neighborhood", "postalCode", "municipality",
            "subjectMatterCodes"), List.copyOf(errors.keySet()));
    }

    @Test
    void requiresAtLeastOneSubjectMatter() {
        CourtForm form = validForm();
        form.setSubjectMatterCodes(new String[0]);
        assertEquals("Elija al menos una materia que atiende el juzgado.", form.validate().get("subjectMatterCodes"));

        form.setSubjectMatterCodes(null);
        assertTrue(form.validate().containsKey("subjectMatterCodes"));
    }

    @Test
    void ignoresBlankAndRepeatedMatterCodes() {
        CourtForm form = validForm();
        form.setSubjectMatterCodes(new String[] {"CIV", " ", "CIV", " FAM "});

        assertEquals(List.of("CIV", "FAM"), form.getSubjectMatterCodes());
    }

    @Test
    void requiresAFiveDigitPostalCode() {
        CourtForm form = validForm();
        form.setPostalCode("6274");
        assertTrue(form.validate().containsKey("postalCode"));
        form.setPostalCode("627AB");
        assertTrue(form.validate().containsKey("postalCode"));
    }

    @Test
    void acceptsAPhoneWithSeparatorsAndKeepsOnlyItsDigits() {
        CourtForm form = validForm();
        form.setPhone("(735) 352-1234");

        assertTrue(form.validate().isEmpty());
        assertEquals("7353521234", form.getPhoneDigits());
    }

    @Test
    void rejectsAPhoneThatIsNotTenDigitsAndHasNoDigitsWhenEmpty() {
        CourtForm form = validForm();
        form.setPhone("12345");
        assertTrue(form.validate().containsKey("phone"));

        form.setPhone("");
        assertNull(form.getPhoneDigits());
    }

    @Test
    void limitsTheLengthOfTheTextFields() {
        CourtForm form = validForm();
        form.setName("a".repeat(CourtForm.MAX_NAME_LENGTH + 1));
        form.setStreet("b".repeat(CourtForm.MAX_STREET_LENGTH + 1));
        form.setNeighborhood("c".repeat(CourtForm.MAX_NEIGHBORHOOD_LENGTH + 1));
        form.setMunicipality("d".repeat(CourtForm.MAX_MUNICIPALITY_LENGTH + 1));

        assertEquals(List.of("name", "street", "neighborhood", "municipality"),
            List.copyOf(form.validate().keySet()));
    }

    @Test
    void startsWithTheCurrentValuesOfACourt() {
        Court court = new Court("12", 12, "Juzgado de Cuautla", "CUA", "Distrito judicial de Cuautla",
            new CourtAddress("Av. Reforma 12", "Centro", "62740", "Cuautla", "7353521234"), List.of("CIV", "PEN"),
            true);

        CourtForm form = CourtForm.of(court);

        assertEquals("Juzgado de Cuautla", form.getName());
        assertEquals("CUA", form.getDistrictCode());
        assertEquals("Av. Reforma 12", form.getStreet());
        assertEquals("62740", form.getPostalCode());
        assertEquals("7353521234", form.getPhone());
        assertEquals(List.of("CIV", "PEN"), form.getSubjectMatterCodes());
    }

    @Test
    void startsEmptyWhenTheCourtHasNoAddressYet() {
        Court legacy = new Court("12", 12, "Juzgado de Cuautla", "CUA", "Distrito judicial de Cuautla",
            CourtAddress.EMPTY, List.of("CIV"), true);

        assertEquals("", CourtForm.of(legacy).getStreet());
        assertTrue(CourtForm.of(legacy).validate().containsKey("street"));
    }
}

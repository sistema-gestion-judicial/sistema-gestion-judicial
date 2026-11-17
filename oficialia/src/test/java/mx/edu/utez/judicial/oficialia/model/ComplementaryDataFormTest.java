package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ComplementaryDataFormTest {

    private static ComplementaryDataForm validForm() {
        ComplementaryDataForm form = new ComplementaryDataForm();
        form.getPlaintiff().setStreet("Calle Hidalgo 12");
        form.getPlaintiff().setNeighborhood("Centro");
        form.getPlaintiff().setPostalCode("62740");
        form.getPlaintiff().setMunicipality("Cuautla");
        form.getPlaintiff().setPhone("735 112 4598");
        form.getPlaintiff().setEmail("Ana.Torres@Ejemplo.mx");
        form.setLegalRepresentative("Lic. Marcos Rivera Peña");
        form.setPresentationType("INDIVIDUAL");
        form.addAnnex(new Annex("Demanda.pdf", "application/pdf", new byte[100]));
        return form;
    }

    @Test
    void aCompleteFormHasNoErrors() {
        ComplementaryDataForm form = validForm();

        assertTrue(form.validate(true, true).isEmpty());
        assertEquals("ana.torres@ejemplo.mx", form.getPlaintiff().getEmail());
        assertEquals("7351124598", form.getPlaintiff().getPhoneDigits());
        assertEquals(PresentationType.INDIVIDUAL, form.getPresentationTypeValue());
    }

    @Test
    void everyRequiredFieldIsMarkedWhenTheFormIsEmpty() {
        var errors = new ComplementaryDataForm().validate(true, true);

        assertEquals(List.of("plaintiffStreet", "plaintiffNeighborhood", "plaintiffPostalCode",
            "plaintiffMunicipality", "plaintiffPhone", "plaintiffEmail", "legalRepresentative", "presentationType",
            "annexes"), List.copyOf(errors.keySet()));
    }

    @Test
    void anAnonymousPlaintiffNeedsNoAddressNorContact() {
        ComplementaryDataForm form = new ComplementaryDataForm();
        form.setLegalRepresentative("Colectivo Vecinos Unidos");
        form.setPresentationType("COLLECTIVE");
        form.addAnnex(new Annex("Demanda.pdf", "application/pdf", new byte[100]));

        assertTrue(form.validate(false, false).isEmpty());
        assertNull(form.plaintiffAddress());
    }

    @Test
    void theDefendantDataIsOptionalButMustBeValidWhenTyped() {
        ComplementaryDataForm form = validForm();
        assertTrue(form.validate(true, true).isEmpty());
        assertNull(form.defendantAddress());

        form.getDefendant().setStreet("Av. Morelos 5");
        form.getDefendant().setPhone("12345");
        form.getDefendant().setEmail("no-es-correo");

        var errors = form.validate(true, true);
        assertEquals(List.of("defendantNeighborhood", "defendantPostalCode", "defendantMunicipality",
            "defendantPhone", "defendantEmail"), List.copyOf(errors.keySet()));
    }

    @Test
    void theDefendantFieldsAreIgnoredWhenTheDefendantIsUnknown() {
        ComplementaryDataForm form = validForm();
        form.getDefendant().setPhone("12345");

        assertTrue(form.validate(true, false).isEmpty());
    }

    @Test
    void thePostalCodeHasFiveDigitsAndThePhoneTen() {
        ComplementaryDataForm form = validForm();
        form.getPlaintiff().setPostalCode("6274");
        form.getPlaintiff().setPhone("735 112");

        var errors = form.validate(true, true);
        assertEquals("El código postal debe tener 5 dígitos.", errors.get("plaintiffPostalCode"));
        assertEquals("Escriba un teléfono de 10 dígitos.", errors.get("plaintiffPhone"));
    }

    @Test
    void onlyIndividualOrCollectiveIsAccepted() {
        ComplementaryDataForm form = validForm();
        form.setPresentationType("OTRO");

        assertTrue(form.validate(true, true).containsKey("presentationType"));
        assertNull(form.getPresentationTypeValue());
    }

    @Test
    void theObservationsAreOptionalUpToTheirLimit() {
        ComplementaryDataForm form = validForm();
        assertNull(form.observationsOrNull());

        form.setObservations("o".repeat(ComplementaryDataForm.MAX_OBSERVATIONS_LENGTH + 1));
        assertEquals("No puede pasar de 500 caracteres.", form.validate(true, true).get("observations"));
    }

    @Test
    void refusesAnAnnexOverTenMegabytesAndAllOfThemOverForty() {
        ComplementaryDataForm big = validForm();
        big.addAnnex(new Annex("Video.mp4", "video/mp4", new byte[10 * 1024 * 1024 + 1]));
        assertEquals("El anexo «Video.mp4» pasa de 10 MB.", big.validate(true, true).get("annexes"));

        ComplementaryDataForm many = validForm();
        for (int i = 0; i < 5; i++) {
            many.addAnnex(new Annex("Foto" + i + ".jpg", "image/jpeg", new byte[9 * 1024 * 1024]));
        }
        assertEquals("Los anexos juntos pasan de 40 MB.", many.validate(true, true).get("annexes"));
    }

    @Test
    void keepsTheAddressTyped() {
        assertEquals("Calle Hidalgo 12, Centro, C.P. 62740, Cuautla", validForm().plaintiffAddress().getFullText());
    }
}

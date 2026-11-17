package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ComplementaryDataFormTest {

    private static ComplementaryDataForm validForm() {
        ComplementaryDataForm form = new ComplementaryDataForm();
        form.setLegalRepresentative("Lic. Marcos Rivera Peña");
        form.setPresentationType("INDIVIDUAL");
        return form;
    }

    @Test
    void aCompleteFormHasNoErrors() {
        ComplementaryDataForm form = validForm();

        assertTrue(form.validate(true, true).isEmpty());
        assertEquals(PresentationType.INDIVIDUAL, form.getPresentationTypeValue());
    }

    @Test
    void everyRequiredFieldIsMarkedWhenTheFormIsEmpty() {
        var errors = new ComplementaryDataForm().validate(true, true);

        assertEquals(List.of("legalRepresentative", "presentationType"), List.copyOf(errors.keySet()));
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
}

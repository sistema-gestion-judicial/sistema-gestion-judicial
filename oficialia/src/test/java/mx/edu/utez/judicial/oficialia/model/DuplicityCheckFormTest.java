package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class DuplicityCheckFormTest {

    @Test
    void requiresAtLeastOneParty() {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter("Familiar");
        form.setPlaintiffAnonymous(true);
        form.setDefendantUnknown(true);

        Map<String, String> errors = form.validate();

        assertTrue(errors.containsKey("parties"));
    }

    @Test
    void acceptsOnlyTheDefendantWhenThePlaintiffIsAnonymous() {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setMatter("Civil");
        form.setPlaintiffAnonymous(true);
        form.setDefendantName("Minera del Sur");
        form.setDefendantId("MSU010203KL9");

        assertTrue(form.validate().isEmpty());
    }

    @Test
    void reportsMissingMatterAndInvalidId() {
        DuplicityCheckForm form = new DuplicityCheckForm();
        form.setPlaintiffName("Ana Torres");
        form.setPlaintiffId("ABC");
        form.setDefendantUnknown(true);

        Map<String, String> errors = form.validate();

        assertEquals(2, errors.size());
        assertTrue(errors.containsKey("matter"));
        assertTrue(errors.containsKey("plaintiffId"));
    }
}

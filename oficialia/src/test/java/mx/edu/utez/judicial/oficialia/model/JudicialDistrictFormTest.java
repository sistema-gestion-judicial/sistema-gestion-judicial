package mx.edu.utez.judicial.oficialia.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class JudicialDistrictFormTest {

    private static JudicialDistrictForm form(String code, String name, String seat) {
        JudicialDistrictForm form = new JudicialDistrictForm();
        form.setCode(code);
        form.setName(name);
        form.setSeatMunicipality(seat);
        return form;
    }

    @Test
    void acceptsACompleteForm() {
        assertTrue(form("TEP", "Distrito judicial de Tetecala", "Tetecala").validate().isEmpty());
    }

    @Test
    void marksEveryMissingField() {
        Map<String, String> errors = new JudicialDistrictForm().validate();

        assertEquals(3, errors.size());
        assertTrue(errors.containsKey("code"));
        assertTrue(errors.containsKey("name"));
        assertTrue(errors.containsKey("seatMunicipality"));
    }

    @Test
    void treatsBlankTextAsMissing() {
        assertEquals(3, form("  ", "   ", null).validate().size());
    }

    @Test
    void keepsTheCodeInUpperCaseWithoutSurroundingSpaces() {
        assertEquals("TEP", form(" tep ", "Distrito", "Tetecala").getCode());
    }

    @Test
    void rejectsCodesWithSpacesSymbolsOrTheWrongLength() {
        assertTrue(form("T", "Distrito", "Tetecala").validate().containsKey("code"));
        assertTrue(form("TE P", "Distrito", "Tetecala").validate().containsKey("code"));
        assertTrue(form("TE-P", "Distrito", "Tetecala").validate().containsKey("code"));
        assertTrue(form("ÑAND", "Distrito", "Tetecala").validate().containsKey("code"));
        assertTrue(form("A".repeat(JudicialDistrictForm.MAX_CODE_LENGTH + 1), "Distrito", "Tetecala").validate()
            .containsKey("code"));
        assertTrue(form("A".repeat(JudicialDistrictForm.MAX_CODE_LENGTH), "Distrito", "Tetecala").validate().isEmpty());
    }

    @Test
    void collapsesInnerSpacesOfTheName() {
        assertEquals("Distrito judicial de Cuautla", form("CUA", " Distrito   judicial de  Cuautla ", "Cuautla").getName());
    }

    @Test
    void limitsTheLengthOfNameAndSeat() {
        Map<String, String> errors = form("TEP", "a".repeat(JudicialDistrictForm.MAX_NAME_LENGTH + 1),
            "b".repeat(JudicialDistrictForm.MAX_SEAT_LENGTH + 1)).validate();

        assertTrue(errors.containsKey("name"));
        assertTrue(errors.containsKey("seatMunicipality"));
    }
}

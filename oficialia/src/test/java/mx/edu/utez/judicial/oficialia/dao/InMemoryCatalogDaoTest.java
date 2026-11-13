package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.edu.utez.judicial.oficialia.model.JudicialDistrict;
import org.junit.jupiter.api.Test;

class InMemoryCatalogDaoTest {

    private final InMemoryCatalogDao dao = new InMemoryCatalogDao();

    @Test
    void startsWithTheFourDistrictsOfTheSampleCatalog() {
        assertEquals(List.of("CUA", "CVA", "JOJ", "YAU"),
            dao.findJudicialDistricts().stream().map(JudicialDistrict::getCode).toList());
    }

    @Test
    void insertsADistrictThatCanBeFoundByItsCode() {
        dao.insertJudicialDistrict(new JudicialDistrict("TEP", "Distrito judicial de Tetecala", "Tetecala", true));

        assertEquals("Tetecala", dao.findJudicialDistrict("TEP").orElseThrow().getSeatMunicipality());
        assertEquals(5, dao.findJudicialDistricts().size());
    }

    @Test
    void refusesARepeatedCodeOrName() {
        assertThrows(DuplicateCodeException.class, () -> dao.insertJudicialDistrict(
            new JudicialDistrict("cva", "Otro nombre", "Cuernavaca", true)));
        assertThrows(DuplicateNameException.class, () -> dao.insertJudicialDistrict(
            new JudicialDistrict("NEW", "Distrito judicial de Cuernavaca", "Cuernavaca", true)));
        assertTrue(dao.findJudicialDistrict("NEW").isEmpty());
    }
}

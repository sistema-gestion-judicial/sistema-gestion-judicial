package mx.edu.utez.judicial.oficialia.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.CourtChanges;
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

    @Test
    void updatesTheNameAndTheSeatOfADistrictAndRenamesItsCourts() {
        dao.updateJudicialDistrict("CUA", "Distrito judicial Oriente", "Cuautla de Morelos");

        JudicialDistrict updated = dao.findJudicialDistrict("CUA").orElseThrow();
        assertEquals("Distrito judicial Oriente", updated.getName());
        assertEquals("Cuautla de Morelos", updated.getSeatMunicipality());
        assertTrue(updated.isActive());
        assertEquals("Distrito judicial Oriente", dao.findCourt("12").orElseThrow().getJudicialDistrict());
        assertEquals("Distrito judicial de Cuernavaca", dao.findCourt("01").orElseThrow().getJudicialDistrict());
    }

    @Test
    void refusesToRenameADistrictWithTheNameOfAnotherButAllowsItsOwn() {
        assertThrows(DuplicateNameException.class,
            () -> dao.updateJudicialDistrict("CUA", "distrito judicial de cuernavaca", "Cuautla"));

        dao.updateJudicialDistrict("CUA", "DISTRITO JUDICIAL DE CUAUTLA", "Cuautla");
        assertEquals("DISTRITO JUDICIAL DE CUAUTLA", dao.findJudicialDistrict("CUA").orElseThrow().getName());
    }

    @Test
    void updatesACourtAndKeepsItsCodeNumberAndStatus() {
        dao.updateCourt("12", new CourtChanges("Juzgado Primero de Cuautla", "CVA",
            new CourtAddress("Av. Reforma 12", "Centro", "62740", "Cuautla", "7353521234"), List.of("CIV")));

        Court updated = dao.findCourt("12").orElseThrow();
        assertEquals("Juzgado Primero de Cuautla", updated.getName());
        assertEquals("CVA", updated.getDistrictCode());
        assertEquals("Distrito judicial de Cuernavaca", updated.getJudicialDistrict());
        assertEquals("62740", updated.getAddress().postalCode());
        assertEquals(List.of("CIV"), updated.getSubjectMatterCodes());
        assertEquals(12, updated.getNumber());
        assertTrue(updated.isActive());
    }

    @Test
    void refusesACourtNameOfAnotherCourtButAllowsItsOwn() {
        CourtChanges clash = new CourtChanges("juzgado de cuernavaca", "CUA", CourtAddress.EMPTY, List.of("CIV"));
        assertThrows(DuplicateNameException.class, () -> dao.updateCourt("12", clash));

        dao.updateCourt("12", new CourtChanges("JUZGADO DE CUAUTLA", "CUA", CourtAddress.EMPTY, List.of("CIV")));
        assertEquals("JUZGADO DE CUAUTLA", dao.findCourt("12").orElseThrow().getName());
    }

    @Test
    void updatingAnUnknownDistrictDoesNothing() {
        dao.updateJudicialDistrict("ZZZ", "Nombre", "Sede");

        assertTrue(dao.findJudicialDistrict("ZZZ").isEmpty());
        assertEquals(4, dao.findJudicialDistricts().size());
    }
}

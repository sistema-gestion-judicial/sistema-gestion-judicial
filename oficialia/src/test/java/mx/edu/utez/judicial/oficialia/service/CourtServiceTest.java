package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.model.Court;
import mx.edu.utez.judicial.oficialia.model.CourtAddress;
import mx.edu.utez.judicial.oficialia.model.CourtRow;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class CourtServiceTest {

    private final User clerk = new User("l@utez.edu.mx", "Laura Méndez", UserRole.CLERK, "Juzgado de Cuautla");
    private final User courtAdmin = new User("a@utez.edu.mx", "Jorge Salas", UserRole.COURT_ADMIN, "Juzgado de Cuautla");
    private final User bodyAdmin = new User("o@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null);

    private final List<Court> courts = new ArrayList<>(List.of(
        new Court("12", 12, "Juzgado de Cuautla", "CUA", "Distrito judicial de Cuautla",
            new CourtAddress("Av. Reforma 10", "Centro", "62740", "Cuautla", "7353520000"),
            List.of("CIV", "FAM"), true),
        new Court("01", 1, "Juzgado de Cuernavaca", "CVA", "Distrito judicial de Cuernavaca",
            new CourtAddress(null, null, null, "Cuernavaca", null), List.of("PEN"), true),
        new Court("09", 9, "Juzgado de Tetecala", "CVA", "Distrito judicial de Cuernavaca",
            CourtAddress.EMPTY, List.of("CIV"), false)));

    private final CourtService service = new CourtService(new InMemoryCatalogDao() {
        @Override
        public synchronized List<Court> findCourts() {
            return courts;
        }
    });

    private static List<String> codes(ResultPage<CourtRow> page) {
        return page.getItems().stream().map(CourtRow::getCode).toList();
    }

    @Test
    void showsTheCourtsOrderedByNameWithTheirAddressAndSubjectMatters() {
        ResultPage<CourtRow> page = service.search(bodyAdmin, "", null, null, null, 1);

        assertEquals(List.of("12", "01", "09"), codes(page));
        CourtRow cuautla = page.getItems().get(0);
        assertEquals("Av. Reforma 10, Col. Centro, C.P. 62740, Cuautla", cuautla.getFullAddress());
        assertEquals("7353520000", cuautla.getPhone());
        assertEquals("Civil, Familiar", cuautla.getSubjectMatters());
        assertEquals("Cuernavaca", page.getItems().get(1).getFullAddress());
        assertEquals("", page.getItems().get(2).getFullAddress());
    }

    @Test
    void anOficialDePartesNeverSeesInactiveCourtsEvenWhenAskingForThem() {
        assertEquals(List.of("12", "01"), codes(service.search(clerk, "", null, null, null, 1)));
        assertTrue(service.search(clerk, "", null, null, false, 1).getItems().stream().allMatch(CourtRow::isActive));
        assertFalse(CourtService.canSeeInactive(clerk));
    }

    @Test
    void theAdministratorsCanFilterByStatus() {
        assertEquals(List.of("09"), codes(service.search(courtAdmin, "", null, null, false, 1)));
        assertEquals(List.of("12", "01"), codes(service.search(bodyAdmin, "", null, null, true, 1)));
        assertTrue(CourtService.canSeeInactive(courtAdmin));
    }

    @Test
    void findsTheTextInTheCodeTheNumberTheNameTheDistrictOrTheMunicipality() {
        assertEquals(List.of("12"), codes(service.search(bodyAdmin, "CUÁUTLA", null, null, null, 1)));
        assertEquals(List.of("09"), codes(service.search(bodyAdmin, "tetecala", null, null, null, 1)));
        assertEquals(List.of("01", "09"), codes(service.search(bodyAdmin, "distrito judicial de cuernavaca", null,
            null, null, 1)));
        assertEquals(List.of("09"), codes(service.search(bodyAdmin, "9", null, null, null, 1)));
    }

    @Test
    void filtersByDistrictAndBySubjectMatter() {
        assertEquals(List.of("01", "09"), codes(service.search(bodyAdmin, "", "CVA", null, null, 1)));
        assertEquals(List.of("12", "09"), codes(service.search(bodyAdmin, "", null, "CIV", null, 1)));
        assertEquals(List.of("09"), codes(service.search(bodyAdmin, "", "CVA", "CIV", null, 1)));
    }

    @Test
    void returnsAnEmptyPageWhenNothingMatches() {
        ResultPage<CourtRow> page = service.search(clerk, "Tlaxcala", null, null, null, 1);

        assertTrue(page.getItems().isEmpty());
        assertEquals(0, page.getTotalItems());
    }

    @Test
    void splitsTheCatalogInPagesOfTen() {
        for (int number = 20; number < 32; number++) {
            courts.add(new Court(String.valueOf(number), number, "Juzgado de prueba " + number, "CVA",
                "Distrito judicial de Cuernavaca", CourtAddress.EMPTY, List.of("CIV"), true));
        }

        ResultPage<CourtRow> second = service.search(bodyAdmin, "", null, null, null, 2);

        assertEquals(15, second.getTotalItems());
        assertEquals(5, second.getItems().size());
        assertTrue(second.isLast());
    }

    @Test
    void refusesWhenNobodyIsSignedIn() {
        assertThrows(AccessDeniedException.class, () -> service.search(null, "", null, null, null, 1));
    }
}

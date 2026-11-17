package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import org.junit.jupiter.api.Test;

class DemandRequestRegistrationServiceTest {

    private final InMemoryCatalogDao catalog = new InMemoryCatalogDao();
    private final DemandRequestRegistrationService service = new DemandRequestRegistrationService(catalog);

    @Test
    void offersTheActiveSubjectMattersOrderedByName() {
        assertEquals(List.of("Civil", "Familiar", "Mercantil", "Penal"), service.activeMatterNames());
    }

    @Test
    void leavesOutAnInactiveSubjectMatter() {
        InMemoryCatalogDao withInactive = new InMemoryCatalogDao() {
            @Override
            public List<SubjectMatter> findSubjectMatters() {
                return List.of(new SubjectMatter(1, "CIV", "Civil", null, true),
                    new SubjectMatter(2, "AGR", "Agraria", null, false));
            }
        };

        assertEquals(List.of("Civil"), new DemandRequestRegistrationService(withInactive).activeMatterNames());
    }
}

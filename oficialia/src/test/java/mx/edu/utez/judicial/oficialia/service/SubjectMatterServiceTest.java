package mx.edu.utez.judicial.oficialia.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import mx.edu.utez.judicial.oficialia.dao.InMemoryCatalogDao;
import mx.edu.utez.judicial.oficialia.exception.AccessDeniedException;
import mx.edu.utez.judicial.oficialia.exception.NotFoundException;
import mx.edu.utez.judicial.oficialia.model.ResultPage;
import mx.edu.utez.judicial.oficialia.model.SubjectMatter;
import mx.edu.utez.judicial.oficialia.model.SubjectMatterForm;
import mx.edu.utez.judicial.oficialia.model.SubjectMatterRow;
import mx.edu.utez.judicial.oficialia.model.User;
import mx.edu.utez.judicial.oficialia.model.UserRole;
import org.junit.jupiter.api.Test;

class SubjectMatterServiceTest {

    private final User admin = new User("o@utez.edu.mx", "Elena Ríos", UserRole.JUDICIAL_BODY_ADMIN, null);
    private final User courtAdmin = new User("a@utez.edu.mx", "Luis Mora", UserRole.COURT_ADMIN, "Juzgado de Cuautla");
    private final InMemoryCatalogDao catalog = new InMemoryCatalogDao();
    private final SubjectMatterService service = new SubjectMatterService(catalog);

    private static SubjectMatterForm form(String code, String name, String description) {
        SubjectMatterForm form = new SubjectMatterForm();
        form.setCode(code);
        form.setName(name);
        form.setDescription(description);
        return form;
    }

    private static List<String> codes(ResultPage<SubjectMatterRow> page) {
        return page.getItems().stream().map(SubjectMatterRow::getCode).toList();
    }

    @Test
    void listsEveryMatterWithTheNumberOfCourtsThatHandleIt() {
        service.register(admin, form("AMP", "Amparo", null));

        ResultPage<SubjectMatterRow> page = service.search(admin, "", null, 1);

        assertEquals(List.of("AMP", "CIV", "FAM", "MER", "PEN"), codes(page));
        assertEquals(0, page.getItems().get(0).getCourtCount());
        assertEquals(4, page.getItems().get(1).getCourtCount());
    }

    @Test
    void filtersByTextIgnoringAccentsAndByStatus() {
        service.register(admin, form("AMP", "Amparo", "Juicios de garantías"));
        service.changeStatus(admin, "PEN", false);

        assertEquals(List.of("AMP"), codes(service.search(admin, "GARANTIAS", null, 1)));
        assertEquals(List.of("PEN"), codes(service.search(admin, null, false, 1)));
        assertTrue(service.search(admin, "Laboral", null, 1).getItems().isEmpty());
    }

    @Test
    void registersTheMatterAsActiveWithItsCodeInUpperCase() {
        SubjectMatterService.Saving saving = service.register(admin, form(" amp ", "  Amparo  ", ""));

        assertTrue(saving.isSaved());
        SubjectMatter saved = catalog.findSubjectMatter("AMP").orElseThrow();
        assertEquals("Amparo", saved.getName());
        assertTrue(saved.isActive());
        assertEquals(null, saved.getDescription());
    }

    @Test
    void refusesARepeatedCodeOrNameAndSavesNothing() {
        SubjectMatterService.Saving saving = service.register(admin, form("civ", "FAMILIAR", null));

        assertFalse(saving.isSaved());
        assertEquals(SubjectMatterService.CODE_TAKEN, saving.errors().get("code"));
        assertEquals(SubjectMatterService.NAME_TAKEN, saving.errors().get("name"));
        assertEquals(4, catalog.findSubjectMatters().size());
    }

    @Test
    void validatesTheFields() {
        SubjectMatterService.Saving saving = service.register(admin, form("A B", "", "x".repeat(256)));

        assertEquals(List.of("code", "name", "description"), List.copyOf(saving.errors().keySet()));
    }

    @Test
    void updatesTheNameAndDescriptionButNeverTheCode() {
        SubjectMatterService.Saving saving = service.update(admin, "MER", form("XXX", "Mercantil y comercio", "Actos de comercio"));

        assertTrue(saving.isSaved());
        SubjectMatter updated = catalog.findSubjectMatter("MER").orElseThrow();
        assertEquals("Mercantil y comercio", updated.getName());
        assertEquals("Actos de comercio", updated.getDescription());
        assertTrue(catalog.findSubjectMatter("XXX").isEmpty());
    }

    @Test
    void refusesToRenameWithTheNameOfAnotherMatter() {
        SubjectMatterService.Saving saving = service.update(admin, "MER", form("MER", "penal", null));

        assertEquals(SubjectMatterService.NAME_TAKEN, saving.errors().get("name"));
        assertEquals("Mercantil", catalog.findSubjectMatter("MER").orElseThrow().getName());
    }

    @Test
    void aDeactivatedMatterIsNoLongerOfferedForNewRequests() {
        service.changeStatus(admin, "FAM", false);

        assertEquals(List.of("Civil", "Mercantil", "Penal"), service.activeNames());
        assertFalse(catalog.findSubjectMatter("FAM").orElseThrow().isActive());

        service.changeStatus(admin, "FAM", true);
        assertEquals(List.of("Civil", "Familiar", "Mercantil", "Penal"), service.activeNames());
    }

    @Test
    void onlyTheJudicialBodyAdminManagesTheCatalog() {
        assertThrows(AccessDeniedException.class, () -> service.search(courtAdmin, "", null, 1));
        assertThrows(AccessDeniedException.class, () -> service.register(courtAdmin, form("AMP", "Amparo", null)));
        assertThrows(AccessDeniedException.class, () -> service.changeStatus(null, "CIV", false));
        assertTrue(catalog.findSubjectMatter("CIV").orElseThrow().isActive());
    }

    @Test
    void anUnknownCodeIsNotFound() {
        assertThrows(NotFoundException.class, () -> service.get(admin, "ZZZ"));
        assertThrows(NotFoundException.class, () -> service.changeStatus(admin, "ZZZ", false));
    }
}
